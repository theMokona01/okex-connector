package classes.WebSocket.messages;

import classes.trading.Order;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.HashMap;

public class OrdersSnapshotMessage {
    HashMap<String, Order> OrdersSnapshot;

    public OrdersSnapshotMessage() {
        this.OrdersSnapshot = new HashMap<>();
    }

    public HashMap<String, Order> getOrdersSnapshot() {
        return OrdersSnapshot;
    }

    public void setOrdersSnapshot(HashMap<String, Order> ordersSnapshot) {
        OrdersSnapshot = ordersSnapshot;
    }

    public OrdersSnapshotMessage copy( OrdersSnapshotMessage original ) {
        OrdersSnapshotMessage copiedSnapshotMessage = new OrdersSnapshotMessage();
        copiedSnapshotMessage.setOrdersSnapshot(original.getOrdersSnapshot());
        return copiedSnapshotMessage;
    }

    @Override
    public String toString(){
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
