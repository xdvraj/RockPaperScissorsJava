package com.rps;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

public class App {
    private static final int PORT = 8080;
    private static final List<String> MOVES = List.of("rock", "paper", "scissors");
    private static final Gson GSON = new Gson();
    private static final Random RANDOM = new Random();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/play", new PlayHandler());
        server.createContext("/", new StaticHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("RPS running at http://localhost:" + PORT);
    }

    static class PlayHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonObject request = GSON.fromJson(body, JsonObject.class);
            String playerMove = request != null && request.has("move")
                ? request.get("move").getAsString().toLowerCase()
                : "";

            if (!MOVES.contains(playerMove)) {
                sendJson(exchange, 400, "{\"error\":\"Invalid move\"}");
                return;
            }

            String computerMove = MOVES.get(RANDOM.nextInt(MOVES.size()));
            String result = decideWinner(playerMove, computerMove);

            JsonObject response = new JsonObject();
            response.addProperty("playerMove", playerMove);
            response.addProperty("computerMove", computerMove);
            response.addProperty("result", result);

            sendJson(exchange, 200, GSON.toJson(response));
        }
    }

    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path)) {
                path = "/index.html";
            }

            String resourcePath = "web" + path;
            try (InputStream resource = App.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (resource == null) {
                    sendText(exchange, 404, "Not found", "text/plain");
                    return;
                }

                byte[] bytes = resource.readAllBytes();
                sendBytes(exchange, 200, bytes, contentType(path));
            }
        }
    }

    private static String decideWinner(String player, String computer) {
        if (player.equals(computer)) {
            return "draw";
        }
        boolean playerWins =
            ("rock".equals(player) && "scissors".equals(computer)) ||
            ("paper".equals(player) && "rock".equals(computer)) ||
            ("scissors".equals(player) && "paper".equals(computer));
        return playerWins ? "win" : "lose";
    }

    private static String contentType(String path) {
        if (path.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (path.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        return "text/html; charset=utf-8";
    }

    private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        sendText(exchange, status, body, "application/json; charset=utf-8");
    }

    private static void sendText(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        sendBytes(exchange, status, body.getBytes(StandardCharsets.UTF_8), contentType);
    }

    private static void sendBytes(HttpExchange exchange, int status, byte[] bytes, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }
}
