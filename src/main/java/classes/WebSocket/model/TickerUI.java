package classes.WebSocket.model;

public class TickerUI {
    private String instrumentId;
    private Double last;
    private Double bestBid;
    private Double bestAsk;
    private Double open24h;
    private Double high24h;
    private Double low24h;
    private Double baseVolume;
    private Double quoteVolume;
    private String timestamp;


    protected TickerUI() {
    }

    public TickerUI(String instrumentId, Double last, Double bestBid, Double bestAsk, Double open24h, Double high24h, Double low24h, Double baseVolume, Double quoteVolume, String timestamp) {
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

    public String toString(){
        return String.format("Ticker[instrumentId='%s', last='%s', bestBid='%s', bestAsk='%s', open24h='%s', high24h='%s', low24h='%s', baseVolume='%s', quoteVolume='%s', timestamp='%s']", instrumentId, last, bestBid, bestAsk, open24h, high24h, low24h, baseVolume, quoteVolume, timestamp);
    }
}
