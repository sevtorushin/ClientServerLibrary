package service;

import clients.another.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientPool extends AbstractNetEntityPool<Client>{

    @Override
    public boolean finalizeEntity(Client client) {
        return client.disconnect();
    }

    @Override
    public int getId(Client client) {
        return client.getClientConnection().getLocalPort();
    }
}
