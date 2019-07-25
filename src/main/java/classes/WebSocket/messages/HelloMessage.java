package classes.WebSocket.messages;

public class HelloMessage {
    private String content="Hello";
    public HelloMessage() {}
    public HelloMessage(String content) {
        this.content = content;
    }
    public String getContent() {
        return content;
    }
}
