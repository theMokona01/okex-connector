package interfaces;

public interface Instrument {
    void setSymbol(String Symbol, String ExchangeSymbol, Double tick, Double price_precision);
    void SetSymbol(String Symbol);
    String GetSymbol();
    void SetExchangeSymbol(String ExchangeSymbol);
    String GetExchangeSymbol();
}