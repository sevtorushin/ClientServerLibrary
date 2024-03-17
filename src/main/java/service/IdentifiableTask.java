package service;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.CompletableFuture;

@ToString
@EqualsAndHashCode
public abstract class IdentifiableTask<ID, TYPE> implements Task<TYPE>{
    @Getter
    @Setter
    protected Object id;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Getter
    protected CompletableFuture<TYPE> completableFuture = new CompletableFuture<>();

    public IdentifiableTask(Object id) {
        this.id = id;
    }

    @Override
    public boolean isDone(){
        return completableFuture.isDone();
    }

    @Override
    public boolean isCancelled(){
        return completableFuture.isCancelled();
    }

    @Override
    public boolean cancel(){
        return completableFuture.cancel(true);
    }
}
