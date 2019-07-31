package classes.trading;

import interfaces.ConnectorStorage;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.HashMap;
import java.util.Map;

public class ExchangeStorage implements ConnectorStorage {
    private String ExchangeName;
    private String Account;
    //Storage for order relations
    private HashMap<String,String> OrderRelations;
    //Storage for working orders - placed, part_filled
    private HashMap<String,Order> WorkingOrders;
    //Storage for cancelled orders
    private HashMap<String,Order> CancelledOrders;
    //Storage for rejected orders
    private HashMap<String,Order> RejectedOrders;
    //Storage for filled orders
    private HashMap<String,Order> FilledOrders;
    //Temp storage for unknown orders
    private HashMap<String,Order> TempOrders;
    //Storage for strategy positions
    private HashMap<String,StrategyPosition> StrategiesPositions;


    public ExchangeStorage(String exchangeName, String account) {
        ExchangeName = exchangeName;
        Account = account;
        InitStorage();
    }

    @Override
    public void InitStorage() {
        WorkingOrders = new HashMap<>();
        CancelledOrders = new HashMap<>();
        RejectedOrders = new HashMap<>();
        FilledOrders = new HashMap<>();
        TempOrders = new HashMap<>();
        StrategiesPositions = new HashMap<>();
        OrderRelations = new HashMap<>();
    }

    public String getExchangeName() {
        return ExchangeName;
    }

    public void setExchangeName(String exchangeName) {
        ExchangeName = exchangeName;
    }

    public String getAccount() {
        return Account;
    }

    public void setAccount(String account) {
        Account = account;
    }

    public HashMap<String, String> getOrderRelations() {
        return OrderRelations;
    }

    public void setOrderRelations(HashMap<String, String> orderRelations) {
        OrderRelations = orderRelations;
    }

    public HashMap<String, Order> getWorkingOrders() {
        return WorkingOrders;
    }

    public void setWorkingOrders(HashMap<String, Order> workingOrders) {
        WorkingOrders = workingOrders;
    }

    public HashMap<String, Order> getCancelledOrders() {
        return CancelledOrders;
    }

    public void setCancelledOrders(HashMap<String, Order> cancelledOrders) {
        CancelledOrders = cancelledOrders;
    }

    public HashMap<String, Order> getRejectedOrders() {
        return RejectedOrders;
    }

    public void setRejectedOrders(HashMap<String, Order> rejectedOrders) {
        RejectedOrders = rejectedOrders;
    }

    public HashMap<String, Order> getFilledOrders() {
        return FilledOrders;
    }

    public void setFilledOrders(HashMap<String, Order> filledOrders) {
        FilledOrders = filledOrders;
    }

    public HashMap<String, Order> getTempOrders() {
        return TempOrders;
    }

    public void setTempOrders(HashMap<String, Order> tempOrders) {
        TempOrders = tempOrders;
    }

    public HashMap<String, StrategyPosition> getStrategiesPositions() {
        return StrategiesPositions;
    }

    public void setStrategiesPositions(HashMap<String, StrategyPosition> strategiesPositions) {
        StrategiesPositions = strategiesPositions;
    }

    public void updateWorkingOrder(Order order){

    }

    @Override
    public void Destroy() {

    }

    @Override
    public String toString(){
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
