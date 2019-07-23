package classes;

import com.lmax.api.LmaxApi;
import com.lmax.api.LmaxApiException;
import com.lmax.api.account.LoginRequest;
import external.PFTLMAXEventsClient;
import interfaces.ExchangeConnector;
import interfaces.Instrument;
import interfaces.Order;
import interfaces.RequestResponse;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LMAXConnector implements ExchangeConnector {
    private PFTLMAXEventsClient Connector;

    @Override
    public void DoInitConnector() {
        System.out.println("Connector bean initialization");
    }

    @Override
    public void DestroyConnector() {
        System.out.println("Connector bean destroy");
    }

    public LMAXConnector(String url, String login, String password) {
        try {
            JSONObject Credentials = new JSONObject()
                    .put("url", url)
                    .put("login", login)
                    .put("password", password);
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
                    InitInstrument.ExchangeSymbol="4001";
                    List<Instrument> InstrumentList = new ArrayList<>();
                    InstrumentList.add(InitInstrument);

                    //Enter into exchange
                    LoginRequest.ProductType productType = LoginRequest.ProductType.valueOf("CFD_DEMO");
                    Connector = new PFTLMAXEventsClient(InstrumentList);
                    lmaxApiAccount.login(new LoginRequest(username, password, productType), Connector);

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
