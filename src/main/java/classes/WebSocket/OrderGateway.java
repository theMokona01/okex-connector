package classes.WebSocket;

import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class OrderGateway {
    //Hashmap for temp store InstructionKey and OrderId
    private HashMap<String,String> OrderGatewayMap;
    public OrderGateway() {
        OrderGatewayMap = new HashMap<>();
    }
}
