package classes.WebSocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class WebSocketEventListener {

    private static Logger trclog = Logger.getLogger(WebSocketEventListener.class.getName());

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        trclog.log(Level.INFO,"Connect:Received a new web socket connection: "+event.toString());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        trclog.log(Level.INFO,"Disconnect: "+event.toString());
    }
    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        trclog.log(Level.INFO,"Subscript initiate: "+event.toString());
    }
}