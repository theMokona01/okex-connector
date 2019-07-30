package classes.WebSocket.messages;

import classes.trading.Order;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SingleOrderMessage {
    private Order order;

    public SingleOrderMessage() { }

    public SingleOrderMessage(Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    @Override
    public String toString(){
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
