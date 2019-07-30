package interfaces;

import org.json.JSONObject;

public interface ExchangeConnector {
  //Spring
  void DoInitConnector();
  void DestroyConnector();

  //Core
  ExchangeConnector InitConnector(JSONObject Credentials,String[] Instruments);
  RequestResponse SendOrder(Order order);
  RequestResponse CancelOrder(Order order);
  RequestResponse CancellAllOrders();
  RequestResponse GetOpenOrders();
  RequestResponse GetOrderStatus(Order order);


  void Destroy();
}
