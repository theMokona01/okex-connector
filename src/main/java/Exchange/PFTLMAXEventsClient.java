package Exchange;

import classes.Enums.OrderSide;
import classes.WebSocket.ServerWSController;
import classes.WebSocket.messages.*;
import classes.trading.ExchangeStorage;
import com.google.gson.Gson;
import com.lmax.api.*;
import com.lmax.api.account.*;
import com.lmax.api.order.*;
import com.lmax.api.orderbook.OrderBookEvent;
import com.lmax.api.orderbook.OrderBookEventListener;
import com.lmax.api.orderbook.OrderBookSubscriptionRequest;
import com.lmax.api.orderbook.PricePoint;
import com.lmax.api.position.PositionEvent;
import com.lmax.api.position.PositionEventListener;
import com.lmax.api.position.PositionSubscriptionRequest;
import com.lmax.api.reject.InstructionRejectedEvent;
import com.lmax.api.reject.InstructionRejectedEventListener;
import interfaces.Instrument;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.currentTimeMillis;


public class PFTLMAXEventsClient implements LoginCallback, AccountStateEventListener, OrderBookEventListener,
        StreamFailureListener, OrderEventListener, InstructionRejectedEventListener, ExecutionEventListener, SessionDisconnectedListener,
        PositionEventListener {
    private String Exchange;
    //Lmax variables
    private List<Instrument> InstrumentList;
    private Session currentSession;

    //Trading state variables
    HashMap<String, classes.trading.Order> OrdersState = new HashMap<>();
    HashMap<String, classes.trading.Execution> ExecutionState = new HashMap<>();

    //WSController variables
    private MessageEndPoint endpoints = new MessageEndPoint();
    private ServerWSController wsController = null;

    //Working messages variables
    private BBOMessage currentBBOMessage= new BBOMessage();
    private BalanceMessage currentBalanceMessage;// = new BalanceMessage(Exchange);
    //private classes.trading.Order currentOrder = new classes.trading.Order();
    private classes.trading.Execution currentExecution = new classes.trading.Execution();
    private SingleOrderMessage currentSingleOrderMessage = new SingleOrderMessage();
    private SingleExecutionMessage currentSingleExecutionMessage = new SingleExecutionMessage();
    private OrdersSnapshotMessage currentOrderSnapshotMessage = new OrdersSnapshotMessage();

    //Logger variables
    private Logger trclog = Logger.getLogger(PFTLMAXEventsClient.class.getName());

    //Connector storage variables
    ExchangeStorage currentExchangeStorage;

    public PFTLMAXEventsClient(List<Instrument> InstrumentList, String Exchange, ExchangeStorage exchangeStorage){
        this.Exchange = Exchange;
        this.InstrumentList=InstrumentList;
        this.currentBalanceMessage = new BalanceMessage(this.Exchange);
        this.currentSingleOrderMessage = new SingleOrderMessage();
        this.currentExchangeStorage = exchangeStorage;

    }

    public void setWsController(ServerWSController wsController) {
        this.wsController = wsController;
    }
    public Session getLmaxSession(){
        return this.currentSession;
    }

    @Override
    public void onLoginSuccess(final Session session)
    {
        trclog.log(Level.INFO,"Logged in, subscribing");
        trclog.log(Level.INFO,"My accountId is: " + session.getAccountDetails().getAccountId()+" "+session.getAccountDetails().toString());
        //Register LMAX listeners
        session.registerAccountStateEventListener(this);
        session.registerOrderBookEventListener(this);
        session.registerStreamFailureListener(this);
        session.registerOrderEventListener(this);
        session.registerInstructionRejectedEventListener(this);
        session.registerExecutionEventListener(this);
        session.registerPositionEventListener(this);

        //Subscribe to messages
        subscribe(session, new PositionSubscriptionRequest(), "Positions");
        subscribe(session, new AccountSubscriptionRequest(), "Account Updates");

        for(Instrument instrument : this.InstrumentList) {
            subscribeToInstrument(session, Long.parseLong(instrument.GetExchangeSymbol()));
        }

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    //HashMap<String,classes.trading.Order> ord = new HashMap<>();
                    //ord.put("TEST", new classes.trading.Order());
                    //currentOrderSnapshotMessage.setOrdersSnapshot(ord);
                    trclog.log(Level.INFO,"Sending:"+currentOrderSnapshotMessage.getClass().toString()+": "+currentOrderSnapshotMessage.toString());
                    while (true)
                    {
                    //   Sending something scheduled
                    //    currentOrderSnapshotMessage.setOrdersSnapshot(OrdersState);
                        //wsController.SendOrderSnapshotPointMessage(currentOrderSnapshotMessage,10);
                        CleanUpTemp(3);
                        Thread.sleep(3000);
                    }
                }
                catch (Exception e)
                {
                    trclog.log(Level.WARNING,e.getMessage());
                    e.printStackTrace();
                }
            }
        }, 1L, 1L, TimeUnit.SECONDS);
        this.currentSession = session;
        session.start();
    }
    public void CleanUpTemp(int unusedTimeSec){
        trclog.log(Level.INFO,"CleanUp: "+String.valueOf(unusedTimeSec)+" sec");
        long CurrentTimestamp = currentTimeMillis();
        Set<String> toRemoveId = new HashSet<>();
        synchronized (currentExchangeStorage.getTempOrders()){
            for(Map.Entry<String, classes.trading.Order> orderEntry: currentExchangeStorage.getTempOrders().entrySet()){
                long diff = CurrentTimestamp - orderEntry.getValue().getLastUpdate();
                trclog.log(Level.INFO,"Clean check "+orderEntry.getKey()+" "+String.valueOf(diff));
                if(CurrentTimestamp - orderEntry.getValue().getLastUpdate() > unusedTimeSec*1000){
                    toRemoveId.add(orderEntry.getKey());
                    //currentExchangeStorage.getTempOrders().remove(orderEntry.getKey());
                    trclog.log(Level.INFO,"CleanedUp: "+orderEntry.getKey());
                }
            }
            currentExchangeStorage.getTempOrders().keySet().removeAll(toRemoveId);
        }

    }

    public void SendOrder(classes.trading.Order limitOrder){
        FixedPointNumber limitPrice = FixedPointNumber.valueOf(limitOrder.getPrice().toString());
        double lSize = Math.abs(limitOrder.getSize());
        if(limitOrder.getSide() == OrderSide.SELL){
            lSize=lSize*(-1);
        }
        synchronized (currentExchangeStorage.getOrderRelations()) {
            if(currentExchangeStorage.getOrderRelations().containsKey(limitOrder.getInstructionKey())){
                trclog.log(Level.INFO,limitOrder.getInstructionKey()+" exist in current storage(duplicated)");
            }else {
                currentExchangeStorage.getOrderRelations().put(limitOrder.getInstructionKey(),null);
            }
        }
        FixedPointNumber limitSize = FixedPointNumber.valueOf(String.valueOf(lSize));
        currentSession.placeLimitOrder(new LimitOrderSpecification(Long.parseLong(limitOrder.getSymbol()),limitPrice
                , limitSize, TimeInForce.GOOD_TIL_CANCELLED), new OrderCallback()
        {
            @Override
            public void onSuccess(String instructionId) {
                trclog.log(Level.INFO, "Order instruction_id: " + instructionId);
                synchronized (currentExchangeStorage.getOrderRelations()) {
                    currentExchangeStorage.getOrderRelations().replace(limitOrder.getInstructionKey(), instructionId);
                }
                //Send relation to client
                InfoMessage response = new InfoMessage(limitOrder.getID()+":"+
                        instructionId);
                wsController.SendInfoPointMessage(response);
            }

            @Override
            public void onFailure(FailureResponse failureResponse)
            {
                trclog.log(Level.WARNING,failureResponse.toString());
            }
        });
    }

    @Override
    public void notifySessionDisconnected()
    {
        trclog.log(Level.WARNING,"Session disconnected");
        /*System.out.println("notifySessionDisconnected");
        Runnable LmaxRunner = new PfLmax("Profluent", "Il&09IsHA!KbbeR%", "https://trade.lmaxtrader.com/", "LMAX",InstrumentList); // or an anonymous class, or lambda...
        Thread LmaxThread = new Thread(LmaxRunner);
        LmaxThread.start();*/
    }


    @Override
    public void notifyStreamFailure(Exception e)
    {
        trclog.log(Level.WARNING,"Error occured on the stream: "+e.getMessage());
        e.printStackTrace(System.out);

    }

    @Override
    public void notify(Execution execution)
    {
        currentExecution.setFilled(Double.parseDouble(execution.getQuantity().toString()));
        if(Double.parseDouble(execution.getQuantity().toString()) != 0) {
            currentExecution.setExecuted(Double.parseDouble(execution.getPrice().toString())*Double.parseDouble(execution.getQuantity().toString()));
        }
        currentExecution.setTimestamp(currentTimeMillis());
        trclog.log(Level.INFO,execution.toString());

        trclog.log(Level.INFO,currentExecution.toString());
        //Send execution to client
        this.currentSingleExecutionMessage.setExecution(this.currentExecution);
        wsController.SendSingleExecutionPointMessage(this.currentSingleExecutionMessage);

    }

    @Override
    public void notify(InstructionRejectedEvent instructionRejected)
    {
        trclog.log(Level.INFO,instructionRejected.toString());
    }

    @Override
    public void notify(Order order)
    {
        classes.trading.Order messageOrder = new classes.trading.Order();
        String ExchangeOrderId = "";
        trclog.log(Level.INFO,order.toString());
        String orderId = order.getOriginalInstructionId();
        messageOrder.setExchange(this.Exchange);
        messageOrder.setExchangeID(order.getOriginalInstructionId());
        messageOrder.setSymbol(String.valueOf(order.getInstrumentId()));
        messageOrder.setFilled(Double.parseDouble(order.getFilledQuantity().toString()));
        messageOrder.setSize(Math.abs(Double.parseDouble(order.getQuantity().toString())));
        messageOrder.setLastUpdate(currentTimeMillis());
        if(Double.parseDouble(order.getQuantity().toString()) < 0){
            messageOrder.setSide(OrderSide.SELL);
        }else{
            messageOrder.setSide(OrderSide.BUY);
        }
        //classes.trading.Order cOrder = this.currentOrder;
        if(this.OrdersState.containsKey(orderId)) {
            //OrdersState.replace(orderId,cOrder);
        }else{
                //if(OrdersState.size() < 30) {
                    //OrdersState.put(orderId, cOrder);
                //}
        }
        trclog.log(Level.INFO,"Order Id:"+messageOrder.getExchangeID());
        //Distibute order message
        synchronized (currentExchangeStorage.getTempOrders()) {
            if(currentExchangeStorage.getWorkingOrders().containsKey(messageOrder.getExchangeID())) {
                currentExchangeStorage.updateWorkingOrder(messageOrder);
            }else{
                if(currentExchangeStorage.getTempOrders().containsKey(messageOrder.getExchangeID())){
                    currentExchangeStorage.getTempOrders().replace(messageOrder.getExchangeID(),messageOrder);
                    trclog.log(Level.INFO,"Update order:"+messageOrder.getExchangeID());

                }else{
                 currentExchangeStorage.getTempOrders().put(messageOrder.getExchangeID(),messageOrder);
                    trclog.log(Level.INFO,"Put new order:"+messageOrder.getExchangeID());
                }
            }
        }
        trclog.log(Level.INFO,messageOrder.toString());
        trclog.log(Level.INFO,currentExchangeStorage.toString());
        //Sending order to client
        this.currentSingleOrderMessage.setOrder(messageOrder);
        wsController.SendSingleOrderPointMessage(this.currentSingleOrderMessage);

    }

    @Override
    public void onLoginFailure(FailureResponse failureResponse)
    {
        trclog.log(Level.WARNING,"Login Failed: " + failureResponse);
    }

    public void notify(final OrderBookEvent orderBookEvent)
    {
        double Ask = 0;
        double Bid = 0;
        double Ask_Size = 0;
        double Bid_Size = 0;
        List<PricePoint> Asks=orderBookEvent.getAskPrices();
        List<PricePoint> Bids=orderBookEvent.getBidPrices();
        if(Asks.size()>0){
            Ask = Double.parseDouble(Asks.get(0).getPrice().toString());
            Ask_Size = Double.parseDouble(Asks.get(0).getQuantity().toString());
        }
        if(Bids.size()>0){
            Bid = Double.parseDouble(Bids.get(0).getPrice().toString());
            Bid_Size = Double.parseDouble(Bids.get(0).getQuantity().toString());
        }
        long instrument_id = orderBookEvent.getInstrumentId();
        String message = String.valueOf(instrument_id)+", ask: "+String.valueOf(Ask)+", bid: "+String.valueOf(Bid);
        //trclog.log(Level.INFO,orderBookEvent.toString());
        currentBBOMessage.setAsk(Ask);
        currentBBOMessage.setBid(Bid);
        currentBBOMessage.setAsk_size(Ask_Size);
        currentBBOMessage.setBid_size(Bid_Size);
        currentBBOMessage.setInstrument(String.valueOf(instrument_id));
        currentBBOMessage.setTimestamp(orderBookEvent.getTimeStamp());
        //wsController.SendBBOPointMessage(currentBBOMessage);
    }

    @Override
    public void notify(final PositionEvent positionEvent)
    {
        trclog.log(Level.INFO,positionEvent.toString());
    }

    public void notify(final AccountStateEvent accountStateEvent)
    {
        //trclog.log(Level.INFO,accountStateEvent.toString());
        Map<String, Wallet> Wallets  = accountStateEvent.getCurrencyWallets();
        String Symbol="";
        Double Cash=0.0;
        Double Credit=0.0;
        this.currentBalanceMessage.CleanBalance();
        for(Map.Entry<String,Wallet> entry : Wallets.entrySet())
        {
             Symbol = entry.getKey();
             Cash = Double.parseDouble(entry.getValue().getCash().toString());
             Credit = Double.parseDouble(entry.getValue().getCredit().toString());
             this.currentBalanceMessage.setSymbolBalance(Symbol,Cash+Credit);
        }
        currentBalanceMessage.setExchange(this.Exchange);
        wsController.SendBalancePointMessage(this.currentBalanceMessage);
    }

    private void subscribe(final Session session, final SubscriptionRequest request, final String subscriptionDescription)
    {
        session.subscribe(request, new Callback()
        {
            public void onSuccess()
            {

                trclog.log(Level.INFO,"Subscribed to " + subscriptionDescription);
            }

            public void onFailure(final FailureResponse failureResponse)
            {
                trclog.log(Level.WARNING,"Failed to subscribe to " + subscriptionDescription + ": %s%n", failureResponse);
            }
        });
    }


    private void subscribeToInstrument(final Session session, final long instrumentId)
    {
        session.subscribe(new OrderBookSubscriptionRequest(instrumentId), new Callback()
        {
            public void onSuccess()
            {
                trclog.log(Level.INFO,"Subscribed to instrument "+ String.valueOf(instrumentId));
            }

            public void onFailure(final FailureResponse failureResponse)
            {
                trclog.log(Level.WARNING,"Failed to subscribe to instrument %"+String.valueOf(instrumentId)+" "+failureResponse);
            }
        });
    }

}
