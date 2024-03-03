package service.containers;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractContainer<I, E> implements Container<I, E> {
    protected Collection<E> entityStorage;

    public AbstractContainer(Collection<E> entityStorage) {
        this.entityStorage = entityStorage;
    }

    @Override
    public boolean addNew(@NonNull E entity) {
        if (entityStorage.contains(entity))
            return false;
        else {
            this.entityStorage.add(entity);
            return true;
        }
    }

    @Override
    public boolean remove(@NonNull E entity) {
        return entityStorage.remove(entity);
    }

    @Override
    public boolean removeForID(@NonNull I id) {
        return entityStorage.removeIf(e -> getId(e).equals(id));
    }

    @Override
    public boolean removeAll() {
        entityStorage.clear();
        return true;
    }

    @Override
    public List<E> getAll() {
        return new ArrayList<>(entityStorage);
    }

    @Override
    public List<I> getAllID() {
        return entityStorage.stream().map(this::getId).collect(Collectors.toList());
    }

    @Override
    public E get(@NonNull I id) {
        return entityStorage.stream()
                .filter(e -> getId(e).equals(id))
                .findFirst()
                .orElse(null);
    }

    protected abstract I getId(@NonNull E entity);
}
