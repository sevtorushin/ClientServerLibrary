package service.containers;

import entity.Net;
import lombok.Getter;
import lombok.NonNull;
import service.ReadProperties;

import java.util.HashSet;

public abstract class AbstractNetEntityPool<I, E extends Net> extends AbstractContainer<I, E> {
    @Getter
    private final int DEFAULT_SOCKET_POOL_SIZE;

    public AbstractNetEntityPool(int DEFAULT_SOCKET_POOL_SIZE) {
        super(new HashSet<>());
        this.DEFAULT_SOCKET_POOL_SIZE = DEFAULT_SOCKET_POOL_SIZE;
    }

    public AbstractNetEntityPool() {
        super(new HashSet<>());
        ReadProperties propertiesReader = ReadProperties.getInstance();
        this.DEFAULT_SOCKET_POOL_SIZE = Integer.parseInt(propertiesReader.getValue("container.defaultPoolSize"));
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

    protected abstract boolean finalizeEntity(@NonNull E netEntity);

    protected abstract Integer getLocalPort(@NonNull E netEntity);

    public E getOnLocalPort(@NonNull Integer localPort){
        return entityStorage.stream()
                .filter(netEntity -> getLocalPort(netEntity).equals(localPort))
                .findFirst()
                .orElse(null);
    }
}
