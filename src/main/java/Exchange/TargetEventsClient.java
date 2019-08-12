package Exchange;

import classes.Enums.*;
import classes.Enums.OrderType;
import classes.WebSocket.ServerWSController;
import classes.WebSocket.messages.*;
import classes.WebSocket.model.EExecution;
import classes.WebSocket.model.EOrder;
import com.lmax.api.*;
import com.lmax.api.account.*;
import com.lmax.api.order.*;
//import com.lmax.api.order.OrderType;
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


public class TargetEventsClient implements LoginCallback, AccountStateEventListener, OrderBookEventListener,
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
    private Logger trclog = Logger.getLogger(TargetEventsClient.class.getName());

    //Locker for update orders on start
    boolean orderLocker = false;

    //Connector run type
    private RunType ConnectorType;

    public TargetEventsClient(List<Instrument> InstrumentList, String Exchange, RunType ConnectorType){
        this.Exchange = Exchange;
        this.InstrumentList=InstrumentList;
        this.currentBalanceMessage = new BalanceMessage(this.Exchange);
        this.currentSingleOrderMessage = new SingleOrderMessage();
        this.ConnectorType=ConnectorType;

    }


    public void setWsController(ServerWSController wsController) {
        this.wsController = wsController;
        //currentExchangeStorage.setWsController(wsController);
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
                            //wsController.cleanOldTrashOrders(10);
                        Thread.sleep(storeTime * 2000);
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


    public void CancelOrder(classes.trading.Order order){
        currentSession.cancelOrder(new CancelOrderRequest(Long.parseLong(order.getExchangeSymbol()),order.getExchangeID()), new OrderCallback() {
            @Override
            public void onSuccess(String s) {

            }

            @Override
            public void onFailure(FailureResponse failureResponse) {

            }
        });
    }

    public void SendOrder(classes.trading.Order limitOrder){
        //Convert order params in current_exchange format
        FixedPointNumber limitPrice = FixedPointNumber.valueOf(limitOrder.getPrice().toString());
        double lSize = Math.abs(limitOrder.getSize());
        if(limitOrder.getSide() == OrderSide.SELL){
            lSize=lSize*(-1);
        }
        FixedPointNumber limitSize = FixedPointNumber.valueOf(String.valueOf(lSize));
        //trclog.log(Level.INFO, "Order exchange symbol: " + limitOrder.getExchangeSymbol());
        //trclog.log(Level.INFO, "Order user symbol: " + limitOrder.getUserSymbol());
        //Place order and get response from exchange with order ID or instructionID(LMAX)
        if(limitOrder.getType() == OrderType.LIMIT) {
            currentSession.placeLimitOrder(new LimitOrderSpecification(Long.parseLong(limitOrder.getExchangeSymbol()), limitPrice
                    , limitSize, TimeInForce.GOOD_TIL_CANCELLED), new OrderCallback() {
                @Override
                public void onSuccess(String instructionId) {

                    trclog.log(Level.INFO, "Order accepted by exchange: " + instructionId);

                    //Send relation to client for information
                    try {
                        JSONObject successedId = new JSONObject();
                        successedId.put("instructionKey", limitOrder.getInstructionKey());
                        successedId.put("exchangeId", instructionId);
                        InfoMessage response = new InfoMessage("{" + limitOrder.getInstructionKey() + ":" +
                                instructionId + "}");
                        response.setContent(successedId.toString());
                        response.setInfoType(InfoType.ORDER_MANAGEMENT);
                        wsController.SendInfoPointMessage(response);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    while(true) {
                        List<EOrder> sendedOrder = wsController.getOrderByExchangeId(instructionId);
                        trclog.log(Level.INFO,"Order check "+sendedOrder.toString());
                        limitOrder.setExchangeID(instructionId);
                        if(sendedOrder.size() > 0){
                            updateAfterSuccess(limitOrder,sendedOrder.get(0),instructionId);
                            break;
                        }else{
                            //Insert order
                            insretAfterSucces(limitOrder);
                        }
                        try {
                            Thread.sleep(1000);
                        }catch(Exception e){
                            trclog.log(Level.WARNING, e.toString());
                        }
                    }
                }

                @Override
                public void onFailure(FailureResponse failureResponse) {
                    //Log and sending failure response to client
                    trclog.log(Level.WARNING, failureResponse.toString());
                    wsController.SendInfoPointMessage(new InfoMessage("{\"error\":\""+failureResponse.toString()+"\"}"));
                }
            });
        } else if(limitOrder.getType()==OrderType.MARKET){
            currentSession.placeMarketOrder(new MarketOrderSpecification(Long.parseLong(limitOrder.getExchangeSymbol())
                    , limitSize, TimeInForce.FILL_OR_KILL), new OrderCallback() {
                @Override
                public void onSuccess(String instructionId) {
                    trclog.log(Level.INFO, "Order accepted by exchange: " + instructionId);

                    //Send relation to client for information
                    try {
                        JSONObject successedId = new JSONObject();
                        successedId.put("instructionKey", limitOrder.getInstructionKey());
                        successedId.put("exchangeId", instructionId);
                        InfoMessage response = new InfoMessage("{" + limitOrder.getInstructionKey() + ":" +
                                instructionId + "}");
                        response.setContent(successedId.toString());
                        response.setInfoType(InfoType.ORDER_MANAGEMENT);
                        wsController.SendInfoPointMessage(response);
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                    while(true) {
                        List<EOrder> sendedOrder = wsController.getOrderByExchangeId(instructionId);
                        trclog.log(Level.INFO,"Order check "+sendedOrder.toString());
                        if(sendedOrder.size() > 0){
                            trclog.log(Level.INFO,"Order found "+sendedOrder.toString());
                            updateAfterSuccess(limitOrder,sendedOrder.get(0),instructionId);
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        }catch(Exception e){
                            trclog.log(Level.WARNING, e.toString());
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

    private void insretAfterSucces(classes.trading.Order order){

    }

    private void updateAfterSuccess(classes.trading.Order successedOrder, EOrder eOrder,String instructionId){
        //InfoMessage iMsg= new InfoMessage("LMAX responded, order found id DATABASE"+eOrder.toString()+" user symbol "+successedOrder.getUserSymbol());
        EOrder updatedOrder = new EOrder();
        updatedOrder.setStrategy(successedOrder.getStrategy());
        updatedOrder.setExchangeId(instructionId);
        updatedOrder.setInstructionKey(successedOrder.getInstructionKey());
        updatedOrder.setExchangeSymbol(successedOrder.getExchangeSymbol());
        updatedOrder.setSymbol(successedOrder.getUserSymbol());
        updatedOrder.setOrderType(successedOrder.getType());
        updatedOrder.setInitTimestamp(successedOrder.getInitTimestamp());
        updatedOrder.setLeftSymbol(successedOrder.getLeftSymbol());
        updatedOrder.setRightSymbol(successedOrder.getRightSymbol());
        updatedOrder.setSize(successedOrder.getSize());
        updatedOrder.setPrice(successedOrder.getPrice());
        //while(true) {
        //    if(!isOrderLocker()) {
        //        setOrderLocker(true);
        synchronized (wsController.orderRepository) {
            wsController.updateClientDBOrder(updatedOrder);
        }
        //        setOrderLocker(false);
        //        break;
        //    }else{trclog.log(Level.INFO,"Locked in order notify");}
        //}
        //wsController.SendInfoPointMessage(iMsg);
        List<EOrder> sendedOrder2 = wsController.getOrderByExchangeId(instructionId);
        InfoMessage iMsg= new InfoMessage("LMAX updated, order found id DATABASE"+sendedOrder2.get(0).toString());
        iMsg.setInfoType(InfoType.ORDER_MANAGEMENT);
        iMsg.setContent(sendedOrder2.get(0).toString());
        wsController.SendInfoPointMessage(iMsg);
    }

    public boolean isOrderLocker() {
        return orderLocker;
    }

    public void setOrderLocker(boolean orderLocker) {
        this.orderLocker = orderLocker;
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
        EOrder rejectedOrder = new EOrder();
        rejectedOrder.setExchangeId(instructionRejected.getInstructionId());
        rejectedOrder.setStatus(OrderStatus.REJECTED);
        wsController.insertDBRejectedFromExchange(rejectedOrder);
        trclog.log(Level.WARNING,instructionRejected.toString());
    }

    @Override
    public void notify(Order order)
    {
        classes.trading.Order messageOrder = new classes.trading.Order();

        trclog.log(Level.INFO,order.toString());
        String orderId = order.getOriginalInstructionId();
        messageOrder.setExchange(this.Exchange);
        messageOrder.setExchangeID(orderId);
        messageOrder.setExchangeSymbol(String.valueOf(order.getInstrumentId()));
        messageOrder.setFilled(Math.abs(Double.parseDouble(order.getFilledQuantity().toString())));
        messageOrder.setSize(Math.abs(Double.parseDouble(order.getQuantity().toString())));
        messageOrder.setCancelled_qty(Double.parseDouble(order.getCancelledQuantity().toString()));
        messageOrder.setLastUpdate(currentTimeMillis());
        if(Double.parseDouble(order.getQuantity().toString()) < 0){
            messageOrder.setSide(OrderSide.SELL);
        }else{
            messageOrder.setSide(OrderSide.BUY);
        }

       if(order.getOrderType() == com.lmax.api.order.OrderType.LIMIT){
           messageOrder.setPrice(Double.parseDouble(order.getLimitPrice().toString()));
       }else {
           messageOrder.setPrice(Double.parseDouble(order.getStopReferencePrice().toString()));
       }
       messageOrder.setExecuted(messageOrder.getFilled()*messageOrder.getPrice());
       /* if(order.getLimitPrice()==null){

            if(order.getStopReferencePrice() == null){
                //messageOrder.setPrice(Double.parseDouble(order.get.toString()));
            }
        }else{
            messageOrder.setPrice(Double.parseDouble(order.getLimitPrice().toString()));
        }*/

        messageOrder.setStatus(detectOrderStatus(messageOrder,false));

        EOrder exchangeOrder = new EOrder("",messageOrder.getExchangeID(),"",
                messageOrder.getPrice(),messageOrder.getSize(),messageOrder.getSide());
        exchangeOrder.setFilled(messageOrder.getFilled());
        exchangeOrder.setStatus(messageOrder.getStatus());
        exchangeOrder.setUpdateTimestamp(messageOrder.getLastUpdate());
        exchangeOrder.setInstructionKey("UNKNOWN");
        exchangeOrder.setExecuted_price(messageOrder.getPrice());
        exchangeOrder.setExecuted(messageOrder.getExecuted());

        //exchangeOrder.setExecuted(messageOrder.getExecuted());

        //while(true) {
           //if(!isOrderLocker()) {
               //setOrderLocker(true);
               synchronized (wsController.orderRepository) {
                   wsController.updateDBorderFromexchange(exchangeOrder);
               }
               //setOrderLocker(false);
          //     break;
          // }else{
               trclog.log(Level.INFO,"Locked in order notify");
          // }
       // }

        trclog.log(Level.INFO,messageOrder.toString());

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
        wsController.SendBBOPointMessage(currentBBOMessage);
    }

    @Override
    public void notify(final PositionEvent positionEvent)
    {
        trclog.log(Level.INFO,positionEvent.toString());
    }

    public void notify(final AccountStateEvent accountStateEvent)
    {
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
