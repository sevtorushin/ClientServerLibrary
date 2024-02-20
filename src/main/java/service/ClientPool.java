package service;

import clients.another.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientPool extends AbstractNetEntityPool<Integer, Client>{

    @Override
    public boolean finalizeEntity(Client client) {
        return client.disconnect();
    }

    @Override
    public Integer getId(Client client) {
        return client.getClientConnection().getLocalPort();
    }
}
