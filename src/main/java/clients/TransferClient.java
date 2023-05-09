package clients;

import check.ClientValidator;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class TransferClient extends AbstractClient implements Transmittable {
    private String identifier = "urg-u66-6603";

    public TransferClient(String host, int port) {
        super(host, port);
        setValidator(new ClientValidator("C:\\Users\\Public\\client_key.txt", this));
    }

    @Override
    public void sendBytes(byte[] bytes) {
        try {
            getOutStrm().write(bytes);
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

    public void startTransferTo(String anotherHost, int anotherPort){
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

    public String getIdentifier() {
        return identifier;
    }
}
