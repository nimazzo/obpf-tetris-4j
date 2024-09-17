package com.example.ui.controllers;

import com.example.concurrent.Worker;
import com.example.daos.CsrfToken;
import com.example.daos.LobbyCreationRequest;
import com.example.ui.ErrorMessages;
import com.example.ui.views.menu.LobbyMenu;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LobbyController {

    private final RestClient restClient;
    private final LobbyMenu lobbyMenu;

    private CsrfToken csrfToken;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LobbyController(LobbyMenu lobbyMenu) {
        this.lobbyMenu = lobbyMenu;

        var encodedAuth = Base64.getEncoder().encodeToString("user:user".getBytes(StandardCharsets.UTF_8));
        var jacksonMessageConverter =
                new MappingJackson2HttpMessageConverter();
        this.restClient = RestClient.builder()
                .requestInterceptor(new CookieInterceptor())
                .baseUrl("http://localhost:8080/")
                .defaultHeader("Authorization", "Basic " + encodedAuth)
                .defaultHeader("Content-Type", "application/json")
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

            csrfToken = Objects.requireNonNull(response.getBody());
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

    public void createNewLobby(LobbyCreationRequest request) {
        Worker.execute(() -> {
            var response = restClient
                    .post()
                    .uri("lobby/create")
                    .header(csrfToken.headerName(), csrfToken.token())
                    .body(objectMapper.writeValueAsString(request))
                    .retrieve()
                    .toEntity(String.class);

            if (response.getStatusCode().isError()) {
                ErrorMessages.showErrorMessage("HTTP Error: " + response.getStatusCode(), response.getBody());
            }

            fetchLobbies();
        });
    }

    static class CookieInterceptor implements ClientHttpRequestInterceptor {
        private final Map<String, HttpCookie> cookieStore = new HashMap<>();

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            for (var cookie : cookieStore.values()) {
                request.getHeaders().add(HttpHeaders.COOKIE, cookie.toString());
            }
            var response = execution.execute(request, body);

            var cookieHeaders = response.getHeaders().get(HttpHeaders.SET_COOKIE);
            if (cookieHeaders != null) {
                for (var cookieHeader : cookieHeaders) {
                    var cookies = HttpCookie.parse(cookieHeader);
                    for (var cookie : cookies) {
                        cookieStore.put(cookie.getName(), cookie);
                    }
                }
            }
            return response;
        }
    }
}
