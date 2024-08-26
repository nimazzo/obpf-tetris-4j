package com.example.worker;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class Worker<T> extends Service<T> {

    private final Callable<T> callable;

    private Worker(Runnable runnable) {
        setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        setOnFailed(_ -> {
            throw new RuntimeException(getException());
        });
        this.callable = () -> {
            runnable.run();
            return null;
        };
    }

    public static void execute(Runnable runnable) {
        new Worker<>(runnable).start();
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
