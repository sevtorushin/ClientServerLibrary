package connection;

import exceptions.DisconnectedException;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class AbstractClient implements Transmit, Closeable {
    private final Socket socket;
    private InputStream inpStrm;
    private OutputStream outStrm;
    protected byte[] buf;
    private final int DEFAULT_BUFFER_SIZE = 512;

    public AbstractClient(String hostName, int port) throws IOException {
        socket = getSocket(hostName, port);
        inpStrm = socket.getInputStream();
        outStrm = socket.getOutputStream();
        buf = new byte[DEFAULT_BUFFER_SIZE];
    }

    @Override
    public byte[] receiveBytes() throws IOException, DisconnectedException {
        inpStrm.read(buf);
        return buf;
    }

    @Override
    public void sendBytes(byte[] bytes) throws IOException {
        outStrm.write(bytes);
    }

    public Socket getSocket() {
        return socket;
    }

    private Socket getSocket(String hostName, int port) {
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

    public InputStream getInpStrm() {
        return inpStrm;
    }

    public void setInpStrm(InputStream inpStrm) {
        this.inpStrm = inpStrm;
    }

    public OutputStream getOutStrm() {
        return outStrm;
    }

    public void setOutStrm(OutputStream outStrm) {
        this.outStrm = outStrm;
    }

    @Override
    public void close() throws IOException {
        if (inpStrm != null)
            inpStrm.close();
        if (outStrm != null)
            outStrm.close();
        if (socket != null)
            socket.close();
    }
}
