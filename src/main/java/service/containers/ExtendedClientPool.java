package service.containers;

import clients.another.Client;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;

public class ExtendedClientPool extends ClientPool {
    private final Set<Client> newClients;

    public ExtendedClientPool(int DEFAULT_SOCKET_POOL_SIZE) {
        super(DEFAULT_SOCKET_POOL_SIZE);
        this.newClients = new HashSet<>();
    }

    public ExtendedClientPool() {
        this.newClients = new HashSet<>();
    }

    @Override
    public boolean addNew(@NonNull Client netEntity) {
        boolean isSuccessful = super.addNew(netEntity);
        if (isSuccessful)
            newClients.add(netEntity);
        return isSuccessful;
    }

    @Override
    public boolean remove(@NonNull Client netEntity) {
        boolean isSuccessful = super.remove(netEntity);
        if (isSuccessful)
            newClients.remove(netEntity);
        return isSuccessful;
    }

    @Override
    public boolean removeAll() {
        boolean isSuccessful = super.removeAll();
        if (isSuccessful)
            newClients.clear();
        return isSuccessful;
    }

    public Client getNewClient() {
        Client newClient = newClients.stream().findFirst().orElse(null);
        newClients.remove(newClient);
        return newClient;
    }
}
