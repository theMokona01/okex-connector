package external;

import com.lmax.api.*;
import com.lmax.api.account.AccountStateEvent;
import com.lmax.api.account.AccountStateEventListener;
import com.lmax.api.account.AccountSubscriptionRequest;
import com.lmax.api.account.LoginCallback;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class PFTLMAXEventsClient implements LoginCallback, AccountStateEventListener, OrderBookEventListener,
        StreamFailureListener, OrderEventListener, InstructionRejectedEventListener, ExecutionEventListener, SessionDisconnectedListener,
        PositionEventListener {
    private String Exchange;
    private static final long INSTRUMENT_ID_1 = 4006; //EURJPY   //4011; //EURCHF
    private static final long INSTRUMENT_ID_2 = 4001; //EURUSD
    private static final long INSTRUMENT_ID_3 = 4004; //USDJPY
    private List<Instrument> InstrumentList;
    private Session currentSession;
    public HashMap<String, HashMap<String, JSONObject>> OrdersStateMap = new HashMap<String, HashMap<String, JSONObject>>();


    public PFTLMAXEventsClient(List<Instrument> InstrumentList){
        this.Exchange = Exchange;
        this.InstrumentList=InstrumentList;
    }

    public Session getLmaxSession(){
        return this.currentSession;
    }

    @Override
    public void onLoginSuccess(final Session session)
    {
        System.out.println("Logged in, subscribing");
        System.out.println("My accountId is: " + session.getAccountDetails().getAccountId()+" "+session.getAccountDetails().toString());
        session.registerAccountStateEventListener(this);
        session.registerOrderBookEventListener(this);
        session.registerStreamFailureListener(this);
        session.registerOrderEventListener(this);
        session.registerInstructionRejectedEventListener(this);
        session.registerExecutionEventListener(this);

        session.registerPositionEventListener(this);

        subscribe(session, new PositionSubscriptionRequest(), "Positions");
        subscribe(session, new AccountSubscriptionRequest(), "Account Updates");

        for(Instrument instrument : this.InstrumentList) {
            subscribeToInstrument(session, Long.parseLong(instrument.ExchangeSymbol));
        }
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    boolean wait_prices = true;
                    System.out.println("Sending snapshot");
                    //while (wait_prices)
                    //{
                    //    SendToClients("ordersnapshot",OrdersStateMap.toString());
                        Thread.sleep(2000);
                    //}
                }
                catch (Exception e)
                {
                    System.out.println("E1");
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
        /*System.out.println("notifySessionDisconnected");
        Runnable LmaxRunner = new PfLmax("Profluent", "Il&09IsHA!KbbeR%", "https://trade.lmaxtrader.com/", "LMAX",InstrumentList); // or an anonymous class, or lambda...
        Thread LmaxThread = new Thread(LmaxRunner);
        LmaxThread.start();*/
    }


    @Override
    public void notifyStreamFailure(Exception e)
    {
        System.out.println("Error occured on the stream");
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
        System.out.println("Login Failed: " + failureResponse);
    }

    public void notify(final OrderBookEvent orderBookEvent)
    {
        double Ask = 0;
        double Bid = 0;
        List<PricePoint> Asks=orderBookEvent.getAskPrices();
        List<PricePoint> Bids=orderBookEvent.getBidPrices();
        if(Asks.size()>0){
            Ask = Double.parseDouble(Asks.get(0).getPrice().toString());
        }
        if(Bids.size()>0){
            Bid = Double.parseDouble(Bids.get(0).getPrice().toString());
        }
        long instrument_id = orderBookEvent.getInstrumentId();
        String message = String.valueOf(instrument_id)+", ask: "+String.valueOf(Ask)+", bid: "+String.valueOf(Bid);

    }

    @Override
    public void notify(final PositionEvent positionEvent)
    {

    }

    public void notify(final AccountStateEvent accountStateEvent)
    {


    }

    private void subscribe(final Session session, final SubscriptionRequest request, final String subscriptionDescription)
    {
        session.subscribe(request, new Callback()
        {
            public void onSuccess()
            {
                System.out.println("Subscribed to " + subscriptionDescription);
            }

            public void onFailure(final FailureResponse failureResponse)
            {
                System.err.printf("Failed to subscribe to " + subscriptionDescription + ": %s%n", failureResponse);
            }
        });
    }


    private void subscribeToInstrument(final Session session, final long instrumentId)
    {
        session.subscribe(new OrderBookSubscriptionRequest(instrumentId), new Callback()
        {
            public void onSuccess()
            {
                System.out.printf("Subscribed to instrument %d.%n", instrumentId);
            }

            public void onFailure(final FailureResponse failureResponse)
            {
                System.err.printf("Failed to subscribe to instrument %d: %s%n", instrumentId, failureResponse);
            }
        });
    }

}
