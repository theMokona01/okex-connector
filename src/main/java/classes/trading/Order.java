package classes.trading;

import classes.Enums.CustomOrderParams;
import classes.Enums.OrderSide;
import classes.Enums.OrderStatus;
import classes.Enums.OrderType;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Order {
    private String InstructionKey;
    private String Strategy;
    private String Exchange;
    private String UserSymbol;
    private String Symbol;
    private String LeftSymbol;
    private String RightSymbol;
    private String ID;
    private String ExchangeID;
    private OrderSide Side;
    private OrderType Type;
    private OrderStatus Status;
    private CustomOrderParams Params;
    private Double Price;
    private Double Size;
    private String CustomJsonData;
    private long InitTimestamp;
    private long LastUpdate;


    private Double filled=0.0;
    private Double executed=0.0;
    private Double cancelled_qty=0.0;
    private List<Execution> Executions= new ArrayList<Execution>();



    public Order(){}

    public Order(String instructionKey, String strategy, String exchange,String userSymbol, String symbol,  String leftSymbol, String rightSymbol, String id, String exchangeID, OrderSide side, OrderType type, OrderStatus status, CustomOrderParams params, Double price, Double size, String customJsonData, long initTimestamp, long lastUpdate) {
        InstructionKey = instructionKey;
        Strategy = strategy;
        Exchange = exchange;
        Symbol = symbol;
        UserSymbol = userSymbol;
        LeftSymbol = leftSymbol;
        RightSymbol = rightSymbol;
        ID = id;
        ExchangeID = exchangeID;
        Side = side;
        Type = type;
        Status = status;
        Params = params;
        Price = price;
        Size = size;
        CustomJsonData = customJsonData;
        InitTimestamp = initTimestamp;
        LastUpdate = lastUpdate;
    }

    public String getInstructionKey() {
        return InstructionKey;
    }

    public void setInstructionKey(String instructionKey) {
        InstructionKey = instructionKey;
    }

    public String getStrategy() {
        return Strategy;
    }

    public void setStrategy(String strategy) {
        Strategy = strategy;
    }

    public String getExchange() {
        return Exchange;
    }

    public void setExchange(String exchange) {
        Exchange = exchange;
    }

    public String getUserSymbol() {
        return UserSymbol;
    }

    public void setUserSymbol(String userSymbol) {
        UserSymbol = userSymbol;
    }

    public String getSymbol() {
        return Symbol;
    }

    public void setSymbol(String symbol) {
        Symbol = symbol;
    }

    public String getLeftSymbol() {
        return LeftSymbol;
    }

    public void setLeftSymbol(String leftSymbol) {
        LeftSymbol = leftSymbol;
    }

    public String getRightSymbol() {
        return RightSymbol;
    }

    public void setRightSymbol(String rightSymbol) {
        RightSymbol = rightSymbol;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getExchangeID() {
        return ExchangeID;
    }

    public void setExchangeID(String exchangeID) {
        ExchangeID = exchangeID;
    }

    public OrderSide getSide() {
        return Side;
    }

    public void setSide(OrderSide side) {
        Side = side;
    }

    public OrderType getType() {
        return Type;
    }

    public void setType(OrderType type) {
        Type = type;
    }

    public OrderStatus getStatus() {
        return Status;
    }

    public void setStatus(OrderStatus status) {
        Status = status;
    }

    public CustomOrderParams getParams() {
        return Params;
    }

    public void setParams(CustomOrderParams params) {
        Params = params;
    }

    public Double getPrice() {
        return Price;
    }

    public void setPrice(Double price) {
        Price = price;
    }

    public Double getSize() {
        return Size;
    }

    public void setSize(Double size) {
        Size = size;
    }

    public String getCustomData() {
        return CustomJsonData;
    }

    public void setCustomData(String customJsonData) {
        CustomJsonData = customJsonData;
    }

    public long getInitTimestamp() {
        return InitTimestamp;
    }

    public void setInitTimestamp(long initTimestamp) {
        InitTimestamp = initTimestamp;
    }

    public long getLastUpdate() {
        return LastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        LastUpdate = lastUpdate;
    }

    public Double getFilled() {
        return filled;
    }

    public void setFilled(Double filled) {
        this.filled = filled;
    }

    public Double getExecuted() {
        return executed;
    }

    public void setExecuted(Double executed) {
        this.executed = executed;
    }

    public List<Execution> getExecutions() {
        return Executions;
    }

    public void addExecution(Execution execution){
        this.Executions.add(execution);
    }

    public void setExecutions(List<Execution> executions) {
        Executions = executions;
    }

    public Double getCancelled_qty() {
        return cancelled_qty;
    }

    public void setCancelled_qty(Double cancelled_qty) {
        this.cancelled_qty = cancelled_qty;
    }

    @Override
    public String toString(){
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}

