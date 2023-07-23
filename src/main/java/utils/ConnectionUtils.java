package utils;

import exceptions.ConnectClientException;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
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

    public static boolean isFreePort(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return serverSocket.getLocalPort() == port;
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Specify port %d is busy", port));
        }
    }

    public static boolean isRunServer(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            return true;
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format(
                    "Server is not run on the specify endpoint %s:%d", host, port));
        }
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

    public static boolean isValidPort(int port) {
        String regexp = "^((6553[0-5])|(655[0-2][0-9])|(65[0-4][0-9]{2})|(6[0-4][0-9]{3})|([1-5][0-9]{4})|([0-5]{0,5})|([0-9]{1,4}))$";
        boolean isValid = String.valueOf(port).matches(regexp);
        if (!isValid)
            throw new IllegalArgumentException(String.format("Specify port %d is not valid", port));
        else
            return true;
    }

    public static boolean isValidHost(String host) {
        String regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        boolean isValid = host.matches(regexp);
        if (!isValid)
            throw new IllegalArgumentException(String.format("Specify host %s is not valid", host));
        else
            return true;
    }
}
