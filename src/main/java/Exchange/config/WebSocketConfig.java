package Exchange.config;

public class WebSocketConfig {

    public static void loginConnect(WebSocketClient webSocketClient, String url, String api_key, String passphrase, String secret_key) {
        webSocketClient.connection(url);
        webSocketClient.login(url, api_key, passphrase, secret_key);
    }
}
