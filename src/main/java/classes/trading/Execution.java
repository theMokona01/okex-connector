package classes.trading;

import org.apache.commons.lang3.builder.ToStringStyle;

public class Execution {
    private Double Filled;
    private Double Executed;
    private Double Fee;
    private long Timestamp;
    public Execution() {
    }

    public Execution(Double filled, Double executed, Double fee, long timestamp) {
        Filled = filled;
        Executed = executed;
        Fee = fee;
        Timestamp = timestamp;
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

    public Double getFee() {
        return Fee;
    }

    public void setFee(Double fee) {
        Fee = fee;
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
