package classes.WebSocket;

import classes.Enums.CommandStatus;
import classes.Enums.Commands;
import classes.Enums.OrderCommand;
import classes.Enums.OrderStatus;

import classes.WebSocket.messages.*;
import classes.WebSocket.model.EExecution;
import classes.WebSocket.model.EOrder;
import classes.WebSocket.model.Ticker;

import classes.WebSocket.repository.ExecutionRepository;
import classes.WebSocket.repository.OrderRepository;
import classes.WebSocket.repository.TickerRepository;
import com.google.gson.Gson;
import interfaces.ExchangeConnector;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.currentTimeMillis;

//@Service("PFService")
@Controller
public class ServerWSController {
    //Exchange connector instances for exchange management
    private ExchangeConnector exchangeConnector;

    //Endpoints storage
    static final MessageEndPoint EndPoints = new MessageEndPoint();
    //Server commands endpoints
    static final String CommandBalancePoint = "/balancepoint";
    static final String CommandPricePoint = "/pricepoint";
    static final String CommandHelloPoint = "/hello";
    static final String CommandDefaultPoint = "/command";
    static final String CommandOrderPoint = "/order";
    //static final String CommandOrderManagementPoint = "/order";

    //Client listeners endpoints
    static final String BBOSendPoint = "/rcv/bbo";
    static final String InfoSendPoint   = "/rcv/info";
    static final String BalanceSendPoint    = "/rcv/balance";
    static final String CommandOrderSendPoint = "/rcv/order";


    static final String SingleExecutionSendPoint = "/rcv/singleexecution";
    static final String SingleOrderSendPoint = "/rcv/singleorder";
    static final String OrdersSnapshotSendPoint = "/rcv/ordersnapshot";


    private static Logger trclog = Logger.getLogger(ServerWSController.class.getName());

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    @Autowired
    private TickerRepository tickerRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    ExecutionRepository executionRepository;

    public ExchangeConnector getExchangeConnector() {
        return exchangeConnector;
    }

    public void setExchangeConnector(ExchangeConnector exchangeConnector) {
        this.exchangeConnector = exchangeConnector;
    }
    //Updateting DB
    public void updateDBorderFromexchange(EOrder order){
        List<EOrder> btwOrders = this.getOrderByExchangeId(order.getExchangeId());
        if(btwOrders.size()!=0){
        EOrder upOrder = btwOrders.get(0);
        upOrder.setStatus(order.getStatus());
        upOrder.setFilled(order.getFilled());
        upOrder.setUpdateTimestamp(order.getUpdateTimestamp());
        orderRepository.save(upOrder);
        }else{
            orderRepository.save(order);
        }
    }

    public void insertDBRejectedFromExchange(EOrder order){
        orderRepository.save(order);
    }

    public void updateClientDBOrder(EOrder order){
        orderRepository.updateFromClient(order.getExchangeId(),order.getInstructionKey(),order.getStrategy(),order.getSymbol(),
                order.getExchangeSymbol(),order.getLeftSymbol(),order.getRightSymbol(),order.getOrderType(),order.getPrice(),order.getSize(),
                order.getInitTimestamp(),order.getUpdateTimestamp());
    }

    public void cleanOldTrashOrders(long seconds){
        try {
            //orderRepository.cleanOldTrashOrders(seconds);
            long currentTimestamp = currentTimeMillis();
            long deleleSeconds = seconds * 1000;
            long delTime = currentTimestamp - deleleSeconds;
            //trclog.log(Level.WARNING,"CleanUp");
            //orderRepository.deleteAllByInstructionKey("UNKNOWN");//AndUpdateTimestampLessThan("",currentTimestamp-deleleSeconds);

            //orderRepository.deleteAllByStatusAndUpdateTimestampLessThan(OrderStatus.REJECTED,delTime);
        } catch(Exception e){
            System.out.println("HERE");
            e.printStackTrace();
        } catch (Throwable throwable) {
            System.out.println("Here exception" );
            throwable.printStackTrace();
        }
    }

    public List<EOrder> getOrderByExchangeId(String exchangeId){
        return orderRepository.findByExchangeId(exchangeId);
    }

    public void updateDBexecution(EExecution execution){
        executionRepository.save(execution);
    }



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

    //Broadcast sending SingleOrderMessage
    public void SendSingleOrderPointMessage(SingleOrderMessage message){
        trclog.log(Level.INFO,"SendSingleOrderPointMessage: "+message.toString());
        messagingTemplate.convertAndSend(SingleOrderSendPoint,message);
    }

