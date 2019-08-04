package classes.WebSocket.model;

import javax.persistence.*;
import java.io.Serializable;

//class which represents objects in database
@Entity
@Table(name = "ticker")
public class Ticker implements Serializable {
    //check if sender and receiver of object have loaded classes for object
    private static final long serialVersionUID = -2343243243242432341L;
    //run time stratey design
    @Id
    //configure increment of specified db column
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    //column names
    @Column(name = "instrumentId")
    private String instrumentId;

    @Column(name = "last")
    private Double last;

    @Column(name = "bestBid")
    private Double bestBid;

    @Column(name = "bestAsk")
    private Double bestAsk;

    @Column(name = "open24h")
    private Double open24h;

    @Column(name = "high24h")
    private Double high24h;

    @Column(name = "low24h")
    private Double low24h;

    @Column(name = "baseVolume")
    private Double baseVolume;

    @Column(name = "quoteVolume")
    private Double quoteVolume;


    @Column(name = "timestamp")
    private String timestamp;


    //private member only accesible here
    protected Ticker(){}



    //constructor + sets and gets
    public Ticker(String instrumentId, Double last, Double bestBid, Double bestAsk, Double open24h, Double high24h, Double low24h, Double baseVolume, Double quoteVolume, String timestamp) {
        this.instrumentId = instrumentId;
        this.last = last;
        this.bestBid = bestBid;
        this.bestAsk = bestAsk;
        this.open24h = open24h;
        this.high24h = high24h;
        this.low24h = low24h;
        this.baseVolume = baseVolume;
        this.quoteVolume = quoteVolume;
        this.timestamp = timestamp;
    }

    @Override
    public String toString(){
        return String.format("Ticker[id=%d, instrumentId='%s', last='%s', bestBid='%s', bestAsk='%s', open24h='%s', high24h='%s', low24h='%s', baseVolume='%s', quoteVolume='%s', timestamp='%s']", id, instrumentId, last, bestBid, bestAsk, open24h, high24h, low24h, baseVolume, quoteVolume, timestamp);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }


    public Double getQuoteVolume() {
        return quoteVolume;
    }

    public void setQuoteVolume(Double quoteVolume) {
        this.quoteVolume = quoteVolume;
    }

    public Double getBaseVolume() {
        return baseVolume;
    }

    public void setBaseVolume(Double baseVolume) {
        this.baseVolume = baseVolume;
    }

    public Double getLow24h() {
        return low24h;
    }

    public void setLow24h(Double low24h) {
        this.low24h = low24h;
    }

    public Double getHigh24h() {
        return high24h;
    }

    public void setHigh24h(Double high24h) {
        this.high24h = high24h;
    }

    public Double getOpen24h() {
        return open24h;
    }

    public void setOpen24h(Double open24h) {
        this.open24h = open24h;
    }

    public void setBestAsk(Double bestAsk) {
        this.bestAsk = bestAsk;
    }

    public Double getBestAsk() {
        return bestAsk;
    }

    public void setBestBid(Double bestBid) {
        this.bestBid = bestBid;
    }

    public Double getBestBid() {
        return bestBid;
    }

    public Double getLast() {
        return last;
    }

    public void setLast(Double last) {
        this.last = last;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }
}
