package service;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@ToString
@EqualsAndHashCode
public abstract class CallableTask<T> implements IdentifiableTask<Object, T>, Callable<T> {
    @Getter
    @Setter
    private Object id;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Getter
    private CompletableFuture<T> completableFuture;

    public CallableTask(Object id) {
        this.id = id;
    }


    @Override
    public CompletableFuture<T> execute() {
        completableFuture = CompletableFuture.supplyAsync(() -> {
            T ob = null;
            try {
                ob = call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ob;
        });
        return completableFuture;
    }
}
