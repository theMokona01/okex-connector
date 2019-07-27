package Exchange;

import classes.WebSocket.ServerWSController;
import classes.WebSocket.messages.BBOMessage;
import classes.WebSocket.messages.BalanceMessage;
import classes.WebSocket.messages.MessageEndPoint;
import com.google.gson.Gson;
import com.lmax.api.*;
import com.lmax.api.account.*;
import com.lmax.api.order.Execution;
import com.lmax.api.order.ExecutionEventListener;
import com.lmax.api.order.Order;
import com.lmax.api.order.OrderEventListener;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PFTLMAXEventsClient implements LoginCallback, AccountStateEventListener, OrderBookEventListener,
        StreamFailureListener, OrderEventListener, InstructionRejectedEventListener, ExecutionEventListener, SessionDisconnectedListener,
        PositionEventListener {
    private String Exchange;
    //Lmax variables
    private List<Instrument> InstrumentList;
    private Session currentSession;
    //public HashMap<String, HashMap<String, JSONObject>> OrdersStateMap = new HashMap<String, HashMap<String, JSONObject>>();

    //WSController variables
    private MessageEndPoint endpoints = new MessageEndPoint();
    private ServerWSController wsController = null;

    //Working messages variables
    private BBOMessage currentBBOMessage= new BBOMessage();
    private BalanceMessage currentBalanceMessage;// = new BalanceMessage(Exchange);


    //Logger variables
    private Logger trclog = Logger.getLogger(PFTLMAXEventsClient.class.getName());

    public PFTLMAXEventsClient(List<Instrument> InstrumentList,String Exchange){
        this.Exchange = Exchange;
        this.InstrumentList=InstrumentList;
        this.currentBalanceMessage = new BalanceMessage(this.Exchange);
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
                    //while (wait_prices)
                    //{
                    //   Sending something scheduled
                    //}
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

    }

    @Override
    public void notify(InstructionRejectedEvent instructionRejected)
    {

    }

    @Override
    public void notify(Order order)
    {

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
        trclog.log(Level.INFO,orderBookEvent.toString());
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

    }

    public void notify(final AccountStateEvent accountStateEvent)
    {
        trclog.log(Level.INFO,accountStateEvent.toString());
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
