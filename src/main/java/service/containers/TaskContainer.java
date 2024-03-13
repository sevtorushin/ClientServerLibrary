package service.containers;

import lombok.NonNull;
import service.IdentifiableTask;
import service.Task;
import service.containers.AbstractContainer;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    @Override
    public boolean remove(@NonNull IdentifiableTask<Object, ?> entity) {
        if (entity.isDone())
            return super.remove(entity);
        else {
            System.err.println(String.format("Task '%s' is not completed", entity));
            return false;
        }
    }

    @Override
    public boolean removeForID(@NonNull Object id) {
        IdentifiableTask<Object, ?> task = get(id);
        return remove(task);
    }

    @Override
    public boolean removeAll() {
        List<IdentifiableTask<Object, ?>> notCompletedTasks = entityStorage.stream().filter(task -> !task.isDone()).collect(Collectors.toList());
        if (notCompletedTasks.isEmpty())
            return super.removeAll();
        else {
            System.err.println(String.format("Tasks '%s' is not completed", notCompletedTasks));
            return false;
        }
    }
}
