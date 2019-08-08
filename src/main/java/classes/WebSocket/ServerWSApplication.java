package classes.WebSocket;

import classes.WebSocket.repository.ExecutionRepository;
import classes.WebSocket.repository.OrderRepository;
import classes.WebSocket.repository.TickerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
//@EnableJpaRepositories(basePackageClasses = {TickerRepository.class, OrderRepository.class})
public class ServerWSApplication{
    @Autowired
    private ApplicationContext appContext;

    @Autowired
    TickerRepository repository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    ExecutionRepository executionRepository;


    public static void main(String[] args) {
        SpringApplication.run(ServerWSApplication.class, args);
    }

}
