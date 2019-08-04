package classes.WebSocket;

import classes.WebSocket.repository.TickerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackageClasses = {TickerRepository.class,TickerRepository.class})
public class ServerWSApplication{
    @Autowired
    private ApplicationContext appContext;

    @Autowired
    TickerRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(ServerWSApplication.class, args);
    }

}
