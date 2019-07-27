package classes.WebSocket.messages;

import com.google.gson.Gson;
import org.apache.commons.lang3.builder.ToStringStyle;

public class BalanceMessage {
    private Gson balances;

    public BalanceMessage() {}

    public BalanceMessage(Gson balances) {
        this.balances = balances;
    }

    public Gson getBalances() {
        return balances;
    }

    public void setBalances(Gson balances) {
        this.balances = balances;
    }

    @Override
    public String toString(){
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
