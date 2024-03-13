package service;

import lombok.*;

import java.util.concurrent.CompletableFuture;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class RunnableTask extends IdentifiableTask<Object, Void> implements Runnable {

    public RunnableTask(Object id) {
        super(id);
    }

    @Override
    public CompletableFuture<Void> execute() {
        completableFuture = CompletableFuture.runAsync(this);
        return completableFuture;
    }
}
