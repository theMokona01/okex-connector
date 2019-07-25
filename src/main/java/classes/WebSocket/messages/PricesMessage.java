package classes.WebSocket.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PricesMessage {
    private String content="Initial content";
    private String content2="Content2";
    public PricesMessage() {
    }
    public PricesMessage(String content) {
        this.content = content;
    }
    public String getContent() {
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        //return content;
    }
    //@Override
    //public String toString(){
    //    return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this);
    //}

}
