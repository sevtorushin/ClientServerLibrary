package service;

import lombok.*;

@ToString
@EqualsAndHashCode
public abstract class IdentifiableMessageHandler<I, T> implements MessageHandler<T> {
    @Getter
    @Setter
    private I identifier;

    public IdentifiableMessageHandler(I identifier) {
        this.identifier = identifier;
    }
}
