package service;

public interface IdentifiableTask<ID, TYPE> extends Task<TYPE>{
    ID getId();
}
