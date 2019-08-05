package classes.WebSocket.repository;

import classes.WebSocket.model.EExecution;
import classes.WebSocket.model.EOrder;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ExecutionRepository extends CrudRepository<EExecution, Long> {
    List<EExecution> findByExecutionId(String executionId);
    List<EExecution> findAll();

}
