package classes.WebSocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

@SpringBootApplication
public class ServerWSApplication{
    @Autowired
    private ApplicationContext appContext;
    public static void main(String[] args) {
        SpringApplication.run(ServerWSApplication.class, args);
    }
}
