package Exchange;

import interfaces.Instrument;

public class OKEXInstrument implements Instrument {
    public String Symbol;
    public String ExchangeSymbol;

    public OKEXInstrument() {
    }

    @Override
    public void setSymbol(String Symbol, String ExchangeSymbol, Double tick, Double price_precision) {

    }

    @Override
    public String GetSymbol() {
        return Symbol;
    }

    @Override
    public String  GetExchangeSymbol() {
        return ExchangeSymbol;
    }

    @Override
    public void SetSymbol(String Symbol) {

    }

    @Override
    public void SetExchangeSymbol(String ExchangeSymbol) {

    }
}
