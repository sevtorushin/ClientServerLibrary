package service.containers;

import clients.another.Client;
import lombok.NonNull;

public class ClientPool extends AbstractNetEntityPool<Object, Client> {

    public ClientPool(int DEFAULT_SOCKET_POOL_SIZE) {
        super(DEFAULT_SOCKET_POOL_SIZE);
    }

    public ClientPool() {
    }

    @Override
    protected boolean finalizeEntity(@NonNull Client client) {
        client.close();
        return !client.isClosed();
    }

    @Override
    protected Object getId(@NonNull Client client) {
        return client.getId();
    }

    protected Integer getLocalPort(@NonNull Client client) {
        return client.getClientConnection().getLocalPort();
    }

    public Client getOnRemotePort(@NonNull Integer remotePort) {
        return entityStorage.stream()
                .filter(client -> client.getClientConnection().getRemotePort() == remotePort)
                .findFirst()
                .orElse(null);
    }
}
