package com.example.concurrent;

import com.example.ui.ErrorMessages;
import javafx.application.Platform;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.*;
import java.util.function.Consumer;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public abstract class Task<T> {
    private final Callable<T> callable;
    private final Consumer<T> clb;

    private final CountDownLatch latch = new CountDownLatch(1);
    private T result;

    private Task(Callable<T> callable, Consumer<T> clb) {
        this.callable = callable;
        this.clb = clb;
    }

    private Task(Callable<T> callable) {
        this.callable = callable;
        this.clb = _ -> {
        };
    }

    private void run() {
        try {
            result = callable.call();
            clb.accept(result);
        } catch (Exception e) {
            var sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            ErrorMessages.showErrorMessage(e.getMessage(), sw.toString());
        } finally {
            latch.countDown();
        }
    }

    public T get() {
        await();
        return result;
    }

    public void await() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Task<T> callOnFxThread(Callable<T> callable) {
        var task = new FxTask<>(callable);
        task.start();
        return task;
    }

    public static <T> void callOnFxThread(Callable<T> callable, Consumer<T> clb) {
        new FxTask<>(callable, clb).start();
    }

    public static Task<Void> runOnFxThread(Executable runnable) {
        var task = new FxTask<>(toCallable(runnable));
        task.start();
        return task;
    }

    public static <T> Task<T> callOnWorkerThread(Callable<T> callable) {
        var task = new WorkerTask<>(callable);
        task.start();
        return task;
    }

    public static <T> void callOnWorkerThread(Callable<T> callable, Consumer<T> clb) {
        new WorkerTask<>(callable, clb).start();
    }

    public static Task<Void> runOnWorkerThread(Executable runnable) {
        var task = new WorkerTask<>(toCallable(runnable));
        task.start();
        return task;
    }

    private static Callable<Void> toCallable(Executable runnable) {
        return () -> {
            runnable.execute();
            return null;
        };
    }

    private static final class FxTask<T> extends Task<T> {
        private FxTask(Callable<T> callable, Consumer<T> clb) {
            super(callable, clb);
        }

        private FxTask(Callable<T> callable) {
            super(callable);
        }

        private void start() {
            Platform.runLater(super::run);
        }
    }

    private static final class WorkerTask<T> extends Task<T> {

        private static final ThreadFactory threadFactory = Thread.ofVirtual().name("worker-thread", 1).factory();
        private static final ExecutorService executor = Executors.newThreadPerTaskExecutor(threadFactory);

        private WorkerTask(Callable<T> callable, Consumer<T> clb) {
            super(callable, clb);
        }

        private WorkerTask(Callable<T> callable) {
            super(callable);
        }

        private void start() {
            executor.execute(super::run);
        }
    }
}