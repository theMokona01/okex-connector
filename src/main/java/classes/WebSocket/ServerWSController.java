package classes.WebSocket;

import classes.WebSocket.messages.BBOMessage;
import classes.WebSocket.messages.BalanceMessage;
import classes.WebSocket.messages.InfoMessage;
import classes.WebSocket.messages.MessageEndPoint;
import com.google.gson.Gson;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import pf.trading.connector.ConnectorCore;

import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class ServerWSController {
    static final MessageEndPoint EndPoints = new MessageEndPoint();
    static final String BBOSendPoint = "/rcv/bbo";
    static final String InfoSendPoint   = "/rcv/info";
    static final String BalanceSendPoint    = "/rcv/balance";


    private static Logger trclog = Logger.getLogger(ServerWSController.class.getName());

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    public void SendBBOPointMessage(BBOMessage message){
        trclog.log(Level.INFO,"SendBBOPointMessage: "+message.toString());
        messagingTemplate.convertAndSend(BBOSendPoint,message);
    }

    public void SendBalancePointMessage(BalanceMessage message){
        trclog.log(Level.INFO,"SendBalancePointMessage: "+message.toString());
        messagingTemplate.convertAndSend(BalanceSendPoint,message);
    }

    @MessageMapping("/balancepoint")
    @SendTo(BBOSendPoint)
    public BalanceMessage balanceMsgDog(@Payload String message) throws Exception {
        trclog.log(Level.INFO,"Received on point /pricepoint: "+message.toString());
        return new BalanceMessage("");//new WSGreeting("Hello, ");// + message.getSubscription_name() + "!");
    }

    @MessageMapping("/pricepoint")
    @SendTo(BBOSendPoint)
    public BBOMessage pricesMsgDog(@Payload String message) throws Exception {
        trclog.log(Level.INFO,"Received on point /pricepoint: "+message.toString());
        return new BBOMessage();//new WSGreeting("Hello, ");// + message.getSubscription_name() + "!");
    }

    @MessageMapping("/hello")
    @SendTo(InfoSendPoint)
    public InfoMessage helloMsgDog(@Payload String message) throws Exception {
        trclog.log(Level.INFO,"Received on point /hello: "+message.toString());
        return new InfoMessage("Hello accepted "+message);
    }

    @SubscribeMapping(BBOSendPoint)
    public BBOMessage subscripe_greetings() {
        trclog.log(Level.INFO,"Received subscription : "+BBOSendPoint);
        return new BBOMessage();
    }

    @Override
    public String toString(){
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
