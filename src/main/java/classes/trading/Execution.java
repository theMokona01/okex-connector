package classes.trading;

import org.apache.commons.lang3.builder.ToStringStyle;

public class Execution {
    private String Id;
    private Double Filled;
    private Double Executed;
    private Double Price;
    private Double Fee;
    private String OrderId;
    private long Timestamp;
    public Execution() {
    }

    public Execution(String id, Double filled, Double executed, Double price, Double fee,String orderId, long timestamp) {
        Id = id;
        Filled = filled;
        Executed = executed;
        Price = price;
        Fee = fee;
        OrderId = orderId;
        Timestamp = timestamp;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public Double getFilled() {
        return Filled;
    }

    public void setFilled(Double filled) {
        Filled = filled;
    }

    public Double getExecuted() {
        return Executed;
    }

    public void setExecuted(Double executed) {
        Executed = executed;
    }

    public Double getPrice() {
        return Price;
    }

    public void setPrice(Double price) {
        Price = price;
    }

    public Double getFee() {
        return Fee;
    }

    public void setFee(Double fee) {
        Fee = fee;
    }

    public String getOrderId() {
        return OrderId;
    }

    public void setOrderId(String orderId) {
        OrderId = orderId;
    }

    public long getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(long timestamp) {
        Timestamp = timestamp;
    }
    @Override
    public String toString(){
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
