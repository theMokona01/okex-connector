package interfaces;

import Exchange.RunType;
import org.json.JSONObject;

import java.util.List;

public interface ExchangeConnector {
  //Spring
  void DoInitConnector();
  void DestroyConnector();

  //Core
  ExchangeConnector InitConnector(JSONObject Credentials, List<String> Instruments, RunType connectorType);
  RequestResponse SendOrder(classes.trading.Order order);
  RequestResponse CancelOrder(classes.trading.Order order);
  RequestResponse CancellAllOrders();
  RequestResponse GetOpenOrders();
  RequestResponse GetOrderStatus(Order order);


  void Destroy();
}
