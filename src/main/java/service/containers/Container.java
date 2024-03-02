package service.containers;

import lombok.NonNull;
import service.Stored;

import java.util.List;

public interface Container<I, E> extends Stored<E> {
    boolean removeForID(@NonNull I ID);
    List<I> getAllID();
    E get(@NonNull I id);
}
