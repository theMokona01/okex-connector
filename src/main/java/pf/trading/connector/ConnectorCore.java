package pf.trading.connector;

import classes.WebSocket.ServerWSApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;

public class ConnectorCore {
    public static void main(String[] args) {
        System.out.println("Connector started");

        //Initialize client UI
        ApplicationContext WSAppContext = SpringApplication.run(ServerWSApplication.class, args);
        //Testing Spring
        String[] beans = WSAppContext.getBeanDefinitionNames();
        Arrays.sort(beans);
        System.out.println("Loaded beans ");
        for (String bean : beans) {
            System.out.println("Bean :"+bean);
        }

        //Initialize Exchange connection
        //ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
        //        "applicationContext.xml"
        //);
        //context.close();


    }
}
