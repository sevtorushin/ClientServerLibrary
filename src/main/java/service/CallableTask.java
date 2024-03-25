package service;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class CallableTask<T> extends IdentifiableTask<Object, T> implements Callable<T> {

    private static final Logger log = LogManager.getLogger(CallableTask.class.getSimpleName());

    public CallableTask(Object id) {
        super(id);
    }

    @Override
    public CompletableFuture<T> execute() {
        completableFuture = CompletableFuture.supplyAsync(() -> {
            log.debug(String.format("Task %s started", this));
            T ob = null;
            try {
                ob = call();
                log.debug(String.format("Task %s completed successful", this));
            } catch (Exception e) {
                log.error(String.format("Task %s failed", this), e);
            }
            return ob;
        });
        return completableFuture;
    }
}
