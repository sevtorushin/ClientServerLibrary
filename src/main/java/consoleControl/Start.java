package consoleControl;

import clients.simple.SimpleClient;
import clients.simple.SimpleClientController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servers.simple.SimpleServerController;
import picocli.CommandLine;
import servers.simple.SimpleServer;
import utils.ConnectionUtils;

import java.io.IOException;

@CommandLine.Command(name = "start", aliases = {"-start"})
public class Start implements Runnable {
    private final SimpleServerController serverController = SimpleServerController.getInstance();
    private final SimpleClientController clientController = SimpleClientController.getInstance();

    private static final Logger log = LogManager.getLogger(Start.class.getSimpleName());

    @Override
    public void run() {
    }

    @CommandLine.Command(name = "server", aliases = {"-server", "-s"})
    SimpleServer startServer(@CommandLine.Option(names = "-p", required = true) int port,
                             @CommandLine.Option(names = "-nc", defaultValue = "1") int maxClient) throws IOException {

        boolean isValidPort = ConnectionUtils.isValidPort(port) && ConnectionUtils.isFreePort(port);
        SimpleServer server = null;
        if (isValidPort) {
            if (maxClient < 1) {
                log.error(String.format("Specified max number client %d for server not valid", maxClient));
                return null;
            }
            server = serverController.create(port, maxClient);
            log.info(String.format("Server %s started on port %d", server, (server.getPort())));
        }
        return server;
    }

    @CommandLine.Command(name = "client", aliases = {"-client", "-c"})
    SimpleClient startClient(@CommandLine.Option(names = "-h", required = true) String host,
                             @CommandLine.Option(names = "-p", required = true) int port) throws IOException {

        boolean isValidPort = ConnectionUtils.isValidPort(port);
        boolean isValidHost;
        try {
            ConnectionUtils.isValidHost(host);
            isValidHost = ConnectionUtils.isReachedHost(host);
        } catch (IOException e) {
            log.error("Connection to endpoint " + host + " failed", e);
            return null;
        }
        SimpleClient client = null;
        if (isValidPort && isValidHost) {
            client = clientController.create(host, port);
            log.info(String.format("Client connected to server %s: %d",
                    client.getHost(),
                    client.getPort()));
        }
        return client;
    }
}