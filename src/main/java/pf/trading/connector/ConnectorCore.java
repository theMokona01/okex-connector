package pf.trading.connector;

import Exchange.EConnector;
import classes.WebSocket.ServerWSApplication;
import classes.WebSocket.ServerWSController;
import classes.WebSocket.messages.BBOMessage;
import classes.WebSocket.repository.TickerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectorCore {

    private static Logger trclog = Logger.getLogger(ConnectorCore.class.getName());
    public static void main(String[] args) {
        trclog.log(Level.INFO,"Connector started");


        //Spring boot properties
        Map<String, Object> CoreServerProperties = new HashMap<>();
        //CoreServerProperties.put("server.port","8081");
        //Initialize client UI

        SpringApplication ClientWSApp = new SpringApplication(ServerWSApplication.class);

        ApplicationContext WSAppContext = ClientWSApp.run(args);
        //Testing Spring
        logLoadedBeans(WSAppContext);

        //Get websocket server instance
        ServerWSController RelationWSController = (ServerWSController)WSAppContext.getBean("serverWSController");

        //Initialize Exchange connection
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "applicationContext.xml"
        );

        EConnector exchangeConnector = (EConnector) context.getBean("ExchangeConnector");
        exchangeConnector.Connector.setWsController(RelationWSController);
        //context.close();

        //Bing exchange connector to ServerWSController
        RelationWSController.setExchangeConnector(exchangeConnector);


    }

    public static void logLoadedBeans(ApplicationContext AppContext){
        String[] beans = AppContext.getBeanDefinitionNames();
        Arrays.sort(beans);
        for (String bean : beans) {
            trclog.log(Level.INFO,"Loaded bean "+bean);
        }
    }
}
