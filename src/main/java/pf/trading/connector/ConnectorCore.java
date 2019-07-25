package pf.trading.connector;

import classes.WebSocket.ServerWSApplication;
import classes.WebSocket.ServerWSController;
import classes.WebSocket.messages.PricesMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ConnectorCore {
    public static void main(String[] args) {
        System.out.println("Connector started");

        //Spring boot properties
        Map<String, Object> pro = new HashMap<>();
        pro.put("log4j.logger.org.springframework", "INFO");
        pro.put("server.port","8081");
        //Initialize client UI

        SpringApplication ClientWSApp = new SpringApplication(ServerWSApplication.class);
        ClientWSApp.setDefaultProperties(pro);
        ApplicationContext WSAppContext = ClientWSApp.run(args);
        //Testing Spring
        String[] beans = WSAppContext.getBeanDefinitionNames();
        Arrays.sort(beans);
        System.out.println("Loaded beans ");
        ServerWSController RelationWSController = (ServerWSController)WSAppContext.getBean("serverWSController");
        while(true) {
            RelationWSController.SendPricePointMessage(new PricesMessage());
            try{
            Thread.sleep(1000);}catch (Exception e){}
        }
        //for (String bean : beans) {
            //System.out.println("Bean :"+bean);
        //}

        //Initialize Exchange connection
        /*ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "applicationContext.xml"
        );
        context.close();*/


    }
}
