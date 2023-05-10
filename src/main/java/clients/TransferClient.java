package clients;

import check.KeyManager;
import check.TransferClientValidator;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class TransferClient extends AbstractClient implements Transmittable {

    private final ByteBuffer buffer = ByteBuffer.allocate(512);

    public TransferClient(String host, int port, String id) {
        super(host, port, id,
                new TransferClientValidator(new KeyManager("c:\\users\\public\\client_keys.txt")));
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

    public void startTransferTo(String anotherHost, int anotherPort) {
        try {
            Socket anotherSocket = setSocket(anotherHost, anotherPort);
            OutputStream os = anotherSocket.getOutputStream();
            new Thread(() -> {
//                try {
//                    is.transferTo(os);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
