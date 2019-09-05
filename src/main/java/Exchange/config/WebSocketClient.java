package Exchange.config;

import com.alibaba.fastjson.JSONArray;
import com.okcoin.commons.okex.open.api.config.APIConfiguration;
import com.okcoin.commons.okex.open.api.enums.CharsetEnum;
import com.okcoin.commons.okex.open.api.utils.DateUtils;

import okhttp3.*;
import okio.ByteString;
import org.apache.commons.compress.compressors.deflate64.Deflate64CompressorInputStream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebSocketClient {

    private static WebSocket webSocket = null;
    private static Boolean flag = false;
    private static Boolean isConnect = false;
    private static String sign;

    private String Exchange;

    private static OkHttpClient client = new OkHttpClient();

    private static final Logger trclog = Logger.getLogger(WebSocketClient.class.getName());

    public static WebSocket connection(final String url) {

        Request request = new Request.Builder()
                .url(url)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            ScheduledExecutorService service;

            @Override
            public void onOpen(final WebSocket webSocket, final Response response) {
                isConnect = true;
                //server connected
                Runnable runnable = new Runnable() {
                    public void run() {
                        try
                        {
                            while (true)
                            {
                                //wsController.cleanOldTrashOrders(10);
                                Thread.sleep(2000);
                            }
                        }
                        catch (Exception e)
                        {
                            trclog.log(Level.WARNING,e.getMessage());
                            e.printStackTrace();
                        }
                        //sendMessage("ping");
                    }
                };
                service = Executors.newSingleThreadScheduledExecutor();
                service.scheduleAtFixedRate(runnable, 25, 25, TimeUnit.SECONDS);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                System.out.println("Connection is about to disconnect！");
                webSocket.close(1000, "Long time no message was sent or received！");
                webSocket = null;
            }

            @Override
            public void onClosed(final WebSocket webSocket, final int code, final String reason) {
                System.out.println("Connection dropped！");
            }

            @Override
            public void onFailure(final WebSocket webSocket, final Throwable t, final Response response) {
                System.out.println("Connection failed,Please reconnect!");
                if (Objects.nonNull(service)) {
                    service.shutdown();
                }
            }


            @Override
            public void onMessage(final WebSocket webSocket, final ByteString bytes) {
                String s = uncompress(bytes.toByteArray());
                isLogin(s);
                System.out.println(Instant.now().toString() + " Message received: "+s);
            }
        });
        return webSocket;
    }

    private static String uncompress(final byte[] bytes) {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
             final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
             final Deflate64CompressorInputStream zin = new Deflate64CompressorInputStream(in)) {
            final byte[] buffer = new byte[1024];
            int offset;
            while (-1 != (offset = zin.read(buffer))) {
                out.write(buffer, 0, offset);
            }
            return out.toString();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void isLogin(String s) {
        if (null != s && s.contains("login")) {
            if (s.endsWith("true}")) {
                flag = true;
            }
        }
    }

    private static String sha256_HMAC(String message, String secret) {
        String hash = "";
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(CharsetEnum.UTF_8.charset()), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(message.getBytes(CharsetEnum.UTF_8.charset()));
            hash = Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            System.out.println("Error HmacSHA256 ===========" + e.getMessage());
        }
        return hash;
    }

    private static String listToJson(List<String> list) {
        JSONArray jsonArray = new JSONArray();
        for (String s : list) {
            jsonArray.add(s);
        }
        return jsonArray.toJSONString();
    }

    public static void login(String url, String apiKey, String passphrase, String secretKey) {


        //For Websocket
        String timestamp = (Double.parseDouble(DateUtils.getEpochTime()) + 28800) + "";
        String message = timestamp + "GET" + "/users/self/verify";
        sign = sha256_HMAC(message, secretKey);
        String str = "{\"op\"" + ":" + "\"login\"" + "," + "\"args\"" + ":" + "[" + "\"" + apiKey + "\"" + "," + "\"" + passphrase + "\"" + "," + "\"" + timestamp + "\"" + "," + "\"" + sign + "\"" + "]}";
        sendMessage(str);
    }

    public static void subscribe(List<String> list) {

        try {
            String s = listToJson(list);
            String str = "{\"op\": \"subscribe\", \"args\":" + s + "}";
            if (null != webSocket)
                sendMessage(str);
        } catch(Exception e){
            trclog.log(Level.WARNING,e.getMessage());
            e.printStackTrace();

        }
    }

    public static void unsubscribe(List<String> list) {
        try {
            String s = listToJson(list);
            String str = "{\"op\": \"unsubscribe\", \"args\":" + s + "}";
            if (null != webSocket)
                sendMessage(str);
        } catch(Exception e){
            trclog.log(Level.WARNING,e.getMessage());
            e.printStackTrace();
        }
    }

    private static void sendMessage(String s) {
        if (null != webSocket) {
            try {
                Thread.sleep(1300);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (s.contains("account") || s.contains("position") || s.contains("order")) {
                if (!flag) {
                    System.out.println("Channels contain channels that require login privileges to operate. Please login and operate again！");
                    return;
                }
            }
            try {
                System.out.println(Instant.now().toString() + " Message sent: " + s);
                webSocket.send(s);
            }catch(Exception e){
                trclog.log(Level.WARNING,e.getMessage());
                e.printStackTrace();
            }

        } else {
            System.out.println("Please establish the connection before you operate it！");
        }
    }

    public static void closeConnection() {
        if (null != webSocket) {
            webSocket.close(1000, "User actively closes the connection");
        } else {
            System.out.println("Please establish the connection before you operate it！");
        }
    }

    public boolean getIsLogin() {
        return flag;
    }

    public boolean getIsConnect() {
        return isConnect;
    }

}
