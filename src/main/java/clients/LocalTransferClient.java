package clients;

import exceptions.ConnectClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class LocalTransferClient extends TransferClient {
    private static final Logger log = LogManager.getLogger(LocalTransferClient.class.getSimpleName());

    public LocalTransferClient(String host, int port, String id) {
        super(host, port, id, null);
    }

//    @Override
//    public void connectToServer() throws ConnectClientException {
//        byte[] message = new byte[512];
//        Socket socket = null;
//        try {
//            try {
//                socket = connect(getHost(), getPort());
//            } catch (ConnectClientException e) {
//                if (e.getMessage().contains("unreachable")) {
//                    log.info("Connect to the server " + getHost() + " failed");
//                    socket = reconnectToServer(getHost(), getPort());
//                } else throw e;
//                if (socket == null)
//                    throw new ConnectClientException("None of the attempts to reconnect to " + getHost() + " host failed");
//            }
//            connect(socket);
//            log.debug("Connect to server " + getHost() + " " + " established");
//            setSessionKey("constant");
//            setInpStrm(socket.getInputStream());
//            setOutStrm(socket.getOutputStream());
//            ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
//            ObjectOutputStream oos = new ObjectOutputStream(baos);
//            oos.writeObject(this);
//            baos.writeTo(getOutStrm());
//            log.debug("Client identity sent to server");
//            socket.getInputStream().read(message);
//            log.info(new String(message));
//        } catch (IOException e) {
//            log.error(e);
//        }
//    }

    @Override
    public void sendBytes(byte[] bytes) {
        try {
            getConnection().getOutputStream().write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void startTransferFrom(String anotherHost, int anotherPort) throws ConnectClientException {
//        Socket anotherSocket;
//        try {
//            try {
//                anotherSocket = setSocket(anotherHost, anotherPort);
//            } catch (ConnectClientException e) {
//                if (e.getMessage().contains("unreachable")) {
//                    log.info("Connect to the server " + anotherHost + " failed");
//                    anotherSocket = reconnectToServer(anotherHost, anotherPort);
//                } else throw e;
//                if (anotherSocket == null)
//                    throw new ConnectClientException("None of the attempts to reconnect to " + getHost() + " host failed");
//            }
//            InputStream is = anotherSocket.getInputStream();
//            new Thread(() -> {
//                byte[] tempBuffer = new byte[512];
//                while (true) {
//                    try {
//                        ConnectionUtils.readFromInputStreamToBuffer(is, tempBuffer);
//                        getOutStrm().write(tempBuffer);
//                    } catch (ConnectClientException | IOException e) {
//                        log.info("Connection to server " + anotherHost + " lost");
//                        break;
//                    }
//                }
//            }).start();
//        } catch (IOException e) {
//            log.error("IO Exception sending data from client to another host " + anotherHost, e);
//        }
//    }
}
