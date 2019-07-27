package classes.WebSocket.messages;

import classes.Enums.Commands;
import classes.Enums.OrderCommand;
import classes.trading.Order;
import org.apache.commons.lang3.builder.ToStringStyle;

public class CommandMessage {
    private String command;
    //private String command2;


    public CommandMessage(String command) {
        this.command = command;
        //this.command2 = command2;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString(){
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}

/*
public class CommandMessage {

    private String TEST;
    private String TEST2;
    private Commands CommandType;
    private Order OrderObject;
    private OrderCommand OrderCommandObject;

    //public CommandMessage() {}

    public CommandMessage(String TEST) {
        this.TEST = TEST;
        this.TEST2 = "TXT23";
    }

    public String getTEST() {
        return TEST;
    }
    /*public CommandMessage(Commands command) {
        this.CommandType = command;
    }

    public CommandMessage(Commands commandType, Order orderObject, OrderCommand orderCommandObject) {
        CommandType = commandType;
        OrderObject = orderObject;
        OrderCommandObject = orderCommandObject;
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
}*/
