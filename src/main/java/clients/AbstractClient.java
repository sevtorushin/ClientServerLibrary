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
//    private transient Socket socket;
//    private transient String host;
//    private transient int port;
//    private transient OutputStream outStrm;
//    private transient InputStream inpStrm;
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

//    protected Socket connect(String hostName, int port) throws ConnectClientException {
//        Socket socket = null;
//        try {
//            if (ConnectionUtils.isReachedHost(hostName))
//                socket = new Socket(hostName, port);
//            else throw new ConnectClientException("The specified endpoint " + hostName + " unreachable");
//        } catch (UnknownHostException e) {
//            log.debug("Unknown host ", e);
//            throw new ConnectClientException("Unknown host " + hostName);
//        } catch (ConnectException e) {
//            if (e.getMessage().contains("timed out")) {
//                log.debug("Connection to server " + hostName + " timed out", e);
//                throw new ConnectClientException(e.getMessage());
//            }
//        } catch (IOException e) {
//            if (e.getMessage().equals("Connection refused: connect")) {
//                log.debug("The server is not running on the specified endpoint " + port, e);
//                throw new ConnectClientException("The server is not running on the specified endpoint " + port);
//            } else throw new ConnectClientException(e.getMessage());
//        }
//
//        return socket;
//    }

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

//    public void connectToServer() throws ConnectClientException {
//        byte[] message = new byte[512];
//        try {
//            keyManager.removeKey(sessionKey);
//            try {
//                this.socket = connect(host, port);
//            } catch (ConnectClientException e) {
//                if (e.getMessage().contains("unreachable")) {
//                    log.info("Connect to the server " + host + " failed");
//                    this.socket = reconnectToServer(host, port);
//                } else throw e;
//                if (socket == null)
//                    throw new ConnectClientException("None of the attempts to reconnect to host " + host + " failed");
//            }
//            log.info("Connect to server " + host + " " + " established");
//            sessionKey = keyManager.getKey();
//            log.debug("Session key loaded");
//            inpStrm = socket.getInputStream();
//            outStrm = socket.getOutputStream();
//
//            ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
//            ObjectOutputStream oos = new ObjectOutputStream(baos);
//            oos.writeObject(this);
//            baos.writeTo(outStrm);
//            log.debug("Client identity sent to server");
//            keyManager.removeKey(sessionKey);
//            socket.getInputStream().read(message);
//            log.info(new String(message));
//        } catch (IOException e) {
//            log.error(e);
//        }
//    }

//    protected Socket reconnectToServer(String host, int port) throws ConnectClientException {
//        Socket socket = null;
//        for (int i = 0; i < NUMBER_OF_CONNECTION_ATTEMPTS; i++) {
//            log.info("Reconnect to server host " + host + "...");
//            try {
//                Thread.sleep(TIMEOUT);
//                socket = connect(host, port);
//                return socket;
//            } catch (ConnectClientException e) {
//                if (e.getMessage().endsWith("unreachable")) {
//                    log.info("Connect to the server " + host + " failed");
//                } else throw e;
//            } catch (InterruptedException e) {
//                log.debug("Reconnect to the host " + host + " interrupted", e);
//            }
//        }
//        return socket;
//    }

    public String getSessionKey() {
        return sessionKey;
    }

//    public Socket getSocket() {
//        return socket;
//    }
//
//    public void setHost(String host) {
//        this.host = host.replace("/", "");
//    }
//
    public OutputStream getOutStrm() {
        return connection.getOutputStream();
    }

    public InputStream getInpStrm() {
        return connection.getInputStream();
    }
//
//    public void setOutStrm(OutputStream outStrm) {
//        this.outStrm = outStrm;
//    }
//
//    public void setInpStrm(InputStream inpStrm) {
//        this.inpStrm = inpStrm;
//    }

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

//    public void setPort(int port) {
//        this.port = port;
//    }
//
//    public void setSocket(Socket socket) {
//        this.socket = socket;
//    }

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
