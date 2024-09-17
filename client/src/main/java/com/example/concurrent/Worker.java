package com.example.concurrent;

import com.example.ui.ErrorMessages;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class Worker<T> extends Service<T> {

    private final Callable<T> callable;

    private Worker(Runnable runnable) {
        setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        setOnFailed(_ -> {
            getException().printStackTrace();
            var sw = new StringWriter();
            getException().printStackTrace(new PrintWriter(sw));
            ErrorMessages.showErrorMessage(getException().getMessage(), sw.toString());
        });
        this.callable = () -> {
            runnable.run();
            return null;
        };
    }

    public static void execute(Executable executable) {
        var worker = new Worker<>(() -> {
            try {
                executable.execute();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Platform.runLater(worker::start);
    }

    @Override
    protected Task<T> createTask() {
        return new Task<>() {
            @Override
            protected T call() throws Exception {
                return callable.call();
            }
        };
    }
}
