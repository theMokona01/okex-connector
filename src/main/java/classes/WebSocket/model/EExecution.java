package classes.WebSocket.model;

import javax.persistence.*;

@Entity
@Table
public class EExecution {
        //check if sender and receiver of object have loaded classes for object
        private static final long serialVersionUID = -2343243243242432341L;
        //run time stratey design
        @Id
        //configure increment of specified db column
        @GeneratedValue(strategy = GenerationType.AUTO)
        private long id;

        //column names
        @Column(name = "exchangename")
        private String exchangename;

        @Column(name = "executionid")
        private String executionId;

        @Column(name = "orderid")
        private String orderId;

        @Column(name = "internalid")
        private String internalId;

        @Column(name = "price")
        private Double price;

        @Column(name = "filled")
        private Double size;

        @Column(name = "executed")
        private Double executed;

        @Column(name = "timestamp")
        private long timestamp;

    public EExecution(String exchangename, String executionid, String orderId, String internalId, Double price, Double size, Double executed, long timestamp) {
        this.exchangename = exchangename;
        this.executionId = executionid;
        this.orderId = orderId;
        this.internalId = internalId;
        this.price = price;
        this.size = size;
        this.executed = executed;
        this.timestamp = timestamp;
    }

    public static long getSerialVersionUID() {
            return serialVersionUID;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getExchangename() {
            return exchangename;
        }

        public void setExchangename(String exchangename) {
            this.exchangename = exchangename;
        }

        public String getExecutionId() {
            return executionId;
        }

        public void setExecutionId(String executionId) {
            this.executionId = executionId;
        }

    public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

    public String getInternalId() {
            return internalId;
        }

        public void setInternalId(String internalId) {
            this.internalId = internalId;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Double getSize() {
            return size;
        }

        public void setSize(Double size) {
            this.size = size;
        }

        public Double getExecuted() {
            return executed;
        }

        public void setExecuted(Double executed) {
            this.executed = executed;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
}
