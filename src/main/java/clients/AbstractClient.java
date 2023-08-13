package clients;

import check.KeyManager;
import exceptions.ConnectClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.Connection;
import utils.ConnectionUtils;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;

public abstract class AbstractClient implements Serializable {
    private String id;
    private String sessionKey;
    private transient Connection connection;
    private transient final KeyManager keyManager;
    private transient volatile boolean isWriteToCache = false;
    private transient volatile boolean isReadFromCache = false;
    private transient int NUMBER_OF_CONNECTION_ATTEMPTS = 10;
    private transient int TIMEOUT = 5000;
    private transient static final Logger log = LogManager.getLogger(AbstractClient.class.getSimpleName());

    public AbstractClient(String host, int port, String id, KeyManager keyManager) {
        this.id = id;
        this.keyManager = keyManager;
        this.connection = new Connection(host, port);
    }

    public boolean establishConnectToServer() throws ConnectClientException {
        boolean isConnect;
        try {
            connection.connect();
            log.info("Connect to server " + connection.getHost() + " " + " established");
            return true;
        } catch (ConnectClientException e) {
            if (e.getMessage().contains("unreachable")) {
                log.info("Connect to the server " + connection.getHost() + " failed");
                isConnect = connection.reconnectToServer();
            } else throw e;
        }
        return isConnect;
    }

    protected abstract void loadSessionKey();

    protected abstract boolean authorize();

    public String getSessionKey() {
        return sessionKey;
    }

    public OutputStream getOutStrm() {
        return connection.getOutputStream();
    }

    public InputStream getInpStrm() {
        return connection.getInputStream();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getHost() {
        return connection.getHost();
    }

    public int getPort() {
        return connection.getPort();
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public boolean isWriteToCache() {
        return isWriteToCache;
    }

    public void setWriteToCache(boolean writeToCache) {
        isWriteToCache = writeToCache;
    }

    public boolean isReadFromCache() {
        return isReadFromCache;
    }

    public void setReadFromCache(boolean readFromCache) {
        isReadFromCache = readFromCache;
    }

    protected KeyManager getKeyManager() {
        return keyManager;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractClient client = (AbstractClient) o;
        return Objects.equals(id, client.id) &&
                Objects.equals(connection.getHost(), client.connection.getHost());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, connection.getHost());
    }

    @Override
    public String toString() {
        return "AbstractClient{" +
                "id='" + id + '\'' +
                ", host=" + getHost() +
                '}';
    }
}
