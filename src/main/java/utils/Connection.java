package utils;

import exceptions.ConnectClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.channels.SocketChannel;

public class Connection implements AutoCloseable {
    private InetSocketAddress socketAddress;
    private SocketChannel socketChannel;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private final int TIMEOUT = 2000;
    private final int NUMBER_OF_CONNECTION_ATTEMPTS = 5;
    private static final Logger log = LogManager.getLogger(Connection.class.getSimpleName());

    public Connection(String host, int port) {
        this.socketAddress = new InetSocketAddress(host, port);
    }

    public Connection() {
    }

    public void connect() throws ConnectClientException {
        try {
            if (isReachedHost(getHost())) {
                if (this.socket == null)
                    this.socket = new Socket(getHost(), getPort());
                this.inputStream = socket.getInputStream();
                this.outputStream = socket.getOutputStream();
//                socketChannel = SocketChannel.open();
//                socketChannel.connect(socketAddress);
            } else throw new ConnectClientException("The specified endpoint " + getHost() + " unreachable");
        } catch (UnknownHostException e) {
            log.debug("Unknown host ", e);
            throw new ConnectClientException("Unknown host " + getHost());
        } catch (ConnectException e) {
            if (e.getMessage().contains("timed out")) {
                log.debug("Connection to server " + getHost() + " timed out", e);
                throw new ConnectClientException(e.getMessage());
            } else if (e.getMessage().equals("Connection refused: connect")) {
                log.debug("The server is not running on the specified endpoint " + getPort(), e);
                throw new ConnectClientException("The server is not running on the specified endpoint " + getPort());
            }
        } catch (IOException e) {
            throw new ConnectClientException(e.getMessage());
        }
    }

    public void bind(Socket socket) throws ConnectClientException {
        try {
            if (socket == null)
                throw new ConnectClientException("Socket can't be null");
            if (socket.isClosed())
                throw new SocketException("Socket is closed");
            if (!socket.isConnected()) {
                throw new SocketException("Socket not connected");
            }
            this.socket = socket;
            setSocketAddress(new InetSocketAddress(socket.getInetAddress().getHostAddress(),
                    socket.getPort()));
            connect();
        } catch (IOException e) {
            throw new ConnectClientException(e.getMessage());
        }
    }

//    public void connect() throws ConnectClientException {
//        try {
//            socketChannel = SocketChannel.open();
//            if (isReachedHost(getHost()))
//                socketChannel.connect(socketAddress);
//            else
//                throw new ConnectClientException("The specified endpoint " + getHost() + " unreachable");
//            this.socket = socketChannel.socket();
//            this.inputStream = socket.getInputStream();
//            this.outputStream = socket.getOutputStream();
//        } catch (UnknownHostException e) {
//            log.debug("Unknown host ", e);
//            throw new ConnectClientException("Unknown host " + getHost());
//        } catch (ConnectException e) {
//            if (e.getMessage().contains("timed out")) {
//                log.debug("Connection to server " + getHost() + " timed out", e);
//                throw new ConnectClientException(e.getMessage());
//            }
//        } catch (IOException e) {
//            if (e.getMessage().equals("Connection refused: connect")) {
//                log.debug("The server is not running on the specified endpoint " + getPort(), e);
//                throw new ConnectClientException("The server is not running on the specified endpoint " + getPort());
//            } else throw new ConnectClientException(e.getMessage());
//        }
//    }

    public boolean reconnectToServer() throws ConnectClientException {
        for (int i = 0; i < NUMBER_OF_CONNECTION_ATTEMPTS; i++) {
            log.info("Reconnect to server host " + getHost() + "...");
            try {
                Thread.sleep(TIMEOUT);
                connect();
                return true;
            } catch (ConnectClientException e) {
                if (e.getMessage().endsWith("unreachable")) {
                    log.info("Connect to the server " + getHost() + " failed");
                } else throw e;
            } catch (InterruptedException e) {
                log.debug("Reconnect to the host " + getHost() + " interrupted", e);
            }
        }
        log.error("None of the attempts to reconnect to host " + getHost() + " failed");
        return false;
    }

    public boolean isReachedHost(String host) throws IOException {
        InetAddress address;
        boolean isReached;
        try {
            address = InetAddress.getByName(host);
            isReached = address.isReachable(TIMEOUT);
        } catch (IOException e) {
            if (e instanceof UnknownHostException)
                throw new UnknownHostException();
            throw e;
        }
        return isReached;
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
        }
        if (outputStream != null) {
            outputStream.close();
            outputStream = null;
        }
        if (socket != null) {
            socket.close();
            socket = null;
        }
        if (socketChannel != null) {
            socketChannel.close();
            socketChannel = null;
        }
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    public Socket getSocket() {
        return socket;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public String getHost() {
        return socketAddress.getHostString();
    }

    public int getPort() {
        return socketAddress.getPort();
    }

    public void setSocketAddress(InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    public boolean isClosed() {
        return socket == null || socket.isClosed();
    }

    @Override
    public String toString() {
        return "Connection{" +
                "socket=" + socket +
                ", inputStream=" + inputStream +
                ", outputStream=" + outputStream +
                '}';
    }
}
