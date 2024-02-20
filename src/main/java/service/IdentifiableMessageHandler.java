package service;

import lombok.*;

@ToString
@EqualsAndHashCode
public abstract class IdentifiableMessageHandler<I, T> implements MessageHandler<T> {
    @Getter
    @Setter
    @NonNull
    private I identifier;

    public IdentifiableMessageHandler(@NonNull I identifier) {
        this.identifier = identifier;
    }
}
