package com.example.ui.controllers;

import com.example.concurrent.Worker;
import com.example.ui.views.menu.LobbyMenu;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class LobbyController {

    private final RestClient restClient;
    private final LobbyMenu lobbyMenu;

    private String csrfToken;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LobbyController(LobbyMenu lobbyMenu) {
        this.lobbyMenu = lobbyMenu;

        var encodedAuth = Base64.getEncoder().encodeToString("user:user".getBytes(StandardCharsets.UTF_8));
        var jacksonMessageConverter =
                new MappingJackson2HttpMessageConverter();
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8080/")
                .defaultHeader("Authorization", "Basic " + encodedAuth)
                .defaultHeader("Content-Type", "application/ json")
                .messageConverters(converters -> converters.add(jacksonMessageConverter))
                .build();

        fetchCsrfToken();
    }

    private void fetchCsrfToken() {
        Worker.execute(() -> {
            var response = restClient
                    .get()
                    .uri("csrf")
                    .retrieve()
                    .toEntity(CsrfToken.class);
            csrfToken = Objects.requireNonNull(response.getBody()).token();
            System.out.println("CSRF token: " + csrfToken);
        });
    }

    public void fetchLobbies() {
        Worker.execute(() -> {
            var response = restClient
                    .get()
                    .uri("lobby")
                    .retrieve()
                    .toEntity(String.class);

            var jsonTree = objectMapper.readTree(response.getBody());
            var contentNode = jsonTree.get("content");

            lobbyMenu.setLobbies(objectMapper.readValue(contentNode.toString(), new TypeReference<>() {
            }));
        });
    }

    record CsrfToken(String parameterName, String headerName, String token) {
    }
}
