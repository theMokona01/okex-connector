package classes.WebSocket.messages;

import org.apache.commons.lang3.builder.ToStringStyle;


import java.util.HashMap;

public class BalanceMessage {
    private HashMap<String,Double> balances;
    private String Exchange;


    public BalanceMessage(){}
    public BalanceMessage(String exchange) {
        this.Exchange = exchange;
        this.balances = new HashMap<String, Double>();
    }

    public BalanceMessage(HashMap balances,String Exchange) {
        this.balances = balances;
        this.Exchange = Exchange;
    }

    public HashMap<String, Double> getBalances() {
        return balances;
    }

    public void setBalances(HashMap balances) {
        this.balances = balances;
    }

    public void setSymbolBalance(String Symbol, double Balance){
        if(this.balances.containsKey(Symbol)){
            this.balances.replace(Symbol,Balance);
        }else{
            this.balances.put(Symbol,Balance);
        }
    }

    public String getExchange() {
        return Exchange;
    }

    public void setExchange(String exchange) {
        Exchange = exchange;
    }

    public void CleanBalance(){
        this.balances.clear();
    }

    public Double getSymbolBalance(String Symbol){
        if(this.balances.containsKey(Symbol)){
            return this.balances.get(Symbol);
        }else{
            return 0.0;
        }
    }
    @Override
    public String toString(){
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
