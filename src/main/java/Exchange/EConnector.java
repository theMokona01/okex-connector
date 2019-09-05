package Exchange;

import com.lmax.api.LmaxApi;
import com.lmax.api.LmaxApiException;
import com.lmax.api.account.LoginRequest;
import interfaces.ExchangeConnector;
import interfaces.Instrument;
import interfaces.Order;
import interfaces.RequestResponse;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EConnector implements ExchangeConnector {
    public TargetEventsClient Connector;
    private String Exchange;
    //Logger variables
    private Logger trclog;


    @Override
    public void DoInitConnector() {
        System.out.println("Connector bean initialization");
    }

    @Override
    public void DestroyConnector() {
        System.out.println("Connector bean destroy");
    }

    public EConnector(String url, String login, String password, String Exchange, String[] Instruments, RunType connectorType) {
        try {
            JSONObject Credentials = new JSONObject()
                    .put("url", url)
                    .put("login", login)
                    .put("password", password);
            this.Exchange=Exchange;
            InitConnector(Credentials,Instruments,connectorType);
            trclog = Logger.getLogger(EConnector.class.getName());
            //currentExchangeDataStrage = exchangeStorage;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public ExchangeConnector InitConnector(JSONObject Credentials,String[] Instruments,RunType connectorType) {
        try {
            if (Credentials.has("url") && Credentials.has("login") && Credentials.has("password")) {
                try {
                    String url = Credentials.getString("url");
                    String username = Credentials.getString("login");
                    String password = Credentials.getString("password");
                    LmaxApi lmaxApiAccount = new LmaxApi(url);

                    List<Instrument> InstrumentList = new ArrayList<>();
                    for(String instrument: Instruments){
                        OKEXInstrument InitInstrument = new OKEXInstrument();
                        InitInstrument.ExchangeSymbol=instrument;
                        InstrumentList.add(InitInstrument);
                    }

                    //Enter into exchange
                    LoginRequest.ProductType productType = LoginRequest.ProductType.valueOf("CFD_DEMO");
                    Connector = new TargetEventsClient(InstrumentList,this.Exchange,connectorType);
                    //this need run in CoreThread
                    new Thread(() -> lmaxApiAccount.login(new LoginRequest(username, password, productType), Connector)).start();
                    //lmaxApiAccount.login(new LoginRequest(username, password, productType), Connector);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }catch (LmaxApiException e){
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public RequestResponse SendOrder(classes.trading.Order order) {
        Connector.SendOrder(order);
        return null;
    }

    @Override
    public RequestResponse CancelOrder(Order order) {
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
