package classes.WebSocket.repository;

import classes.WebSocket.model.EOrder;
import classes.WebSocket.model.Ticker;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface OrderRepository extends CrudRepository<EOrder, Long> {
    List<EOrder> findByInstructionId(String instrumentId);
    List<EOrder> findByExchangeId(String exchangeId);
    List<EOrder> findAll();
    @Modifying
    @Query("UPDATE EOrder e SET e.price = ?2 WHERE e.instructionId = ?1")
    @Transactional
    void updateFromExchange(String instructionId, Double price);

    @Modifying
    @Query("UPDATE EOrder e SET e.size = ?2 WHERE e.instructionId = ?1")
    @Transactional
    void updateFromClient(String instructionId, Double size);

}
