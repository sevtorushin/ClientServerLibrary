package service;

import lombok.*;

import java.util.concurrent.CompletableFuture;

@ToString
@EqualsAndHashCode
public abstract class RunnableTask implements IdentifiableTask<Object, Void>, Runnable {
    @Getter
    @Setter
    private Object id;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Getter
    private CompletableFuture<Void> completableFuture;

    public RunnableTask(Object id) {
        this.id = id;
    }

    @Override
    public CompletableFuture<Void> execute() {
        completableFuture = CompletableFuture.runAsync(this);
        return completableFuture;
    }
}
