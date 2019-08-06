package classes.WebSocket.model;

import classes.Enums.OrderSide;
import classes.Enums.OrderStatus;
import classes.Enums.OrderType;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

//import javax.persistence.*;
import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.UUID;

@Entity
@OptimisticLocking(type = OptimisticLockType.DIRTY)
@DynamicUpdate
@Table
public class EOrder {
    //check if sender and receiver of object have loaded classes for object
    private static final long serialVersionUID = -2343243243242432341L;
    //run time stratey design
    //@Id
    //configure increment of specified db column
    //@GeneratedValue(strategy = GenerationType.AUTO)
    //private long id;

    @Id
    private String exchangeId;

    //column names
    @Column(name = "strategy")
    private String strategy;

    @Column(name = "instructionkey")
    private String instructionKey;

    //@Column(name = "exchangeid")
    //private String exchangeId;

    @Column(name = "internalid")
    private String internalId;

    @Column(name = "price")
    private double price;

    @Column(name = "size")
    private double size;

    @Enumerated(EnumType.STRING)
    @Column(name = "side")
    private OrderSide orderSide;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private OrderType orderType;



    @Column(name = "symbol")
    private String symbol;

    @Column(name = "exchangesymbol")
    private String exchangeSymbol;

    @Column(name = "leftsymbol")
    private String leftSymbol;

    @Column(name = "rightsymbol")
    private String rightSymbol;

    @Column(name = "inittimestamp")
    private long initTimestamp;

    @Column(name = "updatetimestamp")
    private long updateTimestamp;




    @Column(name = "filled")
    private double filled;

    @Column(name = "executed")
    private double executed;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;


    public EOrder() {}

    public EOrder(String instructionKey, String exchangeId, String internalId, Double price, Double size, OrderSide orderSide) {
        this.instructionKey = instructionKey;
        this.exchangeId = exchangeId;
        this.internalId = internalId;
        this.price = price;
        this.size = size;
        this.orderSide = orderSide;
        this.orderType = orderType;


    }

    @Override
    public String toString(){
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        //return String.format("EOrder[id='%d', instructionid='%s', exchangeid='%s', internalid='%s', price='%f', size='%f']", id, instructionId, exchangeId, internalId, price, size);
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(String exchangeId) {
        this.exchangeId = exchangeId;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getInstructionKey() {
        return instructionKey;
    }

    public void setInstructionKey(String instructionKey) {
        this.instructionKey = instructionKey;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public OrderSide getOrderSide() {
        return orderSide;
    }

    public void setOrderSide(OrderSide orderSide) {
        this.orderSide = orderSide;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        symbol = symbol;
    }

    public String getExchangeSymbol() {
        return exchangeSymbol;
    }

    public void setExchangeSymbol(String exchangeSymbol) {
        this.exchangeSymbol = exchangeSymbol;
    }

    public String getLeftSymbol() {
        return leftSymbol;
    }

    public void setLeftSymbol(String leftSymbol) {
        this.leftSymbol = leftSymbol;
    }

    public String getRightSymbol() {
        return rightSymbol;
    }

    public void setRightSymbol(String rightSymbol) {
        this.rightSymbol = rightSymbol;
    }

    public long getInitTimestamp() {
        return initTimestamp;
    }

    public void setInitTimestamp(long initTimestamp) {
        this.initTimestamp = initTimestamp;
    }

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public double getFilled() {
        return filled;
    }

    public void setFilled(double filled) {
        this.filled = filled;
    }

    public double getExecuted() {
        return executed;
    }

    public void setExecuted(double executed) {
        this.executed = executed;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
