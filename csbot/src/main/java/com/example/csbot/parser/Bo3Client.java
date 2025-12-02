package com.example.csbot.parser;

import java.net.http.*;
import java.net.URI;
import java.time.Duration;
import com.fasterxml.jackson.databind.*;

public class Bo3Client {
    private static final String BASE = "https://api.bo3.gg/api/v1";
    private final HttpClient http;
    private final ObjectMapper mapper;

    public Bo3Client() {
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
    }

    private JsonNode getJson(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(new URI(BASE + path))
                .GET()
                .header("Accept", "application/json")
                // возможно нужны заголовки куки, авторизации etc
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("HTTP error: " + resp.statusCode());
        }
        return mapper.readTree(resp.body());
    }

    public JsonNode getLiveMatches() throws Exception {
        return getJson("/matches/");  // пример пути — нужно уточнить
    }

    public JsonNode getTeam(String teamId) throws Exception {
        return getJson("/teams/endpoint?id=" + teamId);
    }

    public JsonNode getPlayer(String playerId) throws Exception {
        return getJson("/players/" + playerId);
    }

    // добавь другие методы: getPastMatches, getTournament, getTransfers и др.
}
