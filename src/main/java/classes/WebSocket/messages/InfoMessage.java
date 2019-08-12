package classes.WebSocket.messages;

import classes.Enums.InfoType;
import com.google.gson.Gson;
import org.apache.commons.lang3.builder.ToStringStyle;

public class InfoMessage {
    private InfoType infoType = InfoType.SIMPLE;
    private String content="Hello";
    public InfoMessage() {}
    public InfoMessage(String content) {
        this.content = content;
    }
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void SerializeSimpleGSONContent(Object o){
        Gson gson = new Gson();
        this.content = gson.toJson(o);
    }

    public InfoType getInfoType() {
        return infoType;
    }

    public void setInfoType(InfoType infoType) {
        this.infoType = infoType;
    }

    @Override
    public String toString(){
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
