package Exchange;

import Exchange.config.WebSocketClient;
import Exchange.config.WebSocketConfig;
import classes.Enums.*;
import classes.Enums.OrderType;
import classes.WebSocket.ServerWSController;
import classes.WebSocket.messages.*;
import classes.WebSocket.model.EExecution;
import classes.WebSocket.model.EOrder;
import classes.trading.Execution;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.okcoin.commons.okex.open.api.bean.futures.param.Order;
import com.okcoin.commons.okex.open.api.bean.futures.result.Currencies;
import com.okcoin.commons.okex.open.api.bean.futures.result.Instruments;
import com.okcoin.commons.okex.open.api.bean.futures.result.OrderResult;
import com.okcoin.commons.okex.open.api.config.APIConfiguration;
import com.okcoin.commons.okex.open.api.enums.FuturesTransactionTypeEnum;
import com.okcoin.commons.okex.open.api.service.futures.FuturesMarketAPIService;
import com.okcoin.commons.okex.open.api.service.futures.FuturesTradeAPIService;
import com.okcoin.commons.okex.open.api.service.futures.impl.FuturesMarketAPIServiceImpl;
import com.okcoin.commons.okex.open.api.service.futures.impl.FuturesTradeAPIServiceImpl;
import com.okcoin.commons.okex.open.api.utils.OrderIdUtils;
import interfaces.Instrument;
import interfaces.RequestResponse;
import okhttp3.WebSocket;
import okio.ByteString;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.currentTimeMillis;


public class TargetEventsClient {
    private String Exchange;

    //Okex variables
    private List<Instrument> InstrumentList;
    private static APIConfiguration config = new APIConfiguration();
    private static FuturesMarketAPIService marketAPIService;
    private static final WebSocketClient webSocketClient = new WebSocketClient();

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
    private static Logger trclog = Logger.getLogger(TargetEventsClient.class.getName());

    //Locker for update orders on start
    boolean orderLocker = false;

    //Connector run type
    private static RunType ConnectorType;

    public TargetEventsClient(List<Instrument> InstrumentList, String Exchange, RunType ConnectorType, APIConfiguration config){
        this.Exchange = Exchange;
        this.InstrumentList=InstrumentList;
        this.currentBalanceMessage = new BalanceMessage(this.Exchange);
        this.currentSingleOrderMessage = new SingleOrderMessage();
        this.ConnectorType=ConnectorType;
        config = config;

    }

    public void setWsController(ServerWSController wsController) {
        this.wsController = wsController;
        //currentExchangeStorage.setWsController(wsController);
    }


   public void login(JSONObject Credentials) throws JSONException {
       String url = Credentials.getString("url");
       String apiKey = Credentials.getString("apiKey");
       String passphrase = Credentials.getString("passphrase");
       String secretKey = Credentials.getString("secretKey");

       WebSocketConfig.loginConnect(webSocketClient, url, apiKey, passphrase, secretKey);
       try {
           Thread.sleep(10000);
           if (webSocketClient.getIsLogin()) {

               if(ConnectorType == RunType.FULLTRADE || ConnectorType==RunType.ALL) {
                   privateStream();
               }
               if(ConnectorType==RunType.ALL || ConnectorType==RunType.FULLMARKET) {
                   publicStream();
               }
               publicStream();

           }
           else{
               trclog.log(Level.WARNING,"Error while authenticating in.");
               return;
           }

       } catch (InterruptedException e) {
           e.printStackTrace();
       } catch (Exception e) {
           e.printStackTrace();
       }

   }

    public static List<String> toList(String items, String flag) {
        List<String> items_list = new ArrayList<String>();
        Gson gson = new Gson();
        JsonArray item_json_array = gson.fromJson(items, JsonArray.class);

        for (int i = 0; i < item_json_array.size(); i++) {
            JsonObject instrument_json_object = item_json_array.get(i).getAsJsonObject();
            if (instrument_json_object.has(flag)) {
                String data = instrument_json_object.get(flag).toString();
                data = data.substring(1, data.length() - 1);
                items_list.add(data);
            }
        }
        return items_list;
    }

    public static List<String> getSymbolEndpoint(String instrument){
        marketAPIService = new FuturesMarketAPIServiceImpl(config);
        List<Instruments> instruments = null;
        List<Currencies> currencies = null;
        List<String> resultList = new ArrayList<String>();

        switch(instrument){
            case "instrument":
                instruments = marketAPIService.getInstruments();
                resultList = toList(JSON.toJSONString(instruments), "instrument_id");
                break;
            case "currency":
                currencies = marketAPIService.getCurrencies();
                resultList = toList(JSON.toJSONString(currencies), "name");
                break;
            default:
                trclog.log(Level.WARNING,"Symbol endpoint could'nt be specified.");
        }
        return resultList;
    }

