package entity;

public interface Cached<T> {
    T readAllCache();
    T readElementCache();
    void saveToCache(T data);
}
