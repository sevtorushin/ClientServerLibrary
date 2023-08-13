package consoleControl;

import clients.simple.SimpleClient;
import clients.simple.SimpleClientController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servers.simple.SimpleServerController;
import picocli.CommandLine;
import servers.simple.SimpleServer;
import utils.ConnectionUtils;

import java.nio.channels.SocketChannel;
import java.rmi.NoSuchObjectException;
import java.util.List;

@CommandLine.Command(name = "get", aliases = {"-get"})
public class Get implements Runnable {
    private final SimpleServerController serverController = SimpleServerController.getInstance();
    private final SimpleClientController clientController = SimpleClientController.getInstance();

    private static final Logger log = LogManager.getLogger(Get.class.getSimpleName());

    @Override
    public void run() {
    }

    @CommandLine.Command(name = "server", aliases = {"-server", "-s"})
    void getServer(@CommandLine.Option(names = "-p") int port,
                   @CommandLine.Option(names = {"-all", "all"}, required = true) boolean all) throws NoSuchObjectException {
        ConnectionUtils.isValidPort(port);

        if (port == 0 && all) {
            List<SimpleServer> serverList = serverController.getAllServers();
            if (serverList.isEmpty())
                System.out.println("none");
            else
                serverList.forEach(System.out::println);
            log.debug("Result printed in console");
        } else if (port != 0 && all) {
            List<SocketChannel> clients = serverController.getAllClients(port);
            if (clients.isEmpty())
                System.out.println("none");
            else
                clients.forEach(System.out::println);
            log.debug("Result printed in console");
        }
    }

    @CommandLine.Command(name = "client", aliases = {"-client", "-c"})
    void getClient(@CommandLine.Option(names = {"-all", "all"}, required = true) boolean all) {
        List<SimpleClient> clients = clientController.getAllClients();
        if (clients.isEmpty())
            System.out.println("none");
        else
            clients.forEach(System.out::println);
        log.debug("Result printed in console");
    }
}
