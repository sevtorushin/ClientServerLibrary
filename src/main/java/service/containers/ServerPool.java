package service.containers;

import clients.another.Client;
import lombok.NonNull;
import servers.another.Server;
import service.containers.AbstractNetEntityPool;

import java.io.IOException;

public class ServerPool extends AbstractNetEntityPool<Object, Server> {

    @Override
    public boolean finalizeEntity(@NonNull Server server) {
        try {
            server.stop();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public Object getId(@NonNull Server server) {
        return server.getId();
    }

    public Server getOnLocalPort(@NonNull Integer localPort) {
        return entityStorage.stream()
                .filter(server -> server.getLocalPort() == localPort)
                .findFirst()
                .orElse(null);
    }
}
