package clients;

import check.ClientValidator;
import check.Validator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class AbstractClient {
    private Socket socket;
    private final String host;
    private final int port;
    private OutputStream outStrm;
    private InputStream inpStrm;
    private Validator validator;

    public AbstractClient(String host, int port) {
        this.host = host;
        this.port = port;
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
        this.socket = setSocket(host, port);
        validator.authenticate(socket);
//        validator.authorize(socket);
//        validator.verify(socket);
        try {
            inpStrm = socket.getInputStream();
            outStrm = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void setValidator(Validator validator) {
        this.validator = validator;
    }
}
