package advisor.services;

import advisor.model.AppConfig;
import advisor.model.Session;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import static java.lang.Thread.sleep;

public class SessionService {
    private final AppConfig config;
    private final Session session;

    public SessionService(Session session, AppConfig config) {
        this.session = session;
        this.config = config;
    }

    public void createCallbackServer() {

        try {
            HttpServer server = HttpServer.create();
            int PORT = (new URI(config.getREDIRECT_URI())).getPort();
            server.bind(new InetSocketAddress(PORT), 0);
            server.start();

            server.createContext("/",
                    exchange -> {
                        String query = exchange.getRequestURI().toString();
                        String response;
                        if (query.contains("code=")) {
                            session.setAuthorizationCode(query.split("=")[1]);
                            session.setValidAuthentication();
                            response = "Got the code. Return back to your program.";
                        } else {
                            response = "Authorization code not found. Try again.";
                        }
                        exchange.sendResponseHeaders(200, response.length());
                        exchange.getResponseBody().write(response.getBytes());
                        exchange.getResponseBody().close();
            });
            while (!session.isAuthenticated()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            server.stop(5);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public String requestAccessToken() {
        String credentials = config.getCLIENT_ID() + ":" + config.getCLIENT_SECRET();
        String encoded_credentials = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", encoded_credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(config.getAUTHORIZATION_SERVER() + "/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        "grant_type=" + "authorization_code"
                                + "&code=" + session.getAuthorizationCode()
                                + "&redirect_uri=" + config.getREDIRECT_URI()))
                .build();
        try {

            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String accessToken = "";
            if (!response.body().isEmpty()) {
                JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
                if (responseJson.has("access_token")) {
                    accessToken = responseJson.get("access_token").getAsString();
                }
            }
            return accessToken;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getResource(String resource) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest requestToken = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + session.getAccessToken())
                .uri(URI.create(config.getAPI_SERVER()+ "/v1/browse/" +  resource))
                .GET()
                .build();

        StringBuilder response = new StringBuilder();
        try {
            HttpResponse<String> apiResponse = client.send(requestToken, HttpResponse.BodyHandlers.ofString());
            if (apiResponse.statusCode() == 200)
                response.append(apiResponse.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return response.toString();
    }
}
