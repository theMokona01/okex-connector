package classes.WebSocket.repository;

import classes.Enums.OrderType;
import classes.WebSocket.model.EOrder;
import classes.WebSocket.model.Ticker;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.List;

public interface OrderRepository extends CrudRepository<EOrder, Long> {
    //List<EOrder> findByInstructionId(String instrumentId);
    //@Lock(LockModeType.PESSIMISTIC_READ)
    List<EOrder> findByExchangeId(String exchangeId);
    //@Lock(LockModeType.PESSIMISTIC_READ)
    List<EOrder> findAll();
    @Modifying
    //@Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("UPDATE EOrder e SET e.filled = ?2 WHERE e.exchangeId = ?1")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    void updateFromExchange(String instructionId, Double filled, long updadeTimeStamp);

    @Modifying
    //@Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("UPDATE EOrder e SET e.instructionKey= ?2, e.strategy = ?3, e.symbol= ?4," +
            "e.exchangeSymbol = ?5, e.leftSymbol = ?6, e.rightSymbol = ?7, e.orderType = ?8, e.price =?9, e.size =?10," +
            "e.initTimestamp =?11 WHERE e.exchangeId = ?1")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    void updateFromClient(String exchangeId, String instructionKey, String strategy, String symbol, String exchangeSymbol, String LeftSymbol, String RightSymbol,
                          OrderType orderType,  double price, double size,
                          long initTimestamp, long updateTimestamp);


    @Modifying
    //@Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("DELETE from EOrder e WHERE e.updateTimestamp < ?1 and e.instructionKey=''")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    void cleanOldTrashOrders(long deleteBeforeTimestamp);



}
