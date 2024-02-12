package service;

public interface Stored<T> {
    void putToStorage(T message);
    T retrieveFromStorage();
    void clearStorage();
}
