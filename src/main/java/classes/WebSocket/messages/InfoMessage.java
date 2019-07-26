package classes.WebSocket.messages;

import org.apache.commons.lang3.builder.ToStringStyle;

public class InfoMessage {
    private String content="Hello";
    public InfoMessage() {}
    public InfoMessage(String content) {
        this.content = content;
    }
    public String getContent() {
        return content;
    }
    @Override
    public String toString(){
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
