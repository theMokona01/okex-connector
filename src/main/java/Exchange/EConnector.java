package Exchange;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.okcoin.commons.okex.open.api.bean.futures.result.Instruments;
import com.okcoin.commons.okex.open.api.bean.futures.result.ServerTime;
import com.okcoin.commons.okex.open.api.config.APIConfiguration;
import com.okcoin.commons.okex.open.api.service.GeneralAPIService;
import com.okcoin.commons.okex.open.api.service.futures.FuturesMarketAPIService;
import com.okcoin.commons.okex.open.api.service.futures.impl.FuturesMarketAPIServiceImpl;
import com.okcoin.commons.okex.open.api.service.futures.impl.GeneralAPIServiceImpl;
import interfaces.ExchangeConnector;
import interfaces.Instrument;
import interfaces.Order;
import interfaces.RequestResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EConnector implements ExchangeConnector {
    public TargetEventsClient Connector;
    private String Exchange;
    //Logger variables
    private static Logger trclog;
    //okex api variables
    public static APIConfiguration config = new APIConfiguration();
    private static FuturesMarketAPIService marketAPIService;

    @Override
    public void DoInitConnector() {
        System.out.println("Connector bean initialization");
    }

    @Override
    public void DestroyConnector() {
        System.out.println("Connector bean destroy");
    }

    public EConnector(String url, String apiKey, String secretKey, String passphrase, String Exchange,List<String> Instruments, RunType connectorType) {
        try {
            JSONObject Credentials = new JSONObject()
                    .put("url", url)
                    .put("apiKey", apiKey)
                    .put("secretKey", secretKey)
                    .put("passphrase", passphrase);
            this.Exchange=Exchange;
            InitConnector(Credentials,Instruments,connectorType);
            trclog = Logger.getLogger(EConnector.class.getName());
            //currentExchangeDataStrage = exchangeStorage;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public ExchangeConnector InitConnector(JSONObject Credentials,List<String> Instruments,RunType connectorType) {
        try {
            if (Credentials.has("url") && Credentials.has("apiKey") && Credentials.has("secretKey") && Credentials.has("passphrase")) {
                try {

                    //API Configuration
                    config_api_login(Credentials);
                    //Get instrument endpoint
                    Instruments = getInstruments(Credentials);
                    List<Instrument> InstrumentList = new ArrayList<>();
                    for(String instrument: Instruments){
                        OKEXInstrument InitInstrument = new OKEXInstrument();
                        InitInstrument.ExchangeSymbol=instrument;
                        InstrumentList.add(InitInstrument);
                    }
                    //Initialize Connector
                    Connector = new TargetEventsClient(InstrumentList,this.Exchange,connectorType, this.config);
                    //Start Process
                    new Thread(() -> {
                        try {
                            Connector.login(Credentials);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }).start();

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return this;
    }

    public static List<String> getInstruments(JSONObject Credentials) throws Exception{

        marketAPIService = new FuturesMarketAPIServiceImpl(config);
        List<Instruments> instruments = marketAPIService.getInstruments();
        return toList(JSON.toJSONString(instruments));
    }

    public static List<String> toList(String items) {

        List<String> items_list = new ArrayList<String>();
        Gson gson = new Gson();
        JsonArray item_json_array = gson.fromJson(items, JsonArray.class);

        for (int i = 0; i < item_json_array.size(); i++) {
            JsonObject instrument_json_object = item_json_array.get(i).getAsJsonObject();
            if (instrument_json_object.has("instrument_id")) {
                String data = instrument_json_object.get("instrument_id").toString();
                data = data.substring(1, data.length() - 1);
                items_list.add(data);
            }
        }
        return items_list;
    }

    private static void config_api_login(JSONObject Credentials) throws JSONException {
        config.setEndpoint(Credentials.getString("url"));
        config.setApiKey(Credentials.getString("apiKey"));
        config.setSecretKey(Credentials.getString("secretKey"));
        config.setPassphrase(Credentials.getString("passphrase"));

        GeneralAPIService marketAPIService = new GeneralAPIServiceImpl(config);
        ServerTime time = marketAPIService.getServerTime();
        if(time==null) throw new IllegalArgumentException();
        trclog.log(Level.INFO,"Config API Successfully Authenticated!");
    }

    @Override
    public RequestResponse SendOrder(classes.trading.Order order) {
        Connector.SendOrder(order);
        return null;
    }

    @Override
    public RequestResponse CancelOrder(classes.trading.Order order) {
        Connector.CancelOrder(order);
        return null;
    }

    @Override
    public RequestResponse CancellAllOrders() {
        return null;
    }

    @Override
    public RequestResponse GetOpenOrders() {
        return null;
    }

    @Override
    public RequestResponse GetOrderStatus(Order order) {
        return null;
    }

    @Override
    public void Destroy() {
        trclog.log(Level.INFO,"Destroy call from "+this.getClass().toString());
    }
}
