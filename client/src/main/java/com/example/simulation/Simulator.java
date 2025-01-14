package com.example.simulation;

import com.example.autogenerated.*;
import com.example.concurrent.Task;
import com.example.network.ServerConnection;
import com.example.network.ServerMessage;
import com.example.state.GameState;
import com.example.ui.views.game.Tetrion;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Simulator {
    private static final Logger log = Logger.getLogger(Simulator.class.getName());

    private final List<Tetrion> tetrions;
    private final Map<Integer, PlayerInfo> players = new LinkedHashMap<>();

    // calculate when to simulate next frame
    private long startTime;
    private long pauseTime = 0;
    private long frame = 1;

    // Multiplayer Network
    private ServerConnection conn;
    private final List<int[]> keyStatesBuffer = new ArrayList<>(16);
    private final AtomicBoolean[] keysPressed = Stream.generate(AtomicBoolean::new).limit(7).toArray(AtomicBoolean[]::new);

    private long seed;
    private int clientId;

    private CountDownLatch gameFinished = new CountDownLatch(1);

    public Simulator(List<Tetrion> tetrions) {
        this.tetrions = tetrions;
    }

    public void startSimulating(ServerConnection conn) {
        resetState();

        this.conn = conn;
        conn.setSimulator(this);


        Task.runOnWorkerThread(conn::connect);

        var msg = conn.waitForMessage();
        if (msg instanceof ServerMessage.GameStartMessage gameStartMessage) {
            seed = gameStartMessage.seed();
            clientId = gameStartMessage.clientId();
            // unused for now
            var _ = gameStartMessage.startFrame();

            var obpfTetrion = ObpfNativeInterface.obpf_create_tetrion(seed);

            var handler = ObpfActionHandler.allocate(this::onAction, Arena.global());
            ObpfNativeInterface.obpf_tetrion_set_action_handler(obpfTetrion, handler, MemorySegment.NULL);

            players.put(clientId, new PlayerInfo(clientId, tetrions.getFirst(), obpfTetrion));
            GameState.INSTANCE.setIsRunning(true);
        }
        try (var executor = Executors.newScheduledThreadPool(2)) {
            startTime = System.currentTimeMillis();
            executor.scheduleAtFixedRate(this::simulate, 0, 1, TimeUnit.MILLISECONDS);
            executor.scheduleAtFixedRate(this::readServerMessages, 0, 1, TimeUnit.MILLISECONDS);
            gameFinished.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopSimulating() {
        GameState.INSTANCE.setIsRunning(false);
        gameFinished.countDown();
        if (conn != null) {
            conn.stop();
            conn = null;
        }
    }

    public void setKeyState(int key, boolean isPressed) {
        if (!GameState.INSTANCE.isRunning()) return;
        keysPressed[key].set(isPressed);
    }

    private void resetState() {
        players.clear();
        frame = 1;
        keyStatesBuffer.clear();
        for (AtomicBoolean atomicBoolean : keysPressed) {
            atomicBoolean.set(false);
        }
        gameFinished = new CountDownLatch(1);
    }

    private void onAction(int id, MemorySegment userData) {
        var action = Actions.fromId(id);
        System.err.println("Action callback called with action: " + action);
    }

    private void readServerMessages() {
        var msg = conn.pollMessage();
        if (msg.isEmpty()) return;

        var stateBroadcastMessage = (ServerMessage.StateBroadcastMessage) msg.get();
        updateOtherTetrions(stateBroadcastMessage.messageFrame(), stateBroadcastMessage.clientStates());
    }

    private void simulate() {
        if (GameState.INSTANCE.isRunning()) {
            var frameToSimulate = (int) ((System.currentTimeMillis() - startTime - pauseTime) / (1000.0 / 60.0));
            while (frameToSimulate > frame) {
                var client = players.get(clientId);

                if (ObpfNativeInterface.obpf_tetrion_is_game_over(client.obpfTetrion())) {
                    client.tetrion().setGameOver();
                }

                keyStatesBuffer.add(Stream.of(keysPressed).mapToInt(b -> b.get() ? 1 : 0).toArray());
                MemorySegment keyState = createKeyState(keysPressed);
                ObpfNativeInterface.obpf_tetrion_simulate_next_frame(client.obpfTetrion(), keyState);

                if (frame % 15 == 0) {
                    conn.addHeartbeatMessage(new ServerMessage.HeartbeatMessage(frame, List.copyOf(keyStatesBuffer)));
                    keyStatesBuffer.clear();
                }

                if (!ObpfNativeInterface.obpf_tetrion_is_game_over(client.obpfTetrion())) {
                    client.tetrion().setCurrentFrame(frame);

                    client.tetrion().update(gameBoard -> {
                        gameBoard.clear();
                        fillGameBoard(client.obpfTetrion(), gameBoard);
                    });

                    client.tetrion().updatePreviewPieces(previewTetrominos -> {
                        previewTetrominos.clear();
                        fillPreviewTetrominos(client.obpfTetrion(), previewTetrominos);
                    });

                    var holdPiece = getHoldPiece(client.obpfTetrion());
                    client.tetrion().updateHoldPiece(holdPiece);

                    try (var allocator = Arena.ofConfined()) {
                        var stats = ObpfNativeInterface.obpf_tetrion_get_stats(allocator, client.obpfTetrion());
                        var score = ObpfStats.score(stats);
                        var linesCleared = ObpfStats.lines_cleared(stats);
                        var level = ObpfStats.level(stats);
                        var timeElapsed = System.currentTimeMillis() - startTime - pauseTime;
                        var gameStats = new Stats(score, level, linesCleared, timeElapsed);
                        GameState.INSTANCE.setGameStats(gameStats);
                        client.tetrion().updateStats(gameStats);
                    }
                }
                frame++;
            }
        }
    }

    private void fillPreviewTetrominos(MemorySegment tetrion, List<Tetromino> previewTetrominos) {
        try (var arena = Arena.ofConfined()) {
            var previewPieces = ObpfNativeInterface.obpf_tetrion_get_preview_pieces(arena, tetrion);
            for (int i = 0; i < ObpfPreviewPieces.types$dimensions()[0]; i++) {
                var type = ObpfPreviewPieces.types(previewPieces, i);
                if (type != 0) {
                    previewTetrominos.add(new Tetromino(getTetrominoOfType(type)));
                }
            }
        }
    }

    private Tetromino getHoldPiece(MemorySegment tetrion) {
        var type = ObpfNativeInterface.obpf_tetrion_get_hold_piece(tetrion);
        if (type != 0) {
            return new Tetromino(getTetrominoOfType(type));
        }
        return new Tetromino(List.of());
    }

    private static List<Mino> getTetrominoOfType(int type) {
        var rotation = ObpfNativeInterface.OBPF_ROTATION_NORTH();
        if (type == ObpfNativeInterface.OBPF_TETROMINO_TYPE_I()) {
            rotation = ObpfNativeInterface.OBPF_ROTATION_EAST();
        }
        var minos = new ArrayList<Mino>();
        try (var arena = Arena.ofConfined()) {
            var minoPositions = ObpfNativeInterface.obpf_tetromino_get_mino_positions(arena, type, rotation);
            for (int i = 0; i < ObpfMinoPositions.positions$dimensions()[0]; i++) {
                var vec2 = ObpfMinoPositions.positions(minoPositions, i);
                minos.add(new Mino(ObpfVec2.x(vec2), ObpfVec2.y(vec2), type, false));
            }
        }
        return minos;
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
                    if (ObpfNativeInterface.obpf_tetrion_is_game_over(player.obpfTetrion())) {
                        continue;
                    }

                    executor.execute(() -> {
                        var timeStart = System.nanoTime();
                        var frameTime = TimeUnit.MILLISECONDS.toNanos((long) (1 / 60.0 * 1000.0));
                        var startFrame = frame - 14;
                        for (int i = 0; i < keyStates.size(); i++) {
                            var encoded = keyStates.get(i);
                            var keyState = createKeyState(decodeKeyState(encoded));
                            ObpfNativeInterface.obpf_tetrion_simulate_next_frame(player.obpfTetrion(), keyState);
                            player.tetrion().setCurrentFrame(startFrame);

                            player.tetrion().update(gameBoard -> {
                                gameBoard.clear();
                                fillGameBoard(player.obpfTetrion(), gameBoard);
                            });

                            var holdPiece = getHoldPiece(player.obpfTetrion());
                            player.tetrion().updateHoldPiece(holdPiece);
                            player.tetrion().updatePreviewPieces(previewTetrominos -> {
                                previewTetrominos.clear();
                                fillPreviewTetrominos(player.obpfTetrion(), previewTetrominos);
                            });

                            if (ObpfNativeInterface.obpf_tetrion_is_game_over(player.obpfTetrion())) {
                                player.tetrion().setGameOver();
                                break;
                            }

                            try {
                                // calculate remaining time until next frame should be simulated
                                var sleepTime = Math.max(0, timeStart + i * frameTime - System.nanoTime());
                                TimeUnit.NANOSECONDS.sleep(sleepTime);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            startFrame++;
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

    private long lastPause;

    public void togglePause() {
        if (GameState.INSTANCE.isRunning()) {
            log.info("Pausing simulation");
            lastPause = System.currentTimeMillis();
            GameState.INSTANCE.setIsRunning(false);
        } else {
            log.info("Resuming simulation");
            pauseTime += System.currentTimeMillis() - lastPause;
            GameState.INSTANCE.setIsRunning(true);
        }
    }
}
