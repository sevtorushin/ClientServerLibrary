package clients;

import check.KeyManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;

public class AbstractClient implements Serializable {
    private String id;
    private String sessionKey;
    private transient Socket socket;
    private String host;
    private int port;
    private transient OutputStream outStrm;
    private transient InputStream inpStrm;
    private transient final KeyManager keyManager;
    private transient volatile boolean isWriteToCache = false;
    private transient volatile boolean isReadFromCache = false;
    private transient static final Logger log = LogManager.getLogger(AbstractClient.class.getSimpleName());

    public AbstractClient(String host, int port, String id, KeyManager keyManager) {
        this.host = host;
        this.port = port;
        this.id = id;
        this.keyManager = keyManager;
    }

    protected Socket setSocket(String hostName, int port) {
        Socket socket = null;
        try {
            socket = new Socket(InetAddress.getByName(hostName), port);
        } catch (UnknownHostException e) {
            log.error("Unknown host " + e);
        } catch (ConnectException e) {
            log.error("The server is not running on the specified endpoint " + port + "\r\n" + e);
        } catch (IOException e) {
            log.error(e);
        }
        return socket;
    }

    public void connectToServer() {
        byte[] message = new byte[512];
        try {
            keyManager.removeKey(sessionKey);
            this.socket = setSocket(host, port);
            log.debug("Connect to server " + host + " " + " established");
            sessionKey = keyManager.getKey();
            log.debug("Session key loaded");
            inpStrm = socket.getInputStream();
            outStrm = socket.getOutputStream();

            ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            baos.writeTo(outStrm);
            log.debug("Client identity sent to server");
            keyManager.removeKey(sessionKey);
            socket.getInputStream().read(message);
            log.info(new String(message));
        } catch (IOException e) {
            log.error(e);
        }
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setHost(String host) {
        this.host = host.replace("/", "");
    }

    public OutputStream getOutStrm() {
        return outStrm;
    }

    public InputStream getInpStrm() {
        return inpStrm;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getHost() {
        return host.replace("/", "");
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractClient client = (AbstractClient) o;
        return  Objects.equals(id, client.id) &&
                Objects.equals(host, client.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, host);
    }
}
