package interfaces;

public interface Instrument {
    public String Symbol="";
    public String ExchangeSymbol="";
    void setSymbol(String Symbol, String ExchangeSymbol, Double tick, Double price_precision);
}