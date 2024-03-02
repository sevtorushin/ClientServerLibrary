package service;

import lombok.NonNull;

import java.util.List;

public interface Stored<E> {
    boolean addNew(@NonNull E message);
    boolean remove(@NonNull E message);
    boolean removeAll();
    List<E> getAll();
}
