package classes.Enums;

public enum OrderStatus {
    NEW("NEW"),
    ACCEPTED("ACCEPTED"),
    PART_FILLED("PART_FILLED"),
    FULL_FILLED("FULL_FILLED"),
    CANCELLED("CANCELLED"),
    REJECTED("REJECTED"),
    UNKNOWN("UNKNOWN");

    private String Status;

    OrderStatus(String Status) {
        this.Status = Status;
    }
}
