package com.example.ma_exam.util;

import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Runs database work off the JavaFX UI thread so the window never freezes.
 * A single worker thread is used: the JDBC connection is shared, so DB calls must not run concurrently.
 * Callbacks (success and error) run back on the JavaFX thread.
 */
public final class Async {

    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "db-worker");
        thread.setDaemon(true);
        return thread;
    });

    @FunctionalInterface
    public interface Work<T> {
        T call() throws Exception;
    }

    @FunctionalInterface
    public interface VoidWork {
        void run() throws Exception;
    }

    private Async() {
    }

    /**
     * Runs work in the background, then onSuccess with its result.
     * On failure, shows an error alert titled errorTitle.
     */
    public static <T> void supply(String errorTitle, Work<T> work, Consumer<T> onSuccess) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
        task.setOnFailed(event -> Alerts.error(errorTitle, task.getException()));
        DB_EXECUTOR.submit(task);
    }

    public static void run(String errorTitle, VoidWork work, Runnable onSuccess) {
        supply(errorTitle, () -> {
            work.run();
            return null;
        }, ignored -> onSuccess.run());
    }
}
