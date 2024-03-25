package service;

import java.util.concurrent.CompletableFuture;

public interface Task<T> {
    CompletableFuture<T> execute();
    boolean isDone();
    boolean isCancelled();
    void cancel();
}
