package Exchange;

import classes.Enums.OrderSide;
import classes.Enums.OrderSnapShotType;
import classes.Enums.OrderStatus;
import classes.Enums.OrderType;
import classes.WebSocket.ServerWSController;
import classes.WebSocket.messages.*;
import classes.WebSocket.model.EExecution;
import classes.WebSocket.model.EOrder;
import classes.trading.ExchangeStorage;
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
    private ExchangeStorage currentExchangeStorage;

    //Connector run type
    private RunType ConnectorType;

    public PFTLMAXEventsClient(List<Instrument> InstrumentList, String Exchange, ExchangeStorage exchangeStorage,RunType ConnectorType){
        this.Exchange = Exchange;
        this.InstrumentList=InstrumentList;
        this.currentBalanceMessage = new BalanceMessage(this.Exchange);
        this.currentSingleOrderMessage = new SingleOrderMessage();
        this.currentExchangeStorage = exchangeStorage;
        this.ConnectorType=ConnectorType;

    }

    public ExchangeStorage getCurrentExchangeStorage() {
        return currentExchangeStorage;
    }

    public void setCurrentExchangeStorage(ExchangeStorage currentExchangeStorage) {
        this.currentExchangeStorage = currentExchangeStorage;
    }

    public void setWsController(ServerWSController wsController) {
        this.wsController = wsController;
        currentExchangeStorage.setWsController(wsController);
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
        if(ConnectorType == RunType.FULLTRADE || ConnectorType==RunType.ALL) {
            session.registerAccountStateEventListener(this);
            session.registerStreamFailureListener(this);
            session.registerOrderEventListener(this);
            session.registerInstructionRejectedEventListener(this);
            session.registerExecutionEventListener(this);
            session.registerPositionEventListener(this);

            //Subscribe to messages
            subscribe(session, new PositionSubscriptionRequest(), "Positions");
            subscribe(session, new AccountSubscriptionRequest(), "Account Updates");
        }
        if(ConnectorType==RunType.ALL || ConnectorType==RunType.FULLMARKET) {
            session.registerOrderBookEventListener(this);
            for(Instrument instrument : this.InstrumentList) {
                subscribeToInstrument(session, Long.parseLong(instrument.GetExchangeSymbol()));
            }
        }





        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    int storeTime = 10;
                    trclog.log(Level.INFO,"Sending:"+currentOrderSnapshotMessage.getClass().toString()+": "+currentOrderSnapshotMessage.toString());
                    while (true)
                    {
                    //  Sending scheduled ordersnapshot
                        currentExchangeStorage.CleanUpTemp(storeTime);
                        //synchronized (currentExchangeStorage.getWorkingOrders()) {
                            //currentOrderSnapshotMessage.setOrdersSnapshot(currentExchangeStorage.getTempOrders());
                            //wsController.SendOrderSnapshotPointMessage(currentOrderSnapshotMessage, 10);
                            Thread.sleep(storeTime * 1000);
                        //}
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


    public void SendOrder(classes.trading.Order limitOrder){
        //Convert order params in current_exchange format
        FixedPointNumber limitPrice = FixedPointNumber.valueOf(limitOrder.getPrice().toString());
        double lSize = Math.abs(limitOrder.getSize());
        if(limitOrder.getSide() == OrderSide.SELL){
            lSize=lSize*(-1);
        }
        FixedPointNumber limitSize = FixedPointNumber.valueOf(String.valueOf(lSize));

        //Insert order instruction id from client to storage with null id from exchange, because exchange don't
        //respond yet, place order with order instruction id to storage
        currentExchangeStorage.putNewOrderRelation(limitOrder);

        //Place order and get response from exchange with order ID or instructionID(LMAX)
        if(limitOrder.getType() == OrderType.LIMIT) {
            currentSession.placeLimitOrder(new LimitOrderSpecification(Long.parseLong(limitOrder.getSymbol()), limitPrice
                    , limitSize, TimeInForce.GOOD_TIL_CANCELLED), new OrderCallback() {
                @Override
                public void onSuccess(String instructionId) {
                    trclog.log(Level.INFO, "Order instruction_id: " + instructionId);
                    //currentExchangeStorage.putNewOrderRelation();
                    synchronized (currentExchangeStorage.getOrderRelations()) {
                        currentExchangeStorage.getOrderRelations().replace(limitOrder.getInstructionKey(), instructionId);
                    }
                    trclog.log(Level.INFO, "Order accepted by exchange: " + instructionId);
                    wsController.SendInfoPointMessage(new InfoMessage(" Order with instruction " + limitOrder.getInstructionKey() + " accepted by exchange: " + instructionId));
                    //Distribute order in storage
                    synchronized (getCurrentExchangeStorage()) {
                        currentExchangeStorage.distributeNewOrderId(limitOrder.getInstructionKey(), instructionId, OrderStatus.ACCEPTED);
                    }

                    //Send relation to client for information
                    InfoMessage response = new InfoMessage(limitOrder.getID() + ":" +
                            instructionId);
                    wsController.SendInfoPointMessage(response);
                    while(true) {
                        List<EOrder> sendedOrder = wsController.getOrderByExchangeId(instructionId);
                        trclog.log(Level.INFO,"Order check "+sendedOrder.toString());
                        if(sendedOrder.size() > 0){
                            updateAfterSucces(limitOrder,sendedOrder.get(0),instructionId);
                            break;
                        }
                    }
                }

                @Override
                public void onFailure(FailureResponse failureResponse) {
                    //Log and sending failure response to client
                    trclog.log(Level.WARNING, failureResponse.toString());
                    wsController.SendInfoPointMessage(new InfoMessage(failureResponse.toString()));
                }
            });
        } else if(limitOrder.getType()==OrderType.MARKET){
            currentSession.placeMarketOrder(new MarketOrderSpecification(Long.parseLong(limitOrder.getSymbol())
                    , limitSize, TimeInForce.FILL_OR_KILL), new OrderCallback() {
                @Override
                public void onSuccess(String instructionId) {
                    trclog.log(Level.INFO, "Order instruction_id: " + instructionId);
                    //currentExchangeStorage.putNewOrderRelation();
                    synchronized (currentExchangeStorage.getOrderRelations()) {
                        currentExchangeStorage.getOrderRelations().replace(limitOrder.getInstructionKey(), instructionId);
                    }
                    trclog.log(Level.INFO, "Order accepted by exchange: " + instructionId);
                    wsController.SendInfoPointMessage(new InfoMessage(" Order with instruction " + limitOrder.getInstructionKey() + " accepted by exchange: " + instructionId));
                    //Distribute order in storage
                    synchronized (getCurrentExchangeStorage()) {
                        currentExchangeStorage.distributeNewOrderId(limitOrder.getInstructionKey(), instructionId, OrderStatus.ACCEPTED);
                    }

                    //Send relation to client for information
                    InfoMessage response = new InfoMessage(limitOrder.getID() + ":" +
                            instructionId);
                    wsController.SendInfoPointMessage(response);

                    while(true) {
                        List<EOrder> sendedOrder = wsController.getOrderByExchangeId(instructionId);
                        trclog.log(Level.INFO,"Order check "+sendedOrder.toString());
                        if(sendedOrder.size() > 0){
                            trclog.log(Level.INFO,"Order found "+sendedOrder.toString());
                            updateAfterSucces(limitOrder,sendedOrder.get(0),instructionId);
                            break;
                        }
                    }
                }

                @Override
                public void onFailure(FailureResponse failureResponse) {
                    //Log and sending failure response to client
                    trclog.log(Level.WARNING, failureResponse.toString());
                    wsController.SendInfoPointMessage(new InfoMessage(failureResponse.toString()));
                }
            });
        }
    }

    private void updateAfterSucces(classes.trading.Order successedOrder, EOrder eOrder,String instructionId){
        InfoMessage iMsg= new InfoMessage("LMAX responded, order found id DATABASE"+eOrder.toString());
        EOrder updatedOrder = new EOrder();
        updatedOrder.setStrategy(successedOrder.getStrategy());
        updatedOrder.setExchangeId(instructionId);
        updatedOrder.setInstructionKey(successedOrder.getInstructionKey());
        updatedOrder.setExchangeSymbol(successedOrder.getSymbol());
        updatedOrder.setOrderType(successedOrder.getType());
        updatedOrder.setInitTimestamp(successedOrder.getInitTimestamp());
        updatedOrder.setLeftSymbol(successedOrder.getLeftSymbol());
        updatedOrder.setRightSymbol(successedOrder.getRightSymbol());
        updatedOrder.setSize(successedOrder.getSize());
        wsController.updateClientDBOrder(updatedOrder);
        wsController.SendInfoPointMessage(iMsg);
        List<EOrder> sendedOrder2 = wsController.getOrderByExchangeId(instructionId);
        InfoMessage iMsg2= new InfoMessage("LMAX updated, order found id DATABASE"+sendedOrder2.get(0).toString());
        wsController.SendInfoPointMessage(iMsg2);
    }

    @Override
    public void notifySessionDisconnected()
    {
        trclog.log(Level.WARNING,"Session disconnected");
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
        trclog.log(Level.INFO,execution.toString());
        currentExecution.setId(String.valueOf(execution.getExecutionId()));
        currentExecution.setOrderId(execution.getOrder().getOriginalInstructionId());
        if(execution.getPrice()==null) {
            currentExecution.setPrice(0.0);//Double.parseDouble(String.valueOf(execution.getPrice().longValue())));
        }else{
            currentExecution.setPrice(Double.parseDouble(execution.getPrice().toString()));
        }
            currentExecution.setFilled(Double.parseDouble(execution.getQuantity().toString()));
        if(Double.parseDouble(execution.getQuantity().toString()) != 0) {
            currentExecution.setExecuted(Double.parseDouble(execution.getPrice().toString())*Double.parseDouble(execution.getQuantity().toString()));
        }
        currentExecution.setTimestamp(currentTimeMillis());


        trclog.log(Level.INFO,currentExecution.toString());
        //Send execution to client
        this.currentSingleExecutionMessage.setExecution(this.currentExecution);
        wsController.SendSingleExecutionPointMessage(this.currentSingleExecutionMessage);

        EExecution eexecution = new EExecution(this.Exchange,currentExecution.getId(),currentExecution.getOrderId(),
                "",currentExecution.getPrice(),currentExecution.getFilled(),0.0,currentTimeMillis());
        wsController.updateDBexecution(eexecution);

    }

    @Override
    public void notify(InstructionRejectedEvent instructionRejected)
    {
        String rejectedId = instructionRejected.getInstructionId();
        trclog.log(Level.WARNING,instructionRejected.toString());
    }

    @Override
    public void notify(Order order)
    {
        //Order order1 = (Order) DeepObjectCopy.clone(order);
        classes.trading.Order messageOrder = new classes.trading.Order();
        String ExchangeOrderId = "";
        trclog.log(Level.INFO,order.toString());
        String orderId = order.getOriginalInstructionId();
        messageOrder.setExchange(this.Exchange);
        messageOrder.setExchangeID(order.getOriginalInstructionId());
        messageOrder.setSymbol(String.valueOf(order.getInstrumentId()));
        messageOrder.setFilled(Math.abs(Double.parseDouble(order.getFilledQuantity().toString())));
        messageOrder.setSize(Math.abs(Double.parseDouble(order.getQuantity().toString())));
        messageOrder.setCancelled_qty(Double.parseDouble(order.getCancelledQuantity().toString()));
        messageOrder.setLastUpdate(currentTimeMillis());
        if(Double.parseDouble(order.getQuantity().toString()) < 0){
            messageOrder.setSide(OrderSide.SELL);
        }else{
            messageOrder.setSide(OrderSide.BUY);
        }

        if(order.getLimitPrice()==null){
            messageOrder.setPrice(Double.parseDouble(order.getStopReferencePrice().toString()));
        }else{
            messageOrder.setPrice(Double.parseDouble(order.getLimitPrice().toString()));
        }

        messageOrder.setStatus(detectOrderStatus(messageOrder,false));

        EOrder exchangeOrder = new EOrder("",messageOrder.getExchangeID(),"",
                messageOrder.getPrice(),messageOrder.getSize(),messageOrder.getSide());
        exchangeOrder.setFilled(messageOrder.getFilled());
        exchangeOrder.setStatus(messageOrder.getStatus());
        exchangeOrder.setUpdateTimestamp(messageOrder.getLastUpdate());
        //exchangeOrder.setExecuted(messageOrder.getExecuted());

        wsController.updateDBorderFromexchange(exchangeOrder);


        trclog.log(Level.INFO,"Order Id:"+messageOrder.getExchangeID());
        //Distibute order message
        OrderStatus returnSnapStatus = currentExchangeStorage.distributeExchangeOrderMessage(messageOrder);

        trclog.log(Level.INFO,messageOrder.toString());
        //trclog.log(Level.INFO,currentExchangeStorage.toString());

        //Sending order to client
        this.currentSingleOrderMessage.setOrder(messageOrder);
        wsController.SendSingleOrderPointMessage(this.currentSingleOrderMessage);


        if(currentExchangeStorage.getOrderRelations().containsValue(messageOrder.getExchangeID())) {
            //Send working snapshot if order from message in working storage
            if (returnSnapStatus == OrderStatus.CANCELLED) {
                synchronized (getCurrentExchangeStorage().getCancelledOrders()) {
                    if (getCurrentExchangeStorage().getCancelledOrders().containsKey(messageOrder.getExchangeID())) {
                        OrdersSnapshotMessage currentCancelledSnapshot = new OrdersSnapshotMessage();
                        currentCancelledSnapshot.setOrdersSnapshot(getCurrentExchangeStorage().getCancelledOrders());
                        currentCancelledSnapshot.setOrderSnapShotType(OrderSnapShotType.CANCELLED);
                        wsController.SendOrderSnapshotPointMessage(currentCancelledSnapshot, 10);
                    }
                }
            } else if (returnSnapStatus == OrderStatus.REJECTED) {
                synchronized (getCurrentExchangeStorage().getRejectedOrders()) {
                    if (getCurrentExchangeStorage().getRejectedOrders().containsKey(messageOrder.getExchangeID())) {
                        OrdersSnapshotMessage currentRejectedSnapshot = new OrdersSnapshotMessage();
                        currentRejectedSnapshot.setOrdersSnapshot(getCurrentExchangeStorage().getRejectedOrders());
                        currentRejectedSnapshot.setOrderSnapShotType(OrderSnapShotType.REJECTED);
                        wsController.SendOrderSnapshotPointMessage(currentRejectedSnapshot, 10);
                    }
                }

            } else if (returnSnapStatus == OrderStatus.FULL_FILLED) {
                synchronized (getCurrentExchangeStorage().getFilledOrders()) {
                    if (getCurrentExchangeStorage().getFilledOrders().containsKey(messageOrder.getExchangeID())) {
                        OrdersSnapshotMessage currentFilledSnapshot = new OrdersSnapshotMessage();
                        currentFilledSnapshot.setOrdersSnapshot(getCurrentExchangeStorage().getFilledOrders());
                        currentFilledSnapshot.setOrderSnapShotType(OrderSnapShotType.FILLED);
                        wsController.SendOrderSnapshotPointMessage(currentFilledSnapshot, 10);
                    }
                }

            } else {
                synchronized (getCurrentExchangeStorage().getWorkingOrders()) {
                    if (getCurrentExchangeStorage().getWorkingOrders().containsKey(messageOrder.getExchangeID())) {
                        trclog.log(Level.INFO, "WORKING_STATUS" + messageOrder.toString() + " " + order.toString());
                        OrdersSnapshotMessage currentWorkingSnapshot = new OrdersSnapshotMessage();
                        currentWorkingSnapshot.setOrdersSnapshot(getCurrentExchangeStorage().getWorkingOrders());
                        currentWorkingSnapshot.setOrderSnapShotType(OrderSnapShotType.WORKING);
                        wsController.SendOrderSnapshotPointMessage(currentWorkingSnapshot, 10);
                    }
                }
            }
        }

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
        wsController.SendBBOPointMessage(currentBBOMessage);
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
        //wsController.SendBalancePointMessage(this.currentBalanceMessage);
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

    private OrderStatus detectOrderStatus(classes.trading.Order LmaxOrderInstruction,boolean isRejected){
        OrderStatus orderStatus = OrderStatus.UNKNOWN;
        //if(Double.parseDouble(LmaxOrderInstruction.getCancelledQuantity().toString()) > 0)
        if(Math.abs(LmaxOrderInstruction.getCancelled_qty()) > 0)
        {
            orderStatus=OrderStatus.CANCELLED;
        }else if(LmaxOrderInstruction.getFilled().equals(LmaxOrderInstruction.getSize())){//LmaxOrderInstruction.getQuantity().longValue() == LmaxOrderInstruction.getFilledQuantity().longValue()){
            orderStatus=OrderStatus.FULL_FILLED;
        }else if(LmaxOrderInstruction.getFilled()>0){ //(Double.parseDouble(LmaxOrderInstruction.getFilledQuantity().toString()) > 0){
            orderStatus = OrderStatus.PART_FILLED;
        } else if(LmaxOrderInstruction.getFilled()==0){//.getFilledQuantity().longValue() == 0){
            orderStatus=OrderStatus.ACCEPTED;
        }
        if(isRejected){
            orderStatus=OrderStatus.REJECTED;
        }
        return orderStatus;
    }

}
