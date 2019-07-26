package classes.WebSocket;

import classes.WebSocket.messages.BBOMessage;
import classes.WebSocket.messages.InfoMessage;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ServerWSController {
    static final String PricesSendPoint = "/rcv/bbo";
    static final String InfoSendPoint   = "/rcv/info";

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    public void SendBBOPointMessage(BBOMessage message){
        messagingTemplate.convertAndSend(PricesSendPoint,message);
    }

    @MessageMapping("/pricepoint")
    @SendTo(PricesSendPoint)
    public BBOMessage pricesMsgDog(@Payload String message) throws Exception {
        System.out.println("inside /pricepoint message "+message.toString());
        return new BBOMessage("Hello sub send "+message);//new WSGreeting("Hello, ");// + message.getSubscription_name() + "!");
    }

    @MessageMapping("/hello")
    @SendTo(InfoSendPoint)
    public InfoMessage helloMsgDog(@Payload String message) throws Exception {
        System.out.println("Received /hello message "+message.toString());
        return new InfoMessage("Hello accepted "+message);//new WSGreeting("Hello, ");// + message.getSubscription_name() + "!");
    }

    @SubscribeMapping(PricesSendPoint)
    public BBOMessage subscripe_greetings() {
        System.out.println("inside subscribe_greetings");
        return new BBOMessage("Subscribed to /rcv/bbo");
    }


}
