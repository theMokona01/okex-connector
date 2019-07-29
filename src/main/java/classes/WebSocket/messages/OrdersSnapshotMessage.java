package classes.WebSocket.messages;

import classes.trading.Order;

import java.util.HashMap;

public class OrdersSnapshotMessage {
    HashMap<String, Order> OrdersSnapshot;
    public OrdersSnapshotMessage() {
        this.OrdersSnapshot = new HashMap<>();
    }
}
