package service;

import java.util.List;

public interface Container<I, E> {
    boolean addNew(E entity);
    boolean remove(E entity);
    boolean removeAll();
    List<E> getAll();
    E get(I id);
}
