package classes.WebSocket.messages;

public class InfoMessage {
    private String content="Hello";
    public InfoMessage() {}
    public InfoMessage(String content) {
        this.content = content;
    }
    public String getContent() {
        return content;
    }
}
