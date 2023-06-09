package service;

import exceptions.BuildObjectException;

public interface Convertable<T> {
    T convert(byte[] data, Class<? extends T> clazz) throws BuildObjectException;
}
