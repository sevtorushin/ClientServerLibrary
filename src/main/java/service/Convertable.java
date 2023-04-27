package service;

public interface Convertable<T> {
    T convert(byte[] data, Class<? extends T> clazz);
}
