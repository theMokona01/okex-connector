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
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;


    private Gson gson = new Gson();

    /*@MessageMapping("/gate")
    @SendTo("/topic/greetings")
    public WSGreeting greeting(@org.jetbrains.annotations.NotNull SubscriptionMessage message) throws Exception {
        System.out.println("OK "+message.toString());
        Thread.sleep(1000); // simulated delay
        return new WSGreeting("Subscription: " + HtmlUtils.htmlEscape(message.getSubscription_name()) + "!");
    }*/
    //@Scheduled(fixedRate = 1)
    @MessageMapping("/greeting")
    @SendTo("/topic/greetings")
    public WSGreeting greeting(@Payload String message) throws Exception {
        System.out.println("inside greeting "+message.toString());
        //System.out.println("OK "+message.toString());
        //messagingTemplate.convertAndSend("/topic/greetings","OQQ");
        return new WSGreeting("Hello sub send "+message);//new WSGreeting("Hello, ");// + message.getSubscription_name() + "!");
    }

    @SubscribeMapping("/topic/greetings")
    public WSGreeting subscripe_greetings() {
        System.out.println("inside subscribe_greetings");
        //return "Done subs";
        return new WSGreeting("Subscribed to /topic/greetings");
    }


}
