package Exchange;

import classes.WebSocket.ServerWSController;
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

public class LMAXConnector implements ExchangeConnector {
    public PFTLMAXEventsClient Connector;
    private String Exchange;

    @Override
    public void DoInitConnector() {
        System.out.println("Connector bean initialization");
    }

    @Override
    public void DestroyConnector() {
        System.out.println("Connector bean destroy");
    }

    public LMAXConnector(String url, String login, String password, String Exchange) {
        try {
            JSONObject Credentials = new JSONObject()
                    .put("url", url)
                    .put("login", login)
                    .put("password", password);
            this.Exchange=Exchange;
            InitConnector(Credentials);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public ExchangeConnector InitConnector(JSONObject Credentials) {
        try {
            if (Credentials.has("url") && Credentials.has("login") && Credentials.has("password")) {
                try {
                    String url = Credentials.getString("url");
                    String username = Credentials.getString("login");
                    String password = Credentials.getString("password");
                    LmaxApi lmaxApiAccount = new LmaxApi(url);

                    //Initializaion of instruments - need in BEANs
                    LMAXInstrument InitInstrument = new LMAXInstrument();
                    InitInstrument.ExchangeSymbol="5002";
                    LMAXInstrument InitInstrument2 = new LMAXInstrument();
                    InitInstrument2.ExchangeSymbol="5003";
                    LMAXInstrument InitInstrument3 = new LMAXInstrument();
                    InitInstrument3.ExchangeSymbol="5004";
                    LMAXInstrument InitInstrument4 = new LMAXInstrument();
                    InitInstrument4.ExchangeSymbol="5005";
                    LMAXInstrument InitInstrument5 = new LMAXInstrument();
                    InitInstrument5.ExchangeSymbol="5006";
                    LMAXInstrument InitInstrument6 = new LMAXInstrument();
                    InitInstrument6.ExchangeSymbol="5013";

                    List<Instrument> InstrumentList = new ArrayList<>();
                    InstrumentList.add(InitInstrument);
                    InstrumentList.add(InitInstrument2);
                    InstrumentList.add(InitInstrument3);
                    InstrumentList.add(InitInstrument4);
                    InstrumentList.add(InitInstrument5);
                    InstrumentList.add(InitInstrument6);

                    //Enter into exchange
                    LoginRequest.ProductType productType = LoginRequest.ProductType.valueOf("CFD_DEMO");
                    Connector = new PFTLMAXEventsClient(InstrumentList,this.Exchange);
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
    public RequestResponse SendOrder(Order order) {
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

    }
}
