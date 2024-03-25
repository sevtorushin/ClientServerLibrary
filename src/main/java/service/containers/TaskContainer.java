package service.containers;

import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servers.another.Server;
import service.IdentifiableTask;
import service.Task;
import service.containers.AbstractContainer;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TaskContainer extends AbstractContainer<Object, IdentifiableTask<Object, ?>> {

    private static final Logger log = LogManager.getLogger(TaskContainer.class.getSimpleName());

    public TaskContainer() {
        super(new HashSet<>());
    }

    @Override
    protected Object getId(@NonNull IdentifiableTask<Object, ?> entity) {
        return entity.getId();
    }

    /**
     * Soft removing task. If the specified task is not completed, it is not delete.
     *
     * @param entity task.
     * @return {@code true} if the specified task removed or {@code false} if not.
     */
    @Override
    public boolean remove(@NonNull IdentifiableTask<Object, ?> entity) {
        if (entity.isDone()) {
            boolean isSuccessful = super.remove(entity);
            if (isSuccessful)
                log.debug(String.format("Task '%s' removed successful", entity));
            else
                log.debug(String.format("Task '%s' is not removed", entity));
            return isSuccessful;
        } else {
            log.warn(String.format("Task '%s' is not completed", entity));
            return false;
        }
    }

    @Override
    public boolean removeForID(@NonNull Object id) {
        IdentifiableTask<Object, ?> task = get(id);
        return remove(task);
    }

    /**
     * Soft removing all tasks. If any task is not completed, then none is delete.
     *
     * @return {@code true} if all tasks are removed or {@code false} if not.
     */
    @Override
    public boolean removeAll() {
        List<IdentifiableTask<Object, ?>> notCompletedTasks = entityStorage.stream()
                .filter(task -> !task.isDone())
                .collect(Collectors.toList());
        if (notCompletedTasks.isEmpty()) {
            boolean isSuccessful = super.removeAll();
            if (isSuccessful)
                log.debug("All tasks removed successful");
            else
                log.debug("All tasks is not removed");
            return isSuccessful;
        }
        else {
            System.err.println(String.format("Tasks '%s' is not completed", notCompletedTasks));
            return false;
        }
    }

    public CompletableFuture<?> execute(@NonNull IdentifiableTask<Object, ?> entity) {
        return entity.execute();
    }

    /**
     * Hard removing task. The task is interrupt and delete in any case.
     *
     * @param entity task
     * @return {@code true} if the task is removed or {@code false} if not.
     */
    public boolean forceRemove(@NonNull IdentifiableTask<Object, ?> entity) {
        entity.cancel();
        if (entity.isCancelled() || entity.isDone())
            return remove(entity);
        else {
            log.debug(String.format("Delete %s impossible", entity));
            return false;
        }
    }

    /**
     * Hard removing all tasks. All tasks are interrupt and delete in any case.
     *
     * @return {@code true} if the task is removed or {@code false} if not.
     */
    public boolean forceRemoveAll() {
        entityStorage.forEach(Task::cancel);
        entityStorage.removeIf(task -> task.isCancelled() || task.isDone());
        boolean isSuccessful = entityStorage.isEmpty();
        if (isSuccessful)
            log.debug("All tasks delete successful");
        else
            log.debug("Fail delete all tasks");
        return isSuccessful;
    }
}
