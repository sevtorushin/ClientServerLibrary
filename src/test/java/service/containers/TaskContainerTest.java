package service.containers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.CallableTask;
import service.RunnableTask;

import static org.junit.jupiter.api.Assertions.*;

class TaskContainerTest {
    private static TaskContainer taskContainer;
    private static RunnableTask runnableTask;
    private static RunnableTask longRunningRunnableTask;
    private static CallableTask<String> callableTask;

    @BeforeAll
    static void beforeAll() {
        taskContainer = new TaskContainer();
        runnableTask = new RunnableTask("runnableTask") {
            @Override
            public void run() {
                System.out.println("runnableTask completed");
            }
        };
        longRunningRunnableTask = new RunnableTask("longRunningRunnableTask") {
            @Override
            public void run() {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        callableTask = new CallableTask<String>("callableTask") {
            @Override
            public String call() throws Exception {
                long start = System.currentTimeMillis();
                Thread.sleep((long) (Math.random() * 10));
                long stop = System.currentTimeMillis();

                return String.valueOf(stop - start);
            }
        };
    }

    @BeforeEach
    void setUp() {
        runnableTask.cancel();
        longRunningRunnableTask.cancel();
        taskContainer.removeAll();
    }

    private void pause(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void add() {
        boolean isSuccessful = taskContainer.addNew(runnableTask);
        assertFalse(taskContainer.entityStorage.isEmpty());
        assertTrue(isSuccessful);
    }

    @Test
    void removeNotRunningTask() {
        taskContainer.addNew(runnableTask);
        taskContainer.remove(runnableTask);
        assertTrue(taskContainer.entityStorage.isEmpty());
    }

    @Test
    void removeCompletedTask() {
        runnableTask.execute();
        taskContainer.addNew(runnableTask);
        pause(100);
        boolean isSuccessful = taskContainer.remove(runnableTask);
        assertTrue(taskContainer.entityStorage.isEmpty());
        assertTrue(isSuccessful);
    }

    @Test
    void removeRunningTask() {
        longRunningRunnableTask.execute();
        taskContainer.addNew(longRunningRunnableTask);
        pause(100);
        boolean isSuccessful = taskContainer.remove(longRunningRunnableTask);
        assertFalse(taskContainer.entityStorage.isEmpty());
        assertFalse(isSuccessful);
    }

    @Test
    void removeRunningButCancelledTask() {
        longRunningRunnableTask.execute();
        taskContainer.addNew(longRunningRunnableTask);
        pause(100);
        longRunningRunnableTask.cancel();
        boolean isSuccessful = taskContainer.remove(longRunningRunnableTask);
        assertTrue(taskContainer.entityStorage.isEmpty());
        assertTrue(isSuccessful);
    }

    @Test
    void removeAllNotRunningTask() {
        taskContainer.addNew(runnableTask);
        taskContainer.addNew(longRunningRunnableTask);
        boolean isSuccessful = taskContainer.removeAll();
        assertTrue(taskContainer.entityStorage.isEmpty());
        assertTrue(isSuccessful);
    }

    @Test
    void removeAllRunningTask() {
        runnableTask.execute();
        longRunningRunnableTask.execute();
        taskContainer.addNew(runnableTask);
        taskContainer.addNew(longRunningRunnableTask);
        pause(100);
        boolean isSuccessful = taskContainer.removeAll();
        assertEquals(2, taskContainer.entityStorage.size());
        assertFalse(isSuccessful);
    }

    @Test
    void forceRemove() {
        longRunningRunnableTask.execute();
        taskContainer.addNew(longRunningRunnableTask);
        pause(100);
        boolean isSuccessful = taskContainer.forceRemove(longRunningRunnableTask);
        assertTrue(taskContainer.entityStorage.isEmpty());
        assertTrue(isSuccessful);
    }

    @Test
    void forceRemoveAll() {
        runnableTask.execute();
        longRunningRunnableTask.execute();
        taskContainer.addNew(runnableTask);
        taskContainer.addNew(longRunningRunnableTask);
        pause(100);
        boolean isSuccessful = taskContainer.forceRemoveAll();
        assertTrue(taskContainer.entityStorage.isEmpty());
        assertTrue(isSuccessful);
    }
}