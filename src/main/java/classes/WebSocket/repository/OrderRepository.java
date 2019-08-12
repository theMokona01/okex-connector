package classes.WebSocket.repository;

import classes.Enums.OrderStatus;
import classes.Enums.OrderType;
import classes.WebSocket.model.EOrder;
import classes.WebSocket.model.Ticker;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import java.util.List;

@Repository
public interface OrderRepository extends CrudRepository<EOrder, Long> {

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<EOrder> findByExchangeId(String exchangeId);
    //@Lock(LockModeType.PESSIMISTIC_READ)
    @Transactional
    List<EOrder> deleteAllByInstructionKey(String InstructionKey);//AndUpdateTimestampLessThan(String instructionKey, long lessUpdateTimestamp);
    @Transactional
    List<EOrder> deleteAllByInitTimestamp(long initTimestamp);

    //@Retryable(maxAttempts = 10, backoff = @Backoff(delay = 100, maxDelay = 500),
    //        include = {ObjectOptimisticLockingFailureException.class, OptimisticLockException.class, DataIntegrityViolationException.class})
    @Transactional
    List<EOrder> deleteAllByStatusAndUpdateTimestampLessThan(OrderStatus orderStatus, long lessTimestamp) throws Throwable;

    List<EOrder> findAll();
    @Modifying
    @Query("UPDATE EOrder e SET e.filled = ?2 , e.executed_price = ?3, e.executed = ?4, e.updateTimestamp = ?5, e.status =?6 WHERE e.exchangeId = ?1")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    void updateFromExchange(String exchangeId, Double filled, Double executed_price, Double executed, long updadeTimeStamp, OrderStatus orderStatus);

    @Modifying
    @Query("UPDATE EOrder e SET e.instructionKey= ?2, e.strategy = ?3, e.symbol= ?4," +
            "e.exchangeSymbol = ?5, e.leftSymbol = ?6, e.rightSymbol = ?7, e.orderType = ?8, e.price =?9, e.size =?10," +
            "e.initTimestamp =?11 WHERE e.exchangeId = ?1")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    void updateFromClient(String exchangeId, String instructionKey, String strategy, String symbol, String exchangeSymbol, String LeftSymbol, String RightSymbol,
                          OrderType orderType, double price, double size,
                          long initTimestamp, long updateTimestamp);


    @Modifying
    //@Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("DELETE from EOrder e WHERE e.updateTimestamp < ?1 and e.instructionKey=''")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    void cleanOldTrashOrders(long deleteBeforeTimestamp);



}
