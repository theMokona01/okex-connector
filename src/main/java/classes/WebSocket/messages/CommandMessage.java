package classes.WebSocket.messages;

import classes.Enums.Commands;
import classes.Enums.OrderCommand;
import classes.trading.Order;
import org.apache.commons.lang3.builder.ToStringStyle;

public class CommandMessage {
    private Commands CommandType;
    private Order OrderObject;
    private OrderCommand OrderCommandObject;

    public CommandMessage(Commands command) {
        this.CommandType = command;
    }

    public Commands getCommand() {
        return CommandType;
    }

    public void setCommand(Commands command) {
        this.CommandType = command;
    }

    @Override
    public String toString(){
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
