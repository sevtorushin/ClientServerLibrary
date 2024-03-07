package service.containers;

import exceptions.HandleException;
import lombok.NonNull;
import service.IdentifiableMessageHandler;

import java.util.*;

public abstract class AbstractHandlerContainer<I, T> extends AbstractContainer<I, IdentifiableMessageHandler<I, T>> { //todo мб убрать I и заменить на Object?

    protected AbstractHandlerContainer() {
        super(new HashSet<>());
    }

    public abstract void invokeAll(@NonNull T message) throws HandleException;
}
