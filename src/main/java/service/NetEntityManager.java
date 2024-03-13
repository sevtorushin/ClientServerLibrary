package service;

import entity.Net;
import lombok.NonNull;
import service.containers.AbstractHandlerContainer;
import service.containers.AbstractNetEntityPool;
import test.AbstractNetManager;

import java.util.*;

public abstract class NetEntityManager<NetType extends Net, MessageType> {

    protected Map<NetType, LocalContainer> map;
    protected AbstractNetEntityPool<Object, NetType> entityPool;

    NetEntityManager(AbstractNetEntityPool<Object, NetType> entityPool) {
        this.entityPool = entityPool;
        this.map = new HashMap<>();
    }

    private class LocalContainer {
        private final AbstractHandlerContainer<Object, MessageType> handlerContainer;
        private final TaskContainer taskContainer;

        public LocalContainer(AbstractHandlerContainer<Object, MessageType> handlerContainer) {
            this.handlerContainer = handlerContainer;
            this.taskContainer = new TaskContainer();
        }
    }

    public AbstractHandlerContainer<Object, MessageType> getHandlerContainer(@NonNull NetType netEntity) {
        return map.get(netEntity).handlerContainer;
    }

    public TaskContainer getTaskContainer(@NonNull NetType netEntity) {
        return map.get(netEntity).taskContainer;
    }

    public boolean addNetEntity(@NonNull NetType netEntity, @NonNull AbstractHandlerContainer<Object, MessageType> handlerContainer) {
        if (entityPool.addNew(netEntity)) {
            map.put(netEntity, new LocalContainer(handlerContainer));
            return true;
        } else return false;
    }

    public boolean removeNetEntity(@NonNull NetType netEntity) {
        if (entityPool.remove(netEntity)) {
            map.remove(netEntity);
            return true;
        } else return false;
    }

//    public boolean removeNetEntity(@NonNull Object idNetEntity) {
//        NetType netEntity = entityPool.get(idNetEntity);
//        if (netEntity == null)
//            throw new NoSuchElementException(String.format("Specified entity with ID '%s' missed in pool", idNetEntity));
//        else return removeNetEntity(netEntity);
//    }

    public boolean addHandler(@NonNull NetType netEntity, @NonNull IdentifiableMessageHandler<Object, MessageType> handler) {
        AbstractHandlerContainer<Object, MessageType> handlerContainer = getHandlerContainer(netEntity);
        if (handlerContainer == null) {
            throw new NoSuchElementException(String.format("Specified %s missed in pool", netEntity.getClass().getSimpleName()));
        }
        boolean isSuccess = handlerContainer.addNew(handler);
        if (isSuccess)
            return true;
        else {
            System.err.println(String.format("%s already contains the specified handler", netEntity));
            return false;
        }
    }

    public boolean removeHandler(@NonNull NetType netEntity, @NonNull Object handlerId) {
        AbstractHandlerContainer<Object, MessageType> handlerContainer = getHandlerContainer(netEntity);
        if (handlerContainer == null) {
            throw new NoSuchElementException(String.format("Specified %s missed in pool", netEntity.getClass().getSimpleName()));
        }
        boolean isSuccess = handlerContainer.removeForID(handlerId);
        if (isSuccess)
            return true;
        else {
            System.err.println(String.format("Handler with specified identifier '%s' is missed for %s", handlerId, netEntity));
            return false;
        }
    }

    public <T> boolean addTask(@NonNull NetType netEntity, @NonNull IdentifiableTask<Object, T> task) {
        TaskContainer taskContainer = getTaskContainer(netEntity);
        if (taskContainer == null) {
            throw new NoSuchElementException(String.format("Specified %s missed in pool", netEntity.getClass().getSimpleName()));
        }
        boolean isSuccess = taskContainer.addNew(task);
        if (isSuccess)
            return true;
        else {
            System.err.println(String.format("%s already contains the specified task", netEntity));
            return false;
        }
    }

    public boolean removeTask(@NonNull NetType netEntity, @NonNull Object taskId) {
        TaskContainer taskContainer = getTaskContainer(netEntity);
        if (taskContainer == null) {
            throw new NoSuchElementException(String.format("Specified %s missed in pool", netEntity.getClass().getSimpleName()));
        }
        boolean isSuccess = taskContainer.removeForID(taskId);
        if (isSuccess)
            return true;
        else {
            System.err.println(String.format("Task with specified identifier '%s' is missed for %s", taskId, netEntity));
            return false;
        }
    }

    public List<NetType> getAllNetEntities() {
        return new ArrayList<>(entityPool.getAll());
    }

    public List<Object> getAllIdNetEntity() {
        return new ArrayList<>(entityPool.getAllID());
    }

    public List<MessageHandler<MessageType>> getAllHandlers(@NonNull NetType netEntity) {
        AbstractHandlerContainer<Object, MessageType> handlerContainer = getHandlerContainer(netEntity);
        if (handlerContainer == null) {
            throw new NoSuchElementException(String.format("Specified %s missed in pool", netEntity.getClass().getSimpleName()));
        } else return new ArrayList<>(handlerContainer.getAll());
    }

    public List<Object> getAllIdHandlers(@NonNull NetType netEntity) {
        AbstractHandlerContainer<Object, MessageType> handlerContainer = getHandlerContainer(netEntity);
        if (handlerContainer == null) {
            throw new NoSuchElementException(String.format("Specified %s missed in pool", netEntity.getClass().getSimpleName()));
        } else return new ArrayList<>(handlerContainer.getAllID());
    }

    public List<Task<?>> getAllTasks(@NonNull NetType netEntity) {
        TaskContainer taskContainer = getTaskContainer(netEntity);
        if (taskContainer == null) {
            throw new NoSuchElementException(String.format("Specified %s missed in pool", netEntity.getClass().getSimpleName()));
        } else return new ArrayList<>(taskContainer.getAll());
    }

    public List<Object> getAllIdTasks(@NonNull NetType netEntity) {
        TaskContainer taskContainer = getTaskContainer(netEntity);
        if (taskContainer == null) {
            throw new NoSuchElementException(String.format("Specified %s missed in pool", netEntity.getClass().getSimpleName()));
        } else return new ArrayList<>(taskContainer.getAllID());
    }

    public boolean removeAllNetEntities() {
        List<NetType> all = entityPool.getAll();
        for (NetType netEntity : all) {
            removeNetEntity(netEntity);
        }
        return entityPool.getAll().isEmpty();
    }

    public boolean removeAllHandlers(@NonNull NetType netEntity) {
        AbstractHandlerContainer<Object, MessageType> handlerContainer = getHandlerContainer(netEntity);
        if (handlerContainer == null) {
            throw new NoSuchElementException(String.format("Specified %s missed in pool", netEntity.getClass().getSimpleName()));
        } else {
            handlerContainer.removeAll();
        }
        return handlerContainer.getAll().isEmpty();
    }

    public boolean removeAllTasks(@NonNull NetType netEntity) {
        TaskContainer taskContainer = getTaskContainer(netEntity);
        if (taskContainer == null)
            throw new NoSuchElementException(String.format("Specified %s missed in pool", netEntity.getClass().getSimpleName()));
        else {
            taskContainer.removeAll();
        }
        return taskContainer.getAll().isEmpty();
    }

    public NetType getNetEntity(@NonNull Object id) {
        return entityPool.get(id);
    }
}
