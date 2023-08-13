package consoleControl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servers.simple.SimpleServerController;
import picocli.CommandLine;
import utils.ConnectionUtils;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

@CommandLine.Command(name = "remove", aliases = {"-remove"})
public class Remove implements Runnable {
    private final SimpleServerController serverController = SimpleServerController.getInstance();
    private static final Logger log = LogManager.getLogger(Remove.class.getSimpleName());

    @Override
    public void run() {
    }

    @CommandLine.Command(name = "server", aliases = {"-server", "-s"})
    void removeClients(@CommandLine.Option(names = "-p", required = true) int serverPort,
                       @CommandLine.Option(names = {"-all", "all"}) boolean all,
                       @CommandLine.Option(names = "-cp") int clientPort) {
        ConnectionUtils.isValidPort(serverPort);
        ConnectionUtils.isValidPort(clientPort);

        if (!all && clientPort != 0) {
            try {
                SocketChannel channel = serverController.removeClient(serverPort, clientPort);
                log.info(String.format("Client %s: %d have been disconnected",
                        channel.socket().getInetAddress().getHostAddress(), channel.socket().getPort()));
            } catch (IOException e) {
                if (e.getMessage().equals("No such client"))
                    log.warn("Client with specified port " + clientPort + " is missing");
                else log.error("Channel close error", e);
            }
        } else if (all && clientPort == 0) {
            try {
                List<SocketChannel> channelList = serverController.removeAllClients(serverPort);
                log.debug("All clients have been disconnected");
                System.out.println("These clients have been disconnected:");
                channelList.forEach(socketChannel -> System.out.println(
                        socketChannel.socket().getInetAddress().getHostAddress() + ": " +
                                socketChannel.socket().getPort()));
            } catch (IOException e) {
                log.error("Channel close error", e);
            }
        }
    }
}
