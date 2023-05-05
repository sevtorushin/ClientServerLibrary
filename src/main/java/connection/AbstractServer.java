package connection;

import exceptions.DisconnectedException;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

public abstract class AbstractServer implements Transmit, Closeable {
    private final ServerSocket serverSocket;
    private final Socket socket;
    private InputStream inpStrm;
    private OutputStream outStrm;
    protected byte[] buf;
//    private byte[] tempBuf = new byte[512];
    private final int DEFAULT_BUFFER_SIZE = 512;

    public AbstractServer(int port) throws IOException {
            serverSocket =getServerSocket(port);
            socket = serverSocket.accept();
            inpStrm = socket.getInputStream();
            outStrm = socket.getOutputStream();
            buf = new byte[DEFAULT_BUFFER_SIZE];
    }

    @Override
    public byte[] receiveBytes() throws DisconnectedException, IOException {
        inpStrm.read();  //skip first marker byte
        if (inpStrm.read(buf) == -1)
            throw new DisconnectedException("Client is disconnected");
        return buf;
    }

    @Override
    public void sendBytes(byte[] bytes) throws IOException {
            outStrm.write(bytes);
    }

    private ServerSocket getServerSocket(int port) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), port));
        } catch (BindException e) {
            System.err.println("Port is not available. Please use another port" + e);
            System.exit(-1);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return serverSocket;
    }

    public Socket getSocket() {
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
        if (serverSocket != null)
            serverSocket.close();
    }
}
