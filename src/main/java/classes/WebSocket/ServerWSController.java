package classes.WebSocket;

import classes.WebSocket.messages.HelloMessage;
import classes.WebSocket.messages.PricesMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ServerWSController {
    static final String PricesSendPoint = "/rcv/prices";
    static final String InfoSendPoint   = "/rcv/info";

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    public void SendPricePointMessage(PricesMessage message){
        messagingTemplate.convertAndSend(PricesSendPoint,message);
    }

    @MessageMapping("/pricepoint")
    @SendTo(PricesSendPoint)
    public PricesMessage pricesMsgDog(@Payload String message) throws Exception {
        System.out.println("inside /pricepoint message "+message.toString());
        return new PricesMessage("Hello sub send "+message);//new WSGreeting("Hello, ");// + message.getSubscription_name() + "!");
    }

    @MessageMapping("/hello")
    @SendTo(InfoSendPoint)
    public HelloMessage helloMsgDog(@Payload String message) throws Exception {
        System.out.println("Received /hello message "+message.toString());
        return new HelloMessage("Hello accepted "+message);//new WSGreeting("Hello, ");// + message.getSubscription_name() + "!");
    }

    @SubscribeMapping(PricesSendPoint)
    public PricesMessage subscripe_greetings() {
        System.out.println("inside subscribe_greetings");
        return new PricesMessage("Subscribed to /rcv/prices");
    }


}
