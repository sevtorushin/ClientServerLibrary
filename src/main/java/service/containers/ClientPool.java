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
        client.close();
        return !client.isConnected();
    }

    @Override
    public Object getId(@NonNull Client client) {
        return client.getId();
    }

    public Integer getLocalPort(@NonNull Client client){
        return client.getClientConnection().getLocalPort();
    }

    public Client getOnLocalPort(@NonNull Integer localPort){
        return entityStorage.stream()
                .filter(client -> client.getClientConnection().getLocalPort()==localPort)
                .findFirst()
                .orElse(null);
    }

    public Client getOnRemotePort(@NonNull Integer remotePort){
        return entityStorage.stream()
                .filter(client -> client.getClientConnection().getRemotePort()==remotePort)
                .findFirst()
                .orElse(null);
    }
}
