package pf.trading.connector;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ConnectorCore {
    public static void main(String[] args) {
        System.out.println("Connector started");

        //Initialize Exchange connection
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "applicationContext.xml"
        );

        context.close();


        //Initialize client UI

    }
}
