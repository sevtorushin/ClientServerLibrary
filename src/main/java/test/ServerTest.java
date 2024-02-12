package test;

import clients.another.Client;
import service.ClientPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;

public class ServerTest implements Runnable {

    private final ServerSocketChannel serverSocketChannel;
    private ClientPool clientPool;
    private boolean stopped;

    public ServerTest(int port) throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(false);
        this.clientPool = new ClientPool();
    }

    @Override
    public void run() {
        while (!isStopped()) {
            try {
                SocketChannel clientSocket = serverSocketChannel.accept();
                if (clientSocket != null) {
                    connectClient(clientSocket);
                }
                Thread.sleep(500);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        new Thread(this).start();
    }

    public void stop() throws IOException {
        stopped = true;
        disconnectAllClients();
        serverSocketChannel.close();
    }

    private Client connectClient(SocketChannel client) {
        Client clientTest = clientPool.createClient(client);
        clientPool.addNewClient(clientTest);
        return clientTest;
    }

    public boolean disconnectClient(Client client) throws IOException {
        if (client == null)
            return false;
        return clientPool.removeClient(client);
    }

    public boolean disconnectAllClients() throws IOException {
        return clientPool.removeAllClients();
    }

    public List<Client> getAllClients() {
        return clientPool.getAllClients();
    }

    public boolean isStopped() {
        return stopped;
    }
}
