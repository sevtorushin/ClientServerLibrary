package service;

import lombok.NonNull;
import service.containers.AbstractContainer;

import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

public class TaskContainer extends AbstractContainer<Object, IdentifiableTask<Object, ?>> {
    public TaskContainer() {
        super(new HashSet<>());
    }

    @Override
    protected Object getId(@NonNull IdentifiableTask<Object, ?> entity) {
        return entity.getId();
    }

    public CompletableFuture<?> executeTask(Object id) {
        return get(id).execute();
    }
}
