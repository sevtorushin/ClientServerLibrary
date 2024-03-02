package servers.another;

import clients.another.Client;
import entity.Net;
import lombok.Getter;
import service.copy.DefaultClientManagerCopy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server implements Runnable, Net {

    private final ServerSocketChannel serverSocketChannel;
    @Getter
    private final DefaultClientManagerCopy clientManager;
    private boolean stopped;

    public Server(int port) throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(false);
        this.clientManager = new DefaultClientManagerCopy();
    }

    @Override
    public void run() {
//        while (!isStopped()) {
//            try {
//                SocketChannel clientSocket = serverSocketChannel.accept();
//                if (clientSocket != null) {
//                    Client client = connectClient(clientSocket);
//                    System.out.println(client);
//                }
//                Thread.sleep(500);
//                checkAliveSocket();
//                clientManager.getAllClients().forEach(client -> System.out.println(client.isConnected()));
//            } catch (InterruptedException | IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

//    private void checkAliveSocket() {
//        getAllClients().forEach(client -> {
//            if(!client.isConnected())
//                clientManager.removeClient(client);
//            else {
//                client.sendMessage(ByteBuffer.wrap(new byte[]{0}));
//            }
//        });
//    }

    public void start() {
        new Thread(this).start();
    }

//    public void stop() throws IOException {
//        stopped = true;
//        disconnectAllClients();
//        serverSocketChannel.close();
//    }

    private Client connectClient(SocketChannel clientSocket) {
        Client clientTest = clientManager.createClient(clientSocket, Client.class);
//        if (clientManager.addNewClient(clientTest))
//            clientTest.connect();
        return clientTest;
    }

//    public boolean disconnectClient(Client client) {
//        if (client == null)
//            return false;
//        return clientManager.removeClient(client);
//    }
//
//    public boolean disconnectAllClients() {
//        return clientManager.removeAllClients();
//    }
//
//    public List<Client> getAllClients() {
//        return clientManager.getAllClients();
//    }

    public boolean isStopped() {
        return stopped;
    }

    public int getLocalPort(){
        return serverSocketChannel.socket().getLocalPort();
    }
}
