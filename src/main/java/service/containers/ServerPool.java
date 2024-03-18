package service.containers;

import clients.another.Client;
import lombok.NonNull;
import servers.another.Server;
import service.containers.AbstractNetEntityPool;

import java.io.IOException;

public class ServerPool extends AbstractNetEntityPool<Object, Server> {

    @Override
    protected boolean finalizeEntity(@NonNull Server server) {
        try {
            server.stop();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected Integer getLocalPort(@NonNull Server netEntity) {
        return netEntity.getLocalPort();
    }

    @Override
    protected Object getId(@NonNull Server server) {
        return server.getId();
    }
}
