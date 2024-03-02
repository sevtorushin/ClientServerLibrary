package service.containers;

import clients.another.Client;
import lombok.NonNull;

public class ClientPool extends AbstractNetEntityPool<Object, Client>{

    public ClientPool(int DEFAULT_SOCKET_POOL_SIZE) {
        super(DEFAULT_SOCKET_POOL_SIZE);
    }

    public ClientPool() {}

    @Override
    public boolean finalizeEntity(@NonNull Client client) {
        return client.disconnect();
    }

    @Override
    public Object getId(@NonNull Client client) {
        return client.getId();
    }

    public Integer getLocalPort(@NonNull Client client){
        return client.getClientConnection().getLocalPort();
    }

    public Client getOnLocalPort(@NonNull Integer localPort){
        return entityPool.stream()
                .filter(client -> client.getClientConnection().getLocalPort()==localPort)
                .findFirst()
                .orElse(null);
    }
}
