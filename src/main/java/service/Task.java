package service;

import java.util.concurrent.CompletableFuture;

public interface Task<T> {
    CompletableFuture<T> execute();
}
