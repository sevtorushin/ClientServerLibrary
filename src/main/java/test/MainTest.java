package test;

import clients.another.ClientTest;
import connect.SocketConnection;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class MainTest {
    static ClientTest client;

    public static void main(String[] args) throws IOException, InterruptedException {

        SocketConnection connection = new SocketConnection(new InetSocketAddress("127.0.0.1", 7000));
        connection.connect();

        byte[] bytes = new byte[512];
        while (true) {
            int b = connection.read(bytes);
            Thread.sleep(100);
            if (b > 0)
                System.out.println(Arrays.toString(bytes));
            Arrays.fill(bytes, (byte) 0);
        }

//        Thread.sleep(30000);

        //-----------------------------------------------------------------------------
//        ServerTest server = new ServerTest(5500);
//        server.start();
        //-----------------------------------------------------------------------------
//
//        client = new ClientTest("127.0.0.1", 7000, true);
//        SocketChannel channel = SocketChannel.open();
//        channel.connect(new InetSocketAddress("127.0.0.1", 5500));
//        channel.configureBlocking(false);
//
//        SocketChannel channel1 = SocketChannel.open();
//        channel1.connect(new InetSocketAddress("127.0.0.1", 5550));
//        channel1.configureBlocking(false);
//
//        //-----------------------------------------------------------------------------
//
//        print();
//        transfer("1", channel);
//        transfer("2", channel1);
//        cache();
//        toFile(Path.of("e:\\log.log"));
//
//        //-----------------------------------------------------------------------------
//
//        client.connect();
//        CompletableFuture.runAsync(() -> {
//            while (true) {
//                client.receiveMessage();
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//
//        Thread.sleep(10000);
//        System.out.println(new String(client.readAllCache().array()));
    }

    private static void print() {
        client.addTask("print", new MessageHandler() {
            @Override
            public void incomingMessageHandle(ByteBuffer message) throws Exception {
                System.out.println(new String(message.array()));
            }

            @Override
            public void outgoingMessageHandle(ByteBuffer message) throws Exception {
                incomingMessageHandle(message);
            }
        });
    }

    private static void transfer(String name, SocketChannel channel) {
        client.addTask(name, new MessageHandler() {
            @Override
            public void incomingMessageHandle(ByteBuffer message) throws Exception {
                outgoingMessageHandle(message);
            }

            @Override
            public void outgoingMessageHandle(ByteBuffer message) throws Exception {
                try {
                    channel.write(message);
                } catch (IOException e) {
                    System.err.println("!!!!!!!!!!!!!!!!!!!!");
                }
            }
        });
    }

    private static void toFile(Path path) {
        client.addTask("toFile", new MessageHandler() {
            @Override
            public void incomingMessageHandle(ByteBuffer message) throws Exception {
                try (FileOutputStream file = new FileOutputStream(path.toString(), true)) {
                    file.getChannel().write(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void outgoingMessageHandle(ByteBuffer message) throws Exception {
            }
        });
    }

    private static void cache() {
        client.addTask("cache", new MessageHandler() {
            @Override
            public void incomingMessageHandle(ByteBuffer message) throws Exception {
                client.saveToCache(message);
            }

            @Override
            public void outgoingMessageHandle(ByteBuffer message) throws Exception {

            }
        });
    }
}
