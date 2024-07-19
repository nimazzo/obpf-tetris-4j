package com.example;

import com.example.generated.ObpfEvent;
import com.example.generated.ObpfNativeInterface;
import com.example.generated.ObpfTetromino;
import com.example.generated.ObpfVec2;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Scanner;

import static com.example.generated.ObpfNativeInterface.*;

public class Main {
    static long frame = 0;

    private static byte width;
    private static byte height;
    private static MemorySegment obpfTetrion;
    private static MemorySegment obpfMatrix;

    private static char[][] board;

    public static void main(String[] args) {

        width = ObpfNativeInterface.obpf_tetrion_width();
        height = ObpfNativeInterface.obpf_tetrion_height();

        board = new char[width][height];

        System.out.println("Tetrion width: " + width);
        System.out.println("Tetrion height: " + height);

        obpfTetrion = ObpfNativeInterface.obpf_create_tetrion(1234);
        obpfMatrix = ObpfNativeInterface.obpf_tetrion_matrix(obpfTetrion);

        simulate();

        ObpfNativeInterface.obpf_destroy_tetrion(obpfTetrion);
    }

    private static void drawGhostTetromino(MemorySegment outTetromino) {
        var type = ObpfTetromino.type(outTetromino);
        if (type == 0) {
            return;
        }
        System.out.println("Ghost piece: '" + tetrominoTypeToChar(type) + "'");
        for (int i = 0; i < ObpfTetromino.mino_positions$dimensions()[0]; i++) {
            var vec2 = ObpfTetromino.mino_positions(outTetromino, i);
            var x = ObpfVec2.x(vec2);
            var y = ObpfVec2.y(vec2);
            board[x][y] = 'G';
        }
    }

    private static void simulate() {
        var scanner = new Scanner(System.in);
        while (true) {
            var input = scanner.nextLine();
            //noinspection IfCanBeSwitch
            if (input.equals("exit")) {
                break;
            } else if (input.equals("L")) {
                try (var arena = Arena.ofConfined()) {
                    var leftEventDown = ObpfEvent.allocate(arena);
                    ObpfEvent.key(leftEventDown, OBPF_KEY_LEFT());
                    ObpfEvent.type(leftEventDown, OBPF_PRESSED());
                    ObpfEvent.frame(leftEventDown, frame - 2);
                    ObpfNativeInterface.obpf_tetrion_enqueue_event(obpfTetrion, leftEventDown);
                    var leftEventUp = ObpfEvent.allocate(arena);
                    ObpfEvent.key(leftEventUp, OBPF_KEY_LEFT());
                    ObpfEvent.type(leftEventUp, OBPF_RELEASED());
                    ObpfEvent.frame(leftEventUp, frame - 1);
                    ObpfNativeInterface.obpf_tetrion_enqueue_event(obpfTetrion, leftEventUp);
                }

            } else if (input.equals("R")) {
                try (var arena = Arena.ofConfined()) {
                    var rightEventDown = ObpfEvent.allocate(arena);
                    ObpfEvent.key(rightEventDown, OBPF_KEY_RIGHT());
                    ObpfEvent.type(rightEventDown, OBPF_PRESSED());
                    ObpfEvent.frame(rightEventDown, frame - 2);
                    ObpfNativeInterface.obpf_tetrion_enqueue_event(obpfTetrion, rightEventDown);

                    var rightEventUp = ObpfEvent.allocate(arena);
                    ObpfEvent.key(rightEventUp, OBPF_KEY_RIGHT());
                    ObpfEvent.type(rightEventUp, OBPF_RELEASED());
                    ObpfEvent.frame(rightEventUp, frame - 1);
                    ObpfNativeInterface.obpf_tetrion_enqueue_event(obpfTetrion, rightEventUp);
                }
            }

            // clear board
            Arrays.stream(board).forEach(row -> Arrays.fill(row, ' '));

            ObpfNativeInterface.obpf_tetrion_simulate_up_until(obpfTetrion, frame);
            frame += 60;
            System.out.println("Frame: " + frame);
            try (var arena = Arena.ofConfined()) {
                var position = ObpfVec2.allocate(arena);
                for (var y = 0; y < height; y++) {
                    for (var x = 0; x < width; x++) {
                        ObpfVec2.x(position, (byte) x);
                        ObpfVec2.y(position, (byte) y);
                        var type = ObpfNativeInterface.obpf_matrix_get(obpfMatrix, position);
                        board[x][y] = tetrominoTypeToChar(type);
                    }
                }
            }

            drawGhostPiece();
            drawActivePiece();
            drawBoard();
        }
    }

    private static void drawActivePiece() {
        try (var arena = Arena.ofConfined()) {
            var outTetromino = ObpfTetromino.allocate(arena);
            ObpfNativeInterface.obpf_tetrion_try_get_active_tetromino(obpfTetrion, outTetromino);
            drawTetromino(outTetromino);
        }
    }

    private static void drawTetromino(MemorySegment outTetromino) {
        var type = ObpfTetromino.type(outTetromino);
        if (type == 0) {
            return;
        }
        System.out.println("Active piece: '" + tetrominoTypeToChar(type) + "'");
        for (int i = 0; i < ObpfTetromino.mino_positions$dimensions()[0]; i++) {
            var vec2 = ObpfTetromino.mino_positions(outTetromino, i);
            var x = ObpfVec2.x(vec2);
            var y = ObpfVec2.y(vec2);
            board[x][y] = tetrominoTypeToChar(type);
        }
    }

    private static void drawBoard() {
        System.out.println("-".repeat(width * 2 + 2));
        for (var y = 0; y < height; y++) {
            System.out.print("|");
            for (var x = 0; x < width; x++) {
                System.out.print(board[x][y] + " ");
            }
            System.out.print("|");
            System.out.println();
        }
        System.out.println("-".repeat(width * 2 + 2));
    }

    private static char tetrominoTypeToChar(int type) {
        return switch (type) {
            case 0 -> ' ';
            case 1 -> 'I';
            case 2 -> 'J';
            case 3 -> 'L';
            case 4 -> 'O';
            case 5 -> 'S';
            case 6 -> 'T';
            case 7 -> 'Z';
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    private static void drawGhostPiece() {
        try (var arena = Arena.ofConfined()) {
            var outTetromino = ObpfTetromino.allocate(arena);
            ObpfNativeInterface.obpf_tetrion_try_get_ghost_tetromino(obpfTetrion, outTetromino);
            drawGhostTetromino(outTetromino);
        }
    }
}