package service;

import java.util.List;

public interface TaskHandler {
    void addTask(String taskName, MessageHandler handler);
    void removeTask(String taskName);
    void removeAllTask();
    List<String> getALLTask();
}
