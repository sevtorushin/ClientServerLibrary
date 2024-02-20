package service;

import entity.Net;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractNetEntityPool<I, E extends Net> implements Container<I, E>{
    protected final LinkedBlockingQueue<E> entityPool;
    private final int DEFAULT_SOCKET_POOL_SIZE;

    public AbstractNetEntityPool(int DEFAULT_SOCKET_POOL_SIZE) {
        this.DEFAULT_SOCKET_POOL_SIZE = DEFAULT_SOCKET_POOL_SIZE;
        this.entityPool = new LinkedBlockingQueue<>(DEFAULT_SOCKET_POOL_SIZE);
    }

    public AbstractNetEntityPool() {
        this.DEFAULT_SOCKET_POOL_SIZE = Integer.MAX_VALUE;
        this.entityPool = new LinkedBlockingQueue<>(DEFAULT_SOCKET_POOL_SIZE);
    }

    @Override
    public boolean addNew(E netEntity) {
        return entityPool.offer(netEntity);
    }

    @Override
    public boolean remove(E netEntity) {
        if (finalizeEntity(netEntity))
            return entityPool.remove(netEntity);
        else {
            System.err.println(String.format("%s disconnect error", netEntity));
            return false;
        }
    }

    @Override
    public boolean removeAll() {
        for (E netEntity : entityPool) {
            remove(netEntity);
        }
        return entityPool.isEmpty();
    }

    @Override
    public List<E> getAll() {
        return new ArrayList<>(entityPool);
    }

    @Override
    public E get(I id) {
        return entityPool.stream()
                .filter(e -> getId(e).equals(id))
                .findFirst()
                .orElse(null);
    }

    public abstract boolean finalizeEntity(E netEntity);

    public abstract I getId(E netEntity);
}
