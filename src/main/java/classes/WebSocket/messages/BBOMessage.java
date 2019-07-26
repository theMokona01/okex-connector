package classes.WebSocket.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.builder.ToStringStyle;

public class BBOMessage {
    private String info="Initial";
    private String instrument;
    private double ask;
    private double bid;
    private double ask_size;
    private double bid_size;
    private long timestamp;

    public BBOMessage() {}
    public BBOMessage(String info, String instrument, double ask, double bid, double ask_size, double bid_size, long timestamp) {
        this.info = info;
        this.instrument = instrument;
        this.ask = ask;
        this.bid = bid;
        this.ask_size = ask_size;
        this.bid_size = bid_size;
        this.timestamp = timestamp;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public double getAsk() {
        return ask;
    }

    public void setAsk(double ask) {
        this.ask = ask;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public double getAsk_size() {
        return ask_size;
    }

    public void setAsk_size(double ask_size) {
        this.ask_size = ask_size;
    }

    public double getBid_size() {
        return bid_size;
    }

    public void setBid_size(double bid_size) {
        this.bid_size = bid_size;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public String toString(){
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
