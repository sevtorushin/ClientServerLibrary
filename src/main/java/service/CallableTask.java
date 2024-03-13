package service;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class CallableTask<T> extends IdentifiableTask<Object, T> implements Callable<T> {

    public CallableTask(Object id) {
        super(id);
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