    public static void privateStream(){
        try{
            String streamType[] = {"futures/position", "futures/order", "futures/account"};

            List<String> instruments = getSymbolEndpoint("instrument");
            List<String> order_position_subscription = new ArrayList<String>();
            for(String instrument: instruments){
                order_position_subscription.add(streamType[0] + ":" + instrument);
                order_position_subscription.add(streamType[1] + ":" + instrument);
            }

            List<String> currencies = getSymbolEndpoint("currency");
            List<String> account_subscription = new ArrayList<String>();
            for(String currency: currencies){
                account_subscription.add(streamType[2] + ":" + currency);
            }
            trclog.log(Level.INFO,"Subscribing to private stream.");
            webSocketClient.subscribe(order_position_subscription);
            try {
                Thread.sleep(10000000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            webSocketClient.subscribe(account_subscription);
            try {
                Thread.sleep(10000000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }catch(Exception e){
            trclog.log(Level.WARNING,"Private stream subscription error.");
        }
    }

    public static void publicStream() {
        try {
            String streamType = "futures/ticker";
            List<String> instruments = getSymbolEndpoint("instrument");
            List<String> ticker_subscription = new ArrayList<String>();
            for (String instrument : instruments) {
                ticker_subscription.add(streamType + ":" + instrument);
            }
            trclog.log(Level.INFO, "Subscribing to public stream.");
            webSocketClient.subscribe(ticker_subscription);
            try {
                Thread.sleep(10000000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        } catch (Exception e) {
            trclog.log(Level.WARNING, "Public stream subscription error.");
        }
    }

    public void CancelOrder(classes.trading.Order order){
        FuturesTradeAPIService tradeAPIService = new FuturesTradeAPIServiceImpl(config);
        tradeAPIService.cancelOrder( order.getInstructionKey(), Long.parseLong( order.getExchangeID()));
        trclog.log(Level.INFO, "Cancelled order");
    }

    public String getOrderResult(String order) {
        Gson gson = new Gson();
        JsonArray item_json_array = gson.fromJson(order, JsonArray.class);
        for (int i = 0; i < item_json_array.size(); i++) {
            JsonObject instrument_json_object = item_json_array.get(i).getAsJsonObject();
            if (instrument_json_object.has("order_id")) {
                String data = instrument_json_object.get("order_id").toString();
                data = data.substring(1, data.length() - 1);
                return data;

            }
        }
        return null;
    }

    public void SendOrder(classes.trading.Order limitOrder) {
        //Convert order params in current_exchange format
        Double limitPrice = Double.valueOf(limitOrder.getPrice().toString());
        double lSize = Math.abs(limitOrder.getSize());
        if (limitOrder.getSide() == OrderSide.SELL) {
            lSize = lSize * (-1);
        }
        try {
            Double limitSize = Double.valueOf(String.valueOf(lSize));
            FuturesTradeAPIService tradeAPIService = new FuturesTradeAPIServiceImpl(config);
            Order order = new Order();
            order.setClient_oid(OrderIdUtils.generator());
            order.setType(FuturesTransactionTypeEnum.OPEN_SHORT.code());
            order.setinstrument_id("BTC-USD-190906");
            order.setSize(1);
            order.setMatch_price(0);
            order.setLeverage(20D);
            order.setPrice(10000D);
            OrderResult Orderresult = tradeAPIService.order(order);
            String instructionId = getOrderResult(JSON.toJSONString(Orderresult));

            try {
                JSONObject successedId = new JSONObject();
                successedId.put("instructionKey", limitOrder.getInstructionKey());
                successedId.put("exchangeId", instructionId);
                InfoMessage response = new InfoMessage("{" + limitOrder.getInstructionKey() + ":" +
                        instructionId + "}");
                response.setContent(successedId.toString());
                response.setInfoType(InfoType.ORDER_MANAGEMENT);
                wsController.SendInfoPointMessage(response);
            } catch (Exception e) {
                e.printStackTrace();
            }

            while (true) {
                List<EOrder> sendedOrder = wsController.getOrderByExchangeId(instructionId);
                trclog.log(Level.INFO, "Order check " + sendedOrder.toString());
                limitOrder.setExchangeID(instructionId);
                if (sendedOrder.size() > 0) {
                    updateAfterSuccess(limitOrder, sendedOrder.get(0), instructionId);
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    trclog.log(Level.WARNING, e.toString());
                }
            }
        } catch (Exception e) {
            trclog.log(Level.WARNING, "Error while processing order");
            wsController.SendInfoPointMessage(new InfoMessage("{\"error\":\"" + e.toString() + "\"}"));
        }
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


    public void notify(Execution execution)
    {
        trclog.log(Level.INFO,execution.toString());
        currentExecution.setId(String.valueOf(execution.getId()));
        currentExecution.setOrderId(execution.getId());
        if(execution.getPrice()==null) {
            currentExecution.setPrice(0.0);//Double.parseDouble(String.valueOf(execution.getPrice().longValue())));
        }else{
            currentExecution.setPrice(Double.parseDouble(execution.getPrice().toString()));
        }
            currentExecution.setFilled(Double.parseDouble(execution.getFilled().toString()));
        if(Double.parseDouble(execution.getFilled().toString()) != 0) {
            currentExecution.setExecuted(Double.parseDouble(execution.getPrice().toString())*Double.parseDouble(execution.getFilled().toString()));
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

    public void notify(long l, String s) {
        trclog.log(Level.INFO,s);
    }

    public void notify(classes.trading.Order order)
    {
        classes.trading.Order messageOrder = new classes.trading.Order();

        trclog.log(Level.INFO,order.toString());
        String orderId = order.getExchangeID();
        messageOrder.setExchange(this.Exchange);
        messageOrder.setExchangeID(orderId);
        messageOrder.setExchangeSymbol(String.valueOf(order.getExchangeSymbol()));
        messageOrder.setFilled(Math.abs(Double.parseDouble(order.getFilled().toString())));
        messageOrder.setSize(Math.abs(Double.parseDouble(order.getSize().toString())));
        messageOrder.setCancelled_qty(Double.parseDouble(order.getCancelled_qty().toString()));
        messageOrder.setLastUpdate(currentTimeMillis());
        if(Double.parseDouble(order.getSize().toString()) < 0){
            messageOrder.setSide(OrderSide.SELL);
        }else{
            messageOrder.setSide(OrderSide.BUY);
        }

        messageOrder.setPrice(Double.parseDouble(order.getPrice().toString()));
        messageOrder.setExecuted(messageOrder.getFilled()*messageOrder.getPrice());
        messageOrder.setStatus(detectOrderStatus(messageOrder,false));
        EOrder exchangeOrder = new EOrder("",messageOrder.getExchangeID(),"",
                messageOrder.getPrice(),messageOrder.getSize(),messageOrder.getSide());
        exchangeOrder.setFilled(messageOrder.getFilled());
        exchangeOrder.setStatus(messageOrder.getStatus());
        exchangeOrder.setUpdateTimestamp(messageOrder.getLastUpdate());
        exchangeOrder.setInstructionKey("UNKNOWN");
        exchangeOrder.setExecuted_price(messageOrder.getPrice());
        exchangeOrder.setExecuted(messageOrder.getExecuted());

        synchronized (wsController.orderRepository) {
            wsController.updateDBorderFromexchange(exchangeOrder);
        }

        trclog.log(Level.INFO,"Locked in order notify");

        trclog.log(Level.INFO,messageOrder.toString());

        //Sending order to client
        this.currentSingleOrderMessage.setOrder(messageOrder);
        wsController.SendSingleOrderPointMessage(this.currentSingleOrderMessage);

    }


//    @Override
//    public void notify()
//    {
//        double Ask = 0;
//        double Bid = 0;
//        double Ask_Size = 0;
//        double Bid_Size = 0;
//        String instrument_id = "";
//
//        currentBBOMessage.setAsk(Ask);
//        currentBBOMessage.setBid(Bid);
//        currentBBOMessage.setAsk_size(Ask_Size);
//        currentBBOMessage.setBid_size(Bid_Size);
//        currentBBOMessage.setInstrument(String.valueOf(instrument_id));
//        currentBBOMessage.setTimestamp(Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())));
//        wsController.SendBBOPointMessage(currentBBOMessage);
//    }

    private OrderStatus detectOrderStatus(classes.trading.Order okexOrderInstruction,boolean isRejected){
        OrderStatus orderStatus = OrderStatus.UNKNOWN;
        //if(Double.parseDouble(okexOrderInstruction.getCancelledQuantity().toString()) > 0)
        if(Math.abs(okexOrderInstruction.getCancelled_qty()) > 0)
        {
            orderStatus=OrderStatus.CANCELLED;
        }else if(okexOrderInstruction.getFilled().equals(okexOrderInstruction.getSize())){//okexOrderInstruction.getQuantity().longValue() == okexOrderInstruction.getFilledQuantity().longValue()){
            orderStatus=OrderStatus.FULL_FILLED;
        }else if(okexOrderInstruction.getFilled()>0){ //(Double.parseDouble(okexOrderInstruction.getFilledQuantity().toString()) > 0){
            orderStatus = OrderStatus.PART_FILLED;
        } else if(okexOrderInstruction.getFilled()==0){//.getFilledQuantity().longValue() == 0){
            orderStatus=OrderStatus.ACCEPTED;
        }
        if(isRejected){
            orderStatus=OrderStatus.REJECTED;
        }
        return orderStatus;
    }

}
