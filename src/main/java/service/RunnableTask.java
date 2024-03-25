package service;

import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class RunnableTask extends IdentifiableTask<Object, Void> implements Runnable {

    private static final Logger log = LogManager.getLogger(RunnableTask.class.getSimpleName());

    public RunnableTask(Object id) {
        super(id);
    }

    @Override
    public CompletableFuture<Void> execute() {
        completableFuture = CompletableFuture.runAsync(() -> {
            log.debug(String.format("Task %s started", this));
            run();
            log.debug(String.format("Task %s completed successful", this));
        });
        return completableFuture;
    }
}
