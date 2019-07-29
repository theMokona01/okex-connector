package classes.WebSocket;

import classes.WebSocket.messages.*;
import com.google.gson.Gson;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
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
    static final String OrdersSnapshotSendPoint = "/rcv/ordersnapshot";


    private static Logger trclog = Logger.getLogger(ServerWSController.class.getName());

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    //Broadcast sending BBO message
    public void SendBBOPointMessage(BBOMessage message){
        trclog.log(Level.INFO,"SendBBOPointMessage: "+message.toString());
        messagingTemplate.convertAndSend(BBOSendPoint,message);
    }

    //Broadcast sending balance message
    public void SendBalancePointMessage(BalanceMessage message){
        trclog.log(Level.INFO,"SendBalancePointMessage: "+message.toString());
        messagingTemplate.convertAndSend(BalanceSendPoint,message);
    }

    //Balance command point
    @MessageMapping("/balancepoint")
    @SendTo(BBOSendPoint)
    public BalanceMessage balanceMsgDog(@Payload String message) throws Exception {
        trclog.log(Level.INFO,"Received on point /balancepoint: "+message.toString());
        return new BalanceMessage("");//new WSGreeting("Hello, ");// + message.getSubscription_name() + "!");
    }

    //Price command point
    @MessageMapping("/pricepoint")
    @SendTo(BBOSendPoint)
    public BBOMessage pricesMsgDog(@Payload String message) throws Exception {
        trclog.log(Level.INFO,"Received on point /pricepoint: "+message.toString());
        return new BBOMessage();
    }

    //Initial hello message from client
    @MessageMapping("/hello")
    @SendTo(InfoSendPoint)
    public InfoMessage helloMsgDog(@Payload String message) throws Exception {
        trclog.log(Level.INFO,"Received on point /hello: "+message.toString());
        return new InfoMessage("Hello accepted "+message);
    }

    //Command point for trading management
    @MessageMapping("/command")
    @SendTo(InfoSendPoint)
    public InfoMessage commandMsgDog(@Payload String message) throws Exception {
        //CommandMessage cmdMsg = message.getPayload().getClass().toString();
        trclog.log(Level.INFO,"Received on point /command: "+message);//.getPayload().toString());
        Gson gson = new Gson();
        CommandMessage cmd = gson.fromJson(message,CommandMessage.class);
        cmd.DeserializeOrder();

        return new InfoMessage("Command accepted "+cmd.getCommand().toString()+":"+cmd.getOrder().toString());//message.toString());
    }


    //Subscribe BBO point #depricated now
    @SubscribeMapping(BBOSendPoint)
    public BBOMessage subscripe_greetings() {
        trclog.log(Level.INFO,"Received subscription : "+BBOSendPoint);
        return new BBOMessage();
    }

    //To String object conversion
    @Override
    public String toString(){
        return org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