    //Broadcast sending SingleExecutionMessage
    public void SendSingleExecutionPointMessage(SingleExecutionMessage message){
        trclog.log(Level.INFO,message.getClass().toString()+": "+message.toString());
        //tickerRepository.save(new Ticker(String.valueOf(System.currentTimeMillis()),221.5, 211.4, 211.4, 211.45, 207.2, 214.3, 206.2, 62449406.56, "2019-07-31T04:52:17.152Z"));
        messagingTemplate.convertAndSend(SingleExecutionSendPoint,message);
    }

    //Broadcast sending SingleExecutionMessage
    public void SendOrderSnapshotPointMessage(OrdersSnapshotMessage message,int OneSendLimitMsg){
        trclog.log(Level.INFO,message.getClass().toString()+": "+message.toString());
                messagingTemplate.convertAndSend(OrdersSnapshotSendPoint,message);
    }

    //Broadcast sending InfoMessage
    public void SendInfoPointMessage(InfoMessage message){
        trclog.log(Level.INFO,message.getClass().toString()+": "+message.toString());
        messagingTemplate.convertAndSend(InfoSendPoint,message);
    }

    //Balance command point
    @MessageMapping(CommandBalancePoint)
    @SendTo(BBOSendPoint)
    public BalanceMessage balanceMsgDog(@Payload String message) throws Exception {
        trclog.log(Level.INFO,"Received on point /balancepoint: "+ message);
        return new BalanceMessage("");//new WSGreeting("Hello, ");// + message.getSubscription_name() + "!");
    }

    //Price command point
    @MessageMapping(CommandPricePoint)
    @SendTo(BBOSendPoint)
    public BBOMessage pricesMsgDog(@Payload String message) throws Exception {
        trclog.log(Level.INFO,"Received on point /pricepoint: "+ message);
        return new BBOMessage();
    }

    //Initial hello message from client
    @MessageMapping(CommandHelloPoint)
    @SendTo(InfoSendPoint)
    public InfoMessage helloMsgDog(@Payload String message) throws Exception {
        trclog.log(Level.INFO,"Received on point /hello: "+ message);
        return new InfoMessage("Hello accepted "+message);
    }

    //Command point for trading management {debug usage}
    @MessageMapping(CommandDefaultPoint)
    @SendTo(InfoSendPoint)
    public InfoMessage commandMsgDog(@Payload String message) throws Exception {
        //CommandMessage cmdMsg = message.getPayload().getClass().toString();
        trclog.log(Level.INFO,"Received on point /command: "+message);//.getPayload().toString());
        Gson gson = new Gson();
        CommandMessage cmd = gson.fromJson(message,CommandMessage.class);
        cmd.DeserializeOrder();

        return new InfoMessage("Command accepted "+cmd.getCommand().toString()+":"+cmd.getOrder().toString());//message.toString());
    }

    //Command point for orders management
    @MessageMapping(CommandOrderPoint)
    @SendTo(CommandOrderSendPoint)
    public CommandMessage orderMsgDog(@Payload String message) throws Exception {
        //CommandMessage cmdMsg = message.getPayload().getClass().toString();
        trclog.log(Level.INFO,"Received on point "+CommandOrderPoint+": "+message);//.getPayload().toString());
        Gson gson = new Gson();
        CommandMessage cmd = gson.fromJson(message,CommandMessage.class);
        cmd.DeserializeOrder();
        cmd.setStatus(CommandStatus.ACCEPTED);
        OrderCommand currentCommand = cmd.getOrderCommand();
        if(cmd.getCommand() == Commands.ORDERCOMMAND){
            switch(currentCommand){
                case PLACE:
                    //trclog.log(Level.INFO,"Exiting "+cmd.getOrder().toString());
                    //System.exit(1);
                    trclog.log(Level.INFO,"Sending order "+cmd.getOrder().toString());
                    exchangeConnector.SendOrder(cmd.getOrder());
                    cmd.setStatus(CommandStatus.EXECUTED);
                    break;
                case CANCEL:
                    cmd.setStatus(CommandStatus.EXECUTED);
                    break;
                case CANCELL_ALL:
                    cmd.setStatus(CommandStatus.EXECUTED);
                    break;
                case REQUEST_STATUS:
                    cmd.setStatus(CommandStatus.SENDED);
                    break;
                default:
                    cmd.setStatus(CommandStatus.REJECTED);
                    break;
            }

        }
        cmd.DeserializeOrder();
        return cmd;//"Command  "+cmd.getCommand().toString()+":"+cmd.getOrder().toString());//message.toString());
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
