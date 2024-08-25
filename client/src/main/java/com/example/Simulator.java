package com.example;

import com.example.autogenerated.ObpfNativeInterface;
import com.example.autogenerated.ObpfTetromino;
import com.example.autogenerated.ObpfVec2;
import com.example.network.GameServerConnection;
import com.example.network.ServerMessage;
import com.example.ui.Mino;
import com.example.ui.Tetrion;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

record PlayerInfo(int playerId, Tetrion tetrion, MemorySegment obpfTetrion) {
}

public class Simulator {
    private final List<Tetrion> tetrions;
    private final Map<Integer, PlayerInfo> players = new LinkedHashMap<>();

    // calculate when to simulate next frame
    private long lastSimulated = System.nanoTime();
    private long frame = 1;

    // Multiplayer Network
    private GameServerConnection conn;
    private final List<int[]> keyStatesBuffer = new ArrayList<>(16);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean[] keysPressed = Stream.generate(AtomicBoolean::new).limit(7).toArray(AtomicBoolean[]::new);

    private long seed;
    private int clientId;

    private final CountDownLatch gameFinished = new CountDownLatch(1);

    public Simulator(List<Tetrion> tetrions) {
        this.tetrions = tetrions;
    }

    public void startSimulating(GameServerConnection conn) {
        this.conn = conn;
        CompletableFuture.runAsync(conn::connect, Executors.newVirtualThreadPerTaskExecutor());

        var msg = conn.waitForMessage();
        if (msg instanceof ServerMessage.GameStartMessage gameStartMessage) {
            seed = gameStartMessage.seed();
            clientId = gameStartMessage.clientId();
            // unused for now
            var _ = gameStartMessage.startFrame();

            var obpfTetrion = ObpfNativeInterface.obpf_create_tetrion(seed);
            players.put(clientId, new PlayerInfo(clientId, tetrions.getFirst(), obpfTetrion));
            running.set(true);
        }
        try (var executor = Executors.newScheduledThreadPool(2)) {
            executor.scheduleAtFixedRate(this::simulate, 0, 1, TimeUnit.MILLISECONDS);
            executor.scheduleAtFixedRate(this::readServerMessages, 0, 1, TimeUnit.MILLISECONDS);
            gameFinished.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void readServerMessages() {
        var msg = conn.pollMessage();
        if (msg.isEmpty()) return;

        var stateBroadcastMessage = (ServerMessage.StateBroadcastMessage) msg.get();
        updateOtherTetrions(stateBroadcastMessage.messageFrame(), stateBroadcastMessage.clientStates());
    }

    public void stopSimulating() {
        running.set(false);
        gameFinished.countDown();
        if (conn != null) {
            conn.stop();
        }
    }

    public void setKeyState(int key, boolean isPressed) {
        if (!running.get()) return;
        keysPressed[key].set(isPressed);
    }

    private void simulate() {
        if (running.get()) {
            var now = System.nanoTime();
            if (now - lastSimulated > 16_000_000) {
                var client = players.get(clientId);
                client.tetrion().setCurrentFrame(frame);

                keyStatesBuffer.add(Stream.of(keysPressed).mapToInt(b -> b.get() ? 1 : 0).toArray());
                MemorySegment keyState = createKeyState(keysPressed);
                ObpfNativeInterface.obpf_tetrion_simulate_next_frame(client.obpfTetrion(), keyState);
                lastSimulated = now;

                client.tetrion().update(gameBoard -> {
                    gameBoard.clear();
                    fillGameBoard(client.obpfTetrion(), gameBoard);
                });

                if (frame % 15 == 0) {
                    conn.addHeartbeatMessage(new ServerMessage.HeartbeatMessage(frame, List.copyOf(keyStatesBuffer)));
                    keyStatesBuffer.clear();
                }
                frame++;
            }
        }
    }

    private void updateOtherTetrions(long frame, LinkedHashMap<Integer, List<Integer>> clientsKeyStates) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (var entry : clientsKeyStates.entrySet()) {
                if (entry.getKey() != clientId) {
                    var playerId = entry.getKey();
                    var keyStates = entry.getValue();
                    var player = players.computeIfAbsent(playerId, _ -> {
                        var tetrion = ObpfNativeInterface.obpf_create_tetrion(seed);
                        return new PlayerInfo(playerId, tetrions.get(players.size()), tetrion);
                    });
                    player.tetrion().setCurrentFrame(frame);
                    executor.execute(() -> {
                        var timeStart = System.nanoTime();
                        var frameTime = TimeUnit.MILLISECONDS.toNanos((long) (1 / 60.0 * 1000.0));
                        for (int i = 0; i < keyStates.size(); i++) {
                            var encoded = keyStates.get(i);
                            var keyState = createKeyState(decodeKeyState(encoded));
                            ObpfNativeInterface.obpf_tetrion_simulate_next_frame(player.obpfTetrion(), keyState);
                            player.tetrion().update(gameBoard -> {
                                gameBoard.clear();
                                fillGameBoard(player.obpfTetrion(), gameBoard);
                            });
                            try {
                                // calculate remaining time until next frame should be simulated
                                var sleepTime = Math.max(0, timeStart + i * frameTime - System.nanoTime());
                                TimeUnit.NANOSECONDS.sleep(sleepTime);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            }
        }
    }

    private AtomicBoolean[] decodeKeyState(Integer encoded) {
        var keyStates = new AtomicBoolean[7];
        for (int i = 0; i < 7; i++) {
            keyStates[i] = new AtomicBoolean((encoded & (1 << i)) != 0);
        }
        return keyStates;
    }

    private void fillGameBoard(MemorySegment obpfTetrion, List<Mino> gameBoard) {
        // matrix
        try (var arena = Arena.ofConfined()) {
            var position = ObpfVec2.allocate(arena);
            for (byte y = 0; y < Tetrion.ROWS; y++) {
                for (byte x = 0; x < Tetrion.COLS; x++) {
                    ObpfVec2.x(position, x);
                    ObpfVec2.y(position, y);
                    var type = ObpfNativeInterface.obpf_tetrion_matrix_get(obpfTetrion, position);
                    if (type != 0) {
                        gameBoard.add(new Mino(x, y, type, false));
                    }
                }
            }
        }
        // active piece
        try (var arena = Arena.ofConfined()) {
            var activeTetromino = ObpfTetromino.allocate(arena);
            ObpfNativeInterface.obpf_tetrion_try_get_active_tetromino(obpfTetrion, activeTetromino);
            var type = ObpfTetromino.type(activeTetromino);
            if (type != 0) {
                addMinos(activeTetromino, type, false, gameBoard);
            }
        }

        // ghost piece
        try (var arena = Arena.ofConfined()) {
            var ghostTetromino = ObpfTetromino.allocate(arena);
            ObpfNativeInterface.obpf_tetrion_try_get_ghost_tetromino(obpfTetrion, ghostTetromino);
            var type = ObpfTetromino.type(ghostTetromino);
            if (type != 0) {
                addMinos(ghostTetromino, type, true, gameBoard);
            }
        }
    }

    private void addMinos(MemorySegment tetromino, int type, boolean ghostPiece, List<Mino> gameBoard) {
        for (int i = 0; i < ObpfTetromino.mino_positions$dimensions()[0]; i++) {
            var vec2 = ObpfTetromino.mino_positions(tetromino, i);
            int x = ObpfVec2.x(vec2);
            int y = ObpfVec2.y(vec2);
            gameBoard.add(new Mino(x, y, type, ghostPiece));
        }
    }

    private MemorySegment createKeyState(AtomicBoolean[] keysPressed) {
        return ObpfNativeInterface.obpf_key_state_create(Arena.ofAuto(),
                keysPressed[0].get(),
                keysPressed[1].get(),
                keysPressed[2].get(),
                keysPressed[3].get(),
                keysPressed[4].get(),
                keysPressed[5].get(),
                keysPressed[6].get());
    }
}