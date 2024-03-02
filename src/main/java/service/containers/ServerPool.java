package service.containers;

import servers.another.Server;
import service.containers.AbstractNetEntityPool;

public class ServerPool extends AbstractNetEntityPool<Integer, Server> {

    @Override
    public boolean finalizeEntity(Server server) {
//        try {
//            server.stop();
//            return true;
//        } catch (IOException e) {
//            return false;
//        }

        return false; //todo удалить
    }

    @Override
    public Integer getId(Server server) {
        return server.getLocalPort();
    }
}
