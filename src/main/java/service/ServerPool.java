package service;

import servers.another.Server;

import java.io.IOException;

public class ServerPool extends AbstractNetEntityPool<Integer, Server>{

    @Override
    public boolean finalizeEntity(Server server) {
        try {
            server.stop();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public Integer getId(Server server) {
        return server.getLocalPort();
    }
}
