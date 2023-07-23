package entity;

public interface Cached {
    byte[] readAllCache();
    byte[] readElementCache();
    void saveToCache(byte[] data);
}
