package classes.WebSocket.messages;

import classes.trading.Execution;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SingleExecutionMessage {
    private Execution execution;

    public SingleExecutionMessage() { }

    public SingleExecutionMessage(Execution execution) {
        this.execution = execution;
    }

    public Execution getExecution() {
        return execution;
    }

    public void setExecution(Execution execution) {
        this.execution = execution;
    }

    @Override
    public String toString(){
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
