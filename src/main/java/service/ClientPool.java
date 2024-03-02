package service;

import clients.another.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientPool extends AbstractNetEntityPool<Object, Client>{

    @Override
    public boolean finalizeEntity(Client client) {
        return client.disconnect();
    }

    @Override
    public Object getId(Client client) {
        return client.getId();
    }

    public Integer getLocalPort(Client client){
        return client.getClientConnection().getLocalPort();
    }

    public Client getOnLocalPort(Integer localPort){
        return entityPool.stream()
                .filter(client -> client.getClientConnection().getLocalPort()==localPort)
                .findFirst()
                .orElse(null);
    }
}
