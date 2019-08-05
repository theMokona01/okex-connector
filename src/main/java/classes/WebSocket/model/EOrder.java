package classes.WebSocket.model;

import classes.Enums.OrderSide;
import classes.Enums.OrderType;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

//import javax.persistence.*;
import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.UUID;

@Entity
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

    @Column(name = "instructionid")
    private String instructionId;

    //@Column(name = "exchangeid")
    //private String exchangeId;

    @Column(name = "internalid")
    private String internalId;

    @Column(name = "price")
    private double price;

    @Column(name = "size")
    private double size;

    @Column(name = "side")
    private OrderSide orderSide;

    @Column(name = "type")
    private OrderType orderType;


    public EOrder() {}

    public EOrder(String instructionId, String exchangeId, String internalId, Double price, Double size, OrderSide orderSide, OrderType orderType) {
        this.instructionId = instructionId;
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

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    /*public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }*/

    public String getInstructionId() {
        return instructionId;
    }

    public void setInstructionId(String instructionId) {
        this.instructionId = instructionId;
    }

    public String getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(String exchangeId) {
        this.exchangeId = exchangeId;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getSize() {
        return size;
    }

    public void setSize(Double size) {
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
}
