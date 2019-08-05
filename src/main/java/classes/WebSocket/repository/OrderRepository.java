package classes.WebSocket.repository;

import classes.WebSocket.model.EOrder;
import classes.WebSocket.model.Ticker;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderRepository extends CrudRepository<EOrder, Long> {
    List<EOrder> findByInstructionId(String instrumentId);
    List<EOrder> findAll();

}
