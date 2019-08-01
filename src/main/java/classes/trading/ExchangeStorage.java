package classes.trading;

import Exchange.PFTLMAXEventsClient;
import classes.Enums.OrderSnapShotType;
import classes.Enums.OrderStatus;
import classes.WebSocket.ServerWSController;
import classes.WebSocket.messages.OrdersSnapshotMessage;
import interfaces.ConnectorStorage;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.currentTimeMillis;

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
    //Temp storage for accepted orders, which wait response from exchange
    private HashMap<String,Order> NewOrders;
    //Storage for strategy positions
    private HashMap<String,StrategyPosition> StrategiesPositions;
    //WS controller for user sending
    private ServerWSController wsController;


    //Logger variables
    private Logger trclog = Logger.getLogger(this.getClass().getName());


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
        NewOrders = new HashMap<>();
    }

    public ServerWSController getWsController() {
        return wsController;
    }

    public void setWsController(ServerWSController wsController) {
        this.wsController = wsController;
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

    public HashMap<String, Order> getNewOrders() {
        return NewOrders;
    }

    public void setNewOrders(HashMap<String, Order> newOrders) {
        NewOrders = newOrders;
    }

    public OrderStatus updateWorkingOrder(Order order){
        OrderStatus currentStatus = order.getStatus();
        if(WorkingOrders.containsKey(order.getExchangeID())){
            synchronized (getWorkingOrders()){
                Order changeOrder = WorkingOrders.get(order.getExchangeID());
                changeOrder.setFilled(order.getFilled());
                changeOrder.setExecuted(order.getExecuted());
                changeOrder.setLastUpdate(order.getLastUpdate());
                changeOrder.setCancelled_qty(order.getCancelled_qty());
                changeOrder.setStatus(order.getStatus());
                if(currentStatus == OrderStatus.CANCELLED){
                    synchronized (getCancelledOrders()) {
                        if (!CancelledOrders.containsKey(changeOrder.getExchangeID())) {
                            CancelledOrders.put(changeOrder.getExchangeID(), changeOrder);
                            WorkingOrders.remove(order.getExchangeID());
                        }
                    }
                }
                if(currentStatus == OrderStatus.REJECTED){
                    synchronized (getRejectedOrders()){
                        if(!RejectedOrders.containsKey(changeOrder.getExchangeID())){
                            RejectedOrders.put(changeOrder.getExchangeID(),changeOrder);
                            WorkingOrders.remove(order.getExchangeID());
                        }
                    }
                }
                if(currentStatus == OrderStatus.FULL_FILLED){
                    synchronized (getFilledOrders()){
                        if(!FilledOrders.containsKey(changeOrder.getExchangeID())){
                            FilledOrders.put(changeOrder.getExchangeID(),changeOrder);
                            WorkingOrders.remove(order.getExchangeID());
                        }
                    }
                }
            }
        }
        return currentStatus;
    }

    public void putNewOrderRelation(Order order){
        //Logging
        trclog.log(Level.INFO,Thread.currentThread()
                .getStackTrace()[1]
                .getMethodName()+" "+order.toString());

        synchronized (getOrderRelations()) {
            if(OrderRelations.containsKey(order.getInstructionKey())){
                trclog.log(Level.INFO,order.getInstructionKey()+" exist in current storage(duplicated)");
            }else {
                OrderRelations.put(order.getInstructionKey(),null);
            }

        }

        synchronized ((getNewOrders())){
            if(NewOrders.containsKey(order.getInstructionKey())){
                trclog.log(Level.INFO,order.getInstructionKey()+" exist in current storage(duplicated)");
            }else {
                NewOrders.put(order.getInstructionKey(),order);
            }
        }

    }
    public void updateOrderRelation(String clientInstructionID,String exchangeOrderId) {
        trclog.log(Level.INFO,Thread.currentThread()
                .getStackTrace()[1]
                .getMethodName()+" "+clientInstructionID+" "+exchangeOrderId);

        synchronized (getOrderRelations()) {
            OrderRelations.replace(clientInstructionID, exchangeOrderId);
        }
    }

    public void distributeNewOrderId(String clientInstructionId, String exchangeId, OrderStatus status){
        //Logging
        String func = Thread.currentThread().getStackTrace()[1].getMethodName();
        trclog.log(Level.WARNING,func+" "+clientInstructionId+" "+exchangeId+" "+status.toString());
        updateOrderRelation(clientInstructionId,exchangeId);
        trclog.log(Level.WARNING,func+"Step 1");
        //If this order was sent by client
        if(NewOrders.containsKey(clientInstructionId)) {
            trclog.log(Level.WARNING,func+"Step 2 ");
            Order ClientOrder = NewOrders.get(clientInstructionId);
            trclog.log(Level.WARNING,func+"Step 2.1.1 "+NewOrders.toString());
            trclog.log(Level.WARNING,func+"Step 2.1.2 "+ClientOrder.toString());
            ClientOrder.setExchangeID(exchangeId);
            ClientOrder.setLastUpdate(currentTimeMillis());
            ClientOrder.setStatus(status);
            //Maybe we have messages from exchange stream for this order
            if(TempOrders.containsKey(exchangeId)){
                trclog.log(Level.WARNING,func+"Step 2.2");
                Order existOrder = TempOrders.get(exchangeId);
                ClientOrder.setFilled(existOrder.getFilled());
                ClientOrder.setExecuted(existOrder.getExecuted());
                ClientOrder.setCancelled_qty(existOrder.getCancelled_qty());
                ClientOrder.setStatus(existOrder.getStatus());
                ClientOrder.setLastUpdate(existOrder.getLastUpdate());
            }
            trclog.log(Level.WARNING,func+"Step 3");
            OrderStatus orderStatus = ClientOrder.getStatus();
            OrdersSnapshotMessage currentWorkingSnapshot = new OrdersSnapshotMessage();
            switch(orderStatus) {
                case CANCELLED:
                    CancelledOrders.put(ClientOrder.getExchangeID(), ClientOrder);
                    currentWorkingSnapshot.setOrdersSnapshot(CancelledOrders);
                    currentWorkingSnapshot.setOrderSnapShotType(OrderSnapShotType.CANCELLED);
                    break;
                case REJECTED:
                    RejectedOrders.put(ClientOrder.getExchangeID(), ClientOrder);
                    currentWorkingSnapshot.setOrdersSnapshot(RejectedOrders);
                    currentWorkingSnapshot.setOrderSnapShotType(OrderSnapShotType.REJECTED);
                case FULL_FILLED:
                    FilledOrders.put(ClientOrder.getExchangeID(), ClientOrder);
                    currentWorkingSnapshot.setOrdersSnapshot(FilledOrders);
                    currentWorkingSnapshot.setOrderSnapShotType(OrderSnapShotType.FILLED);
                    break;
                default:
                    WorkingOrders.put(ClientOrder.getExchangeID(), ClientOrder);
                    currentWorkingSnapshot.setOrdersSnapshot(WorkingOrders);
                    currentWorkingSnapshot.setOrderSnapShotType(OrderSnapShotType.WORKING);
                    break;
            }
            trclog.log(Level.WARNING,func+"Step 4");
            trclog.log(Level.INFO,func+ClientOrder.toString());
            //Send working orders to client

            trclog.log(Level.WARNING,func+"Step 5");
            wsController.SendOrderSnapshotPointMessage(currentWorkingSnapshot,10);
        }
    }

    public OrderStatus distributeExchangeOrderMessage(Order order){
        //Logging
        String func = Thread.currentThread().getStackTrace()[1].getMethodName();
        trclog.log(Level.INFO,func+" "+order.toString());

        synchronized (getTempOrders()) {
            if(WorkingOrders.containsKey(order.getExchangeID())) {
                trclog.log(Level.INFO,func+" Update_Order in WorkingOrders"+order.toString());
                updateWorkingOrder(order);
            }else{
                if(TempOrders.containsKey(order.getExchangeID())){
                    TempOrders.replace(order.getExchangeID(),order);
                    trclog.log(Level.INFO,"Update order in TempStorage"+order.getExchangeID());

                }else{
                    TempOrders.put(order.getExchangeID(),order);
                    trclog.log(Level.INFO,"Put New Order to TempStorage:"+order.getExchangeID());
                }
            }
        }
        return order.getStatus();
    }


    public void CleanUpTemp(int unusedTimeSec){
        trclog.log(Level.INFO,"CleanUp TEMP storage , store-time: "+String.valueOf(unusedTimeSec)+" sec");
        long CurrentTimestamp = currentTimeMillis();
        Set<String> toRemoveId = new HashSet<>();
        synchronized (this.TempOrders){
            for(Map.Entry<String, classes.trading.Order> orderEntry: TempOrders.entrySet()){
                long diff = CurrentTimestamp - orderEntry.getValue().getLastUpdate();
                if(CurrentTimestamp - orderEntry.getValue().getLastUpdate() > unusedTimeSec*1000){
                    toRemoveId.add(orderEntry.getKey());
                }
            }
            int count = TempOrders.size();
            TempOrders.keySet().removeAll(toRemoveId);
            trclog.log(Level.INFO,"Cleanup TEMP storage before: "+String.valueOf(count)+" ,after: "+String.valueOf(TempOrders.size()));
        }

    }

    @Override
    public void Destroy() {

    }

    @Override
    public String toString() {
        while(true){
        try {
            return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
         }catch(Exception e){
            e.printStackTrace();
        }
        }
    }
}
