package classes.WebSocket;

import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ClientWebsocketHandler extends TextWebSocketHandler {
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {

    }
}

