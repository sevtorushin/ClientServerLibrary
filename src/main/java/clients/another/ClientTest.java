package clients.another;

import connect.ClientConnection;
import connect.SocketChannelConnection;
import test.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

public class ClientTest implements AutoCloseable{
    private String name;
    private long id;
    private static int clientCount = 0;
    private ClientConnection clientConnection;
    private TaskManager taskManager;
    private MessageStorage messageStorage;

    public ClientTest(SocketChannel socketChannel) {
        this.clientConnection = new SocketChannelConnection(socketChannel);
        this.name = "";
        this.id = ++clientCount;
        this.taskManager = new TaskManager();
        this.messageStorage = new MessageStorage();
    }

    public ClientTest(InetSocketAddress endpoint) {
        this.clientConnection = new SocketChannelConnection(endpoint);
        this.name = "";
        this.id = ++clientCount;
        this.taskManager = new TaskManager();
        this.messageStorage = new MessageStorage();
    }

    public ClientTest(String host, int port) {
        this.clientConnection = new SocketChannelConnection(host, port);
        this.name = "";
        this.id = ++clientCount;
        this.taskManager = new TaskManager();
        this.messageStorage = new MessageStorage();
    }

    public void connect() {
        clientConnection.connect();
    }

    public void disconnect() {
        clientConnection.disconnect();
    }

    public boolean isConnected() {
        return clientConnection.isConnected();
    }

    public void receiveMessage() {
        if (!isConnected())
            return;
        try {
            ByteBuffer buffer = messageStorage.getTempBuffer();
            clientConnection.read(buffer);
            taskManager.handleAllIncomingMessage(buffer);
        } catch (IOException e) {
            clientConnection.disconnect();
            clientConnection.reconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(ByteBuffer message) {
        if (!isConnected())
            return;
        try {
            taskManager.handleAllOutgoingMessage(message);
        } catch (IOException e) {
            clientConnection.disconnect();
            clientConnection.reconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClientConnection getClientConnection() {
        return clientConnection;
    }

    @Override
    public String toString() {
        return "ClientTest{" +
                ", connection=" + clientConnection +
                ", name='" + name + '\'' +
                ", id=" + id +
                '}';
    }

    @Override
    public void close() {
        clientConnection.disconnect();
        taskManager.removeAllTask();
        messageStorage.clear();
    }

    //----------------------------------------------------------------------

    //todo вынести в отдельного клиента

    public void addTask(String name, MessageHandler handler) {
        this.taskManager.addTask(name, handler);
    }

    public void removeTask(String name){
        this.taskManager.removeTask(name);
    }

    public List<String> getALLTask(){
        return taskManager.getALLTask();
    }

    public void removeAllTask(){
        taskManager.removeAllTask();
    }

    public void saveToCache(ByteBuffer message){
        messageStorage.saveToCache(message);
    }

    public ByteBuffer readAllCache(){
        return messageStorage.readAllCache();
    }

    public ByteBuffer readElementCache(){
        return messageStorage.readElementCache();
    }
}
