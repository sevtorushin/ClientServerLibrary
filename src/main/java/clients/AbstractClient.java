package clients;

import check.AbstractValidator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class AbstractClient {
    private String id;
    private String sessionKey;
    private Socket socket;
    private final String host;
    private final int port;
    private OutputStream outStrm;
    private InputStream inpStrm;
    private AbstractValidator validator;

    public AbstractClient(String host, int port, String id, AbstractValidator validator) {
        this.host = host;
        this.port = port;
        this.id = id;
        this.validator = validator;
    }

    protected Socket setSocket(String hostName, int port) {
        Socket socket = null;
        try {
            socket = new Socket(InetAddress.getByName(hostName), port);
        } catch (UnknownHostException e) {
            System.err.println("Unknown host " + e);
        } catch (ConnectException e) {
            System.err.println("The server is not running on the specified endpoint\r\n" + e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return socket;
    }

    public void connectToServer() {
        try {
            validator.getKeyManager().removeKey(sessionKey);
            this.socket = setSocket(host, port);
            sessionKey = validator.getKeyManager().getKey();
            inpStrm = socket.getInputStream();
            outStrm = socket.getOutputStream();

            outStrm.write((id + "\r\n").getBytes());
            outStrm.write(sessionKey.getBytes());
            validator.getKeyManager().removeKey(sessionKey);
//            validator.authenticate(socket);
//            validator.authorize(socket);
//            validator.verify(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public Socket getSocket() {
        return socket;
    }

    public OutputStream getOutStrm() {
        return outStrm;
    }

    public InputStream getInpStrm() {
        return inpStrm;
    }

    public String getId() {
        return id;
    }
}
