package classes.WebSocket.messages;

public class StrategyPositionMessage {
    String Strategy;
    String Symbol;
    String ExchangeSymbol;
    double SellPosition;
    double BuyPosition;
    double SellExecuted;
    double BuyExecuted;
    long timestamp;

    public StrategyPositionMessage() {}

    public StrategyPositionMessage(String strategy, String symbol, String exchangeSymbol, double sellPosition, double buyPosition, double sellExecuted, double buyExecuted, long timestamp) {
        Strategy = strategy;
        Symbol = symbol;
        ExchangeSymbol = exchangeSymbol;
        SellPosition = sellPosition;
        BuyPosition = buyPosition;
        SellExecuted = sellExecuted;
        BuyExecuted = buyExecuted;
        this.timestamp = timestamp;
    }

    public String getStrategy() {
        return Strategy;
    }

    public void setStrategy(String strategy) {
        Strategy = strategy;
    }

    public String getSymbol() {
        return Symbol;
    }

    public void setSymbol(String symbol) {
        Symbol = symbol;
    }

    public String getExchangeSymbol() {
        return ExchangeSymbol;
    }

    public void setExchangeSymbol(String exchangeSymbol) {
        ExchangeSymbol = exchangeSymbol;
    }

    public double getSellPosition() {
        return SellPosition;
    }

    public void setSellPosition(double sellPosition) {
        SellPosition = sellPosition;
    }

    public double getBuyPosition() {
        return BuyPosition;
    }

    public void setBuyPosition(double buyPosition) {
        BuyPosition = buyPosition;
    }

    public double getSellExecuted() {
        return SellExecuted;
    }

    public void setSellExecuted(double sellExecuted) {
        SellExecuted = sellExecuted;
    }

    public double getBuyExecuted() {
        return BuyExecuted;
    }

    public void setBuyExecuted(double buyExecuted) {
        BuyExecuted = buyExecuted;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}




