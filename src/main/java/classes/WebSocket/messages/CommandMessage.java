package classes.WebSocket.messages;

import classes.Enums.Commands;
import classes.Enums.OrderCommand;
import classes.trading.Order;
import com.google.gson.Gson;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.json.JSONObject;

public class CommandMessage {
    private Commands command;

    //Order commands serialization/deserialization variables
    private String OrderString;
    private Order order;

    public CommandMessage(){
        this.order = new Order();
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Commands getCommand() {
        return command;
    }

    public void setCommand(Commands command) {
        this.command = command;
    }

    public String toGsonSerialize(){
        Gson gson = new Gson();
        this.OrderString = gson.toJson(this.order);
        this.order=null;
        String jsonInString = gson.toJson(this);
        return jsonInString;
    }

    public void DeserializeOrder(){
        Gson gson = new Gson();
        order = gson.fromJson(this.OrderString,Order.class);
    }

    @Override
    public String toString(){
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}

