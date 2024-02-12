package service;

public interface Cached<T> {
    T readAllCache();
    T readElementCache();
    void saveToCache(T data);
}
