package service;

import exceptions.BuildObjectException;

public interface Convertable<T, U> {
    T convert(U data, Class<? extends T> clazz) throws BuildObjectException;
}
