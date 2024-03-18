package service.containers;

import entity.Net;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public abstract class AbstractNetEntityPool<I, E extends Net> extends AbstractContainer<I, E> {
    private final int DEFAULT_SOCKET_POOL_SIZE;

    public AbstractNetEntityPool(int DEFAULT_SOCKET_POOL_SIZE) {
        super(new HashSet<>());
        this.DEFAULT_SOCKET_POOL_SIZE = DEFAULT_SOCKET_POOL_SIZE;
    }

    public AbstractNetEntityPool() {
        super(new HashSet<>());
        this.DEFAULT_SOCKET_POOL_SIZE = 100;
    }

    @Override
    public boolean addNew(@NonNull E netEntity) {
        if (entityStorage.size() >= DEFAULT_SOCKET_POOL_SIZE)
            return false;
        else return super.addNew(netEntity);
    }

    @Override
    public boolean remove(@NonNull E netEntity) {
        if (finalizeEntity(netEntity))
            return super.remove(netEntity);
        else {
            return false;
        }
    }

    @Override
    public boolean removeAll() {
        entityStorage.removeIf(this::finalizeEntity);
        return entityStorage.isEmpty();
    }

    public abstract boolean finalizeEntity(@NonNull E netEntity);
}
