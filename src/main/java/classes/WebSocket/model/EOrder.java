package classes.WebSocket.model;

import javax.persistence.*;

@Entity
@Table(name = "eorder")
public class EOrder {
    //check if sender and receiver of object have loaded classes for object
    private static final long serialVersionUID = -2343243243242432341L;
    //run time stratey design
    @Id
    //configure increment of specified db column
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    //column names
    @Column(name = "instructionid")
    private String instructionId;

    @Column(name = "exchangeid")
    private String exchangeId;

    @Column(name = "internalid")
    private String internalId;

    @Column(name = "price")
    private Double price;

    @Column(name = "size")
    private Double size;

    public EOrder(String instructionId, String exchangeId, String internalId, Double price, Double size) {
        this.instructionId = instructionId;
        this.exchangeId = exchangeId;
        this.internalId = internalId;
        this.price = price;
        this.size = size;
    }

    @Override
    public String toString(){
        return String.format("EOrder[id='%d', instructionid='%s', exchangeid='%s', internalid='%s', price='%d', size='%d']", id, instructionId, exchangeId, internalId, price, size);
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

    public String getInstructionId() {
        return instructionId;
    }

    public void setInstructionId(String instructionId) {
        this.instructionId = instructionId;
    }

    public String getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(String exchangeId) {
        this.exchangeId = exchangeId;
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
}
