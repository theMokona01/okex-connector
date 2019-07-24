package classes.WebSocket;

public class SubscriptionMessage {

    private String subscription_name;
    public SubscriptionMessage() { }
    public SubscriptionMessage(String name) {
        this.subscription_name = name;
    }
    public String getSubscription_name() {
        return subscription_name;
    }
    public void setSubscription_name(String subscription_name) {
        this.subscription_name = subscription_name;
    }
}
