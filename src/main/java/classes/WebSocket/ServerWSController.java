package classes.WebSocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class ServerWSController {
    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting greeting(SubscriptionMessage message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return new Greeting("Subscription: " + HtmlUtils.htmlEscape(message.getSubscription_name()) + "!");
    }

}
