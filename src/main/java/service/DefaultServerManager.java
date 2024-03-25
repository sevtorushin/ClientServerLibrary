package service;

import servers.another.Server;
import service.containers.AbstractNetEntityPool;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

@Deprecated
public class DefaultServerManager extends NetEntityManager<Server, ByteBuffer> {

    public DefaultServerManager(AbstractNetEntityPool<Object, Server> entityPool) {
        super(entityPool);
    }

    public Server createServer(Integer port, Class<? extends Server> serverClass) {
        Server server = null;
        try {
            server = serverClass.getConstructor(Integer.class).newInstance(port);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return server;
    }

    public Server getServer(Integer localPort) {
        return entityPool.getOnLocalPort(localPort);
    }

}
