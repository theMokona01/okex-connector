package classes.trading;

import classes.Enums.CustomOrderParams;
import classes.Enums.OrderSide;
import classes.Enums.OrderStatus;
import classes.Enums.OrderType;
import netscape.javascript.JSObject;

public class Order {
    private String Exchange;
    private String Symbol;
    private String ID;
    private String ExchangeID;
    private OrderSide Side;
    private OrderType Type;
    private OrderStatus Status;
    private CustomOrderParams Params;
    private Double Price;
    private Double Size;
    private JSObject CustomData;
    private long InitTimestamp;
    private long LastUpdate;

    public Order(String exchange, String symbol, String ID, String exchangeID, OrderSide side, OrderType type, OrderStatus status, CustomOrderParams params, Double price, Double size, JSObject customData, long initTimestamp, long lastUpdate) {
        Exchange = exchange;
        Symbol = symbol;
        this.ID = ID;
        ExchangeID = exchangeID;
        Side = side;
        Type = type;
        Status = status;
        Params = params;
        Price = price;
        Size = size;
        CustomData = customData;
        InitTimestamp = initTimestamp;
        LastUpdate = lastUpdate;
    }

    public String getExchange() {
        return Exchange;
    }

    public void setExchange(String exchange) {
        Exchange = exchange;
    }

    public String getSymbol() {
        return Symbol;
    }

    public void setSymbol(String symbol) {
        Symbol = symbol;
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

    public JSObject getCustomData() {
        return CustomData;
    }

    public void setCustomData(JSObject customData) {
        CustomData = customData;
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
}
