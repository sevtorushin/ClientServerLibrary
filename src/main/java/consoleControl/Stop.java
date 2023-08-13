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
import java.util.List;

@CommandLine.Command(name = "stop", aliases = {"-stop"})
public class Stop implements Runnable {

    private final SimpleServerController serverController = SimpleServerController.getInstance();
    private final SimpleClientController clientController = SimpleClientController.getInstance();

    private static final Logger log = LogManager.getLogger(Stop.class.getSimpleName());

    @Override
    public void run() {

    }

    @CommandLine.Command(name = "server", aliases = {"-server", "-s"})
    void stopServer(@CommandLine.Option(names = "-p") int port,
                    @CommandLine.Option(names = {"-all", "all"}) boolean all) {
        ConnectionUtils.isValidPort(port);

        if (port == 0 && all) {
            try {
                List<SimpleServer> serverList = serverController.stopAllServers();
                log.debug("All servers has been stopped");
                System.out.println("These servers have been stopped:");
                serverList.forEach(System.out::println);
            } catch (IOException e) {
                log.error("Servers stop error", e);
            }
        } else if (!all && port != 0) {
            try {
                SimpleServer server = serverController.stop(port);
                log.info(String.format("Server %s have been stopped", server));
            } catch (IOException e) {
                log.error("Server stop error", e);
            }
        }
    }

    @CommandLine.Command(name = "client", aliases = {"-client", "-c"})
    void stopClient(@CommandLine.Option(names = "-h") String host,
                    @CommandLine.Option(names = "-p") int port,
                    @CommandLine.Option(names = "-id") int id,
                    @CommandLine.Option(names = {"-all", "all"}) boolean all) {
        ConnectionUtils.isValidPort(port);

        if (all && port == 0 && host == null && id == 0) {
            try {
                List<SimpleClient> clientList = clientController.stopAllClients();
                log.debug("All clients have been disconnected");
                System.out.println("These clients have been disconnected:");
                clientList.forEach(System.out::println);
            } catch (IOException e) {
                log.error("Clients stop error", e);
            }
        } else if (!all && port != 0 && host != null) {
            try {
                SimpleClient client = clientController.stop(host, port, id);
                log.info(String.format("Client %s have been disconnected", client));
            } catch (IOException e) {
                log.error("Client stop error", e);
            }
        }
    }
}