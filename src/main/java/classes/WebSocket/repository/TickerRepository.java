package classes.WebSocket.repository;



import classes.WebSocket.model.Ticker;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;

public interface TickerRepository extends CrudRepository<Ticker, Long> {
    List<Ticker> findByInstrumentId(String instrumentId);
    List<Ticker> findAll();

}
