package interfaces;

import org.json.JSONObject;

public interface Connector {
  Connector InitConnector(JSONObject Credentials);
  RequestResponse SendOrder(Order order);
  RequestResponse CancelOrder(Order order);
  RequestResponse CancellAllOrders();
  RequestResponse GetOpenOrders();
  RequestResponse GetOrderStatus(Order order);

  Connector Destroy();
}
