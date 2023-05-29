package clients;

import check.KeyManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servers.Receivable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class TransferClient extends AbstractClient implements Transmittable, Receivable {

    private transient final ByteBuffer buffer = ByteBuffer.allocate(512);
    private static final Logger log = LogManager.getLogger(TransferClient.class.getSimpleName());

    public TransferClient(String host, int port, String id) {
        super(host, port, id,
                new KeyManager("c:\\users\\public\\client_keys.txt")); //todo вынести путь к ключам наружу
    }

    @Override
    public void sendBytes(byte[] bytes) {
        buffer.put((getId() + "\r\n").getBytes()).put((getSessionKey() + "\r\n").getBytes()).put(bytes);
        try {
            getOutStrm().write(buffer.array());
            buffer.clear();
        } catch (SocketException e) {
            System.out.println("Connection was reset. Reconnect...");
            connectToServer();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] receiveBytes(String source) {
        try {
            getInpStrm().read(buffer.array());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.array();
    }

    public void startTransferTo(String anotherHost, int anotherPort) {
        try {
            Socket anotherSocket = setSocket(anotherHost, anotherPort);

            InputStream is = getInpStrm();
            OutputStream os = anotherSocket.getOutputStream();
            new Thread(() -> {
                try {
                    is.transferTo(os);
                } catch (IOException e) {
                    e.printStackTrace(); //todo добавить логер. При отключении another сервера выбрасывается исключение
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startTransferFrom(String anotherHost, int anotherPort) {
        try {
            Socket anotherSocket = setSocket(anotherHost, anotherPort);
            if (anotherSocket == null) {
                log.error("Connection to node " + anotherHost + " " + anotherPort + " not established");
                return;
            }
            InputStream is = anotherSocket.getInputStream();
            OutputStream os = getOutStrm();
            new Thread(() -> {
                try {
                    is.transferTo(os);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
