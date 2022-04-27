package me.ayunami2000.ayunextras;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HCaptchaHandler implements HttpHandler {
    private static HttpServer server = null;
    private static String secret = "";
    private static String hostname = "";
    // todo: add url-based username auto-filling
    private static final String basePage =
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "  <head>\n" +
            "    <title>captcha'd</title>\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n" +
            "    <script src=\"https://js.hcaptcha.com/1/api.js\" async defer></script>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <form action=\"\" method=\"POST\">\n" +
            "      <input type=\"text\" name=\"username\" placeholder=\"Username\" />\n" +
            "      <script>if (window.location.hash != \"\") document.querySelector(\"input[name=username]\").value = decodeURIComponent(window.location.hash.slice(1));</script>\n" +
            "      <div class=\"h-captcha\" data-sitekey=\"SITEKEYHERE\"></div>\n" +
            "      <input type=\"submit\" value=\"Verify\" />\n" +
            "    </form>\n" +
            "  </body>\n" +
            "</html>";
    private static String page = "";

    public static void create(String s, String h, String sk, int port) throws Exception {
        secret = s;
        hostname = h;
        page = basePage.replace("SITEKEYHERE", sk);
        stop();
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/captcha", new HCaptchaHandler());
        server.setExecutor(null);
        server.start();
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public static boolean check(String key) {
        try {
            URL url = new URL("https://hcaptcha.com/siteverify");
            String postData = "response=" + key + "&secret=" + secret;

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length()));

            try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                dos.writeBytes(postData);
            }

            String json = new BufferedReader(new InputStreamReader(conn.getInputStream())).lines().collect(Collectors.joining("\n"));
            JsonObject jsonObject = (new JsonParser()).parse(json).getAsJsonObject();

            return jsonObject.get("success").getAsBoolean() && jsonObject.get("hostname").getAsString().equals(hostname);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        String response = "";
        if (t.getRequestMethod().equalsIgnoreCase("POST")) {
            StringBuilder sb = new StringBuilder();
            InputStream ios = t.getRequestBody();
            int i;
            while ((i = ios.read()) != -1) {
                sb.append((char) i);
            }
            Map<String, String> params = parseQuery(sb.toString(), 5); // 2 extra, to be safe
            if (params.containsKey("username") && params.containsKey("h-captcha-response")) {
                String uname = params.get("username");
                String key = params.get("h-captcha-response");
                Player player = Bukkit.getPlayerExact(uname);
                if (player != null && player.isOnline() && AyunExtras.INSTANCE.captchas.contains(player.getName())) {
                    if (check(key)) {
                        AyunExtras.INSTANCE.captchas.remove(player.getName());
                        player.sendMessage("You are now verified!");
                        Bukkit.getScheduler().scheduleSyncDelayedTask(AyunExtras.INSTANCE, () -> player.setGameMode(GameMode.CREATIVE));
                        Bukkit.broadcastMessage("§e" + player.getName() + "§e verified and joined the game");
                    }
                }
            }
            response = "Captcha received! Check back in-game if you did it correctly.";
        } else if (t.getRequestMethod().equalsIgnoreCase("GET")) {
            response = page;
        } else {
            t.close();
        }
        t.getResponseHeaders().set("Content-Type", "text/html");
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public static Map<String, String> parseQuery(String query, int maxQuerySize) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String[] pairs = query.split("&", maxQuerySize);
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx == -1) return query_pairs;
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }
}
