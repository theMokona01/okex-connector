package classes.WebSocket.repository;

import classes.WebSocket.model.Ticker;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderRepository extends CrudRepository<Ticker, Long> {
    List<Ticker> findByInstrumentId(String instrumentId);
    List<Ticker> findAll();

}
