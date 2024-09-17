package com.example.ui.controllers;

import com.example.concurrent.Worker;
import com.example.daos.CsrfToken;
import com.example.daos.Lobby;
import com.example.daos.LobbyCreationRequest;
import com.example.daos.UserInfo;
import com.example.network.ConnectionInfo;
import com.example.simulation.GameMode;
import com.example.state.AppState;
import com.example.state.GameState;
import com.example.ui.ErrorMessages;
import com.example.ui.SceneManager;
import com.example.ui.views.menu.LobbyMenu;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LobbyController {

    private final RestClient restClient;
    private final LobbyMenu lobbyMenu;
    private final SceneManager sceneManager;
    private final GameController gameController;

    private CsrfToken csrfToken;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final SimpleObjectProperty<UserInfo> userInfo = new SimpleObjectProperty<>(null);
    private String encodedCredentials;

    public LobbyController(LobbyMenu lobbyMenu, SceneManager sceneManager, GameController gameController) {
        this.lobbyMenu = lobbyMenu;
        this.sceneManager = sceneManager;
        this.gameController = gameController;

        this.restClient = RestClient.builder()
                .requestInterceptors(interceptors -> {
                    interceptors.add(new CookieInterceptor());
                    interceptors.add(new AuthInterceptor());
                })
                .baseUrl("http://localhost:8080/")
                .defaultHeader("Content-Type", "application/json")
                .defaultStatusHandler(new ErrorResponseHandler())
                .messageConverters(converters -> converters.add(new MappingJackson2HttpMessageConverter()))
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

    public void joinLobby(Lobby lobby) {
        if (isAuthenticated()) {
            Worker.execute(() -> {
                var response = restClient
                        .post()
                        .uri("lobby/join/{lobbyId}", lobby.id())
                        .header(csrfToken.headerName(), csrfToken.token())
                        .retrieve()
                        .toEntity(ConnectionInfo.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    Platform.runLater(() -> gameController.joinMultiplayerGame(lobby, response.getBody()));
                }
            });
        }
    }

    public void leaveLobby() {
        var lobbyId = GameState.INSTANCE.getLobby().id();
        var gameResult = GameState.INSTANCE.getGameStats();
        if (isAuthenticated()) {
            Worker.execute(() -> restClient
                    .post()
                    .uri("lobby/leave/{lobbyId}", lobbyId)
                    .header(csrfToken.headerName(), csrfToken.token())
                    .body(objectMapper.writeValueAsString(gameResult))
                    .retrieve()
                    .toBodilessEntity());
        }
    }

    public void fetchLobbies() {
        if (isAuthenticated()) {
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
    }

    public void createNewLobby(LobbyCreationRequest request) {
        if (isAuthenticated()) {
            Worker.execute(() -> {
                restClient
                        .post()
                        .uri("lobby/create")
                        .header(csrfToken.headerName(), csrfToken.token())
                        .body(objectMapper.writeValueAsString(request))
                        .retrieve()
                        .toEntity(String.class);

                fetchLobbies();
            });
        }
    }

    private boolean isAuthenticated() {
        if (csrfToken == null) {
            fetchCsrfToken();
        }

        if (encodedCredentials == null) {
            return login() != null;
        }
        return true;
    }

    public UserInfo login() {
        encodedCredentials = lobbyMenu.askForCredentials();

        if (encodedCredentials != null) {
            userInfo.set(tryFetchUserInfo());
        }
        return userInfo.get();
    }

    private UserInfo tryFetchUserInfo() {
        var response = restClient.get()
                .uri("/user")
                .retrieve()
                .toEntity(UserInfo.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        return null;
    }

    public void returnToMainMenu() {
        sceneManager.switchAppState(AppState.MAIN_MENU);
    }

    public void logout() {
        if (GameState.INSTANCE.isRunning() && GameState.INSTANCE.getGameMode() == GameMode.MULTIPLAYER) {
            ErrorMessages.showErrorMessage("Cannot logout", "You are in a multiplayer game.");
            return;
        }
        userInfo.set(null);
        encodedCredentials = null;
    }

    public SimpleObjectProperty<UserInfo> userInfoProperty() {
        return userInfo;
    }

    public void goToLobbyMenu() {
        sceneManager.switchAppState(AppState.LOBBIES);
        fetchLobbies();
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

    private class AuthInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            var headers = request.getHeaders();
            if (encodedCredentials != null) {
                headers.setBasicAuth(encodedCredentials);
            }
            return execution.execute(request, body);
        }
    }

    private class ErrorResponseHandler implements ResponseErrorHandler {

        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return response.getStatusCode().isError();
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                ErrorMessages.showErrorMessage("Unauthorized", "Credentials are invalid");
                encodedCredentials = null;
                Platform.runLater(() -> sceneManager.switchAppState(AppState.MAIN_MENU));
            } else {
                ErrorMessages.showErrorMessage("HTTP Error: " + response.getStatusCode(), "" + response.getBody());
            }
        }
    }
}
