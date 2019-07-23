package interfaces;

public interface Order {
    Order InitOrder(String Exchange,String Symbol, Double Size, Double Price, String Side);
}
