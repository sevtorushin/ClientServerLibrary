package utils;

import exceptions.ConnectClientException;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class ConnectionUtils {
    private static final int TIMEOUT = 2000;

    public static boolean isReachedHost(String host) throws IOException {
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

    public static void readFromInputStreamToBuffer(InputStream is, byte[] buffer) throws ConnectClientException {
        readFromInputStreamToBuffer(is, buffer, Integer.MAX_VALUE);
    }

    public static void readFromInputStreamToBuffer(InputStream is, byte[] buffer, int timeout) throws ConnectClientException {
        Arrays.fill(buffer, (byte) 0);
        int checkTime = 100;
        int[] byteCount = new int[1];
        Runnable r = () -> {
            try {
                byteCount[0] = is.read(buffer);
            } catch (IOException e) {
                return;
            }
        };
        Thread t1 = new Thread(r);
        t1.setDaemon(true);
        t1.setName("Read_From_InputStream " + t1.getId());
        t1.start();
        for (int i = 0; i < timeout / checkTime; i++) {
            if (!ArrayUtils.isEmpty(buffer))
                return;
            if (byteCount[0] == -1)
                throw new ConnectClientException("Client disconnected");
            try {
                Thread.sleep(checkTime);
            } catch (InterruptedException e) {
                e.printStackTrace(); //todo как-то обработать
            }
        }
        t1.stop();
        throw new ConnectClientException("Client disconnected");
    }
}
