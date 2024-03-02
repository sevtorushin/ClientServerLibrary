package service.containers;

import lombok.NonNull;

import java.util.List;

public interface Container<I, E> {
    boolean addNew(@NonNull E entity);
    boolean remove(@NonNull E entity);
    boolean removeForID(@NonNull I ID);
    boolean removeAll();
    List<E> getAll();
    List<I> getAllID();
    E get(@NonNull I id);
}
