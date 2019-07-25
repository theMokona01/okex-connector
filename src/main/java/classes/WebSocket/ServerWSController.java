package classes.WebSocket;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.security.Principal;
import java.util.Map;

@Controller
public class ServerWSController {
    static final String PricesSendPoint = "/rcv/prices";

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    public void SendPricePointMessage(WSPrices message){
        messagingTemplate.convertAndSend(PricesSendPoint,message);
    }

    @MessageMapping("/pricepoint")
    @SendTo(PricesSendPoint)
    public WSPrices pricesMsgDog(@Payload String message) throws Exception {
        System.out.println("inside /pricepoint message "+message.toString());
        return new WSPrices("Hello sub send "+message);//new WSGreeting("Hello, ");// + message.getSubscription_name() + "!");
    }

    @SubscribeMapping(PricesSendPoint)
    public WSPrices subscripe_greetings() {
        System.out.println("inside subscribe_greetings");
        return new WSPrices("Subscribed to /rcv/prices");
    }


}
