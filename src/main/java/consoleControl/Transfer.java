package consoleControl;

import clients.simple.SimpleClientController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.StringMapMessage;
import servers.simple.SimpleServerController;
import picocli.CommandLine;
import utils.ConnectionUtils;

import java.io.IOException;
import java.rmi.NoSuchObjectException;

@CommandLine.Command(name = "transfer", aliases = {"-transfer", "tr", "-tr"}, subcommands = {Transfer.Begin.class, Transfer.Break.class})
public class Transfer implements Runnable {

    private static final Logger log = LogManager.getLogger(Transfer.class.getSimpleName());

    @Override
    public void run() {
    }

    @CommandLine.Command(name = "begin", aliases = {"-begin", "bg", "-bg"})
    static class Begin implements Runnable {
        private final SimpleServerController serverController = SimpleServerController.getInstance();
        private final SimpleClientController clientController = SimpleClientController.getInstance();

        @Override
        public void run() {
        }

        @CommandLine.Command(name = "server", aliases = {"-server", "-s"})
        void transferFromServer(@CommandLine.Option(names = "-p", required = true) int serverPort,
                                @CommandLine.Option(names = "-cp", required = true) int clientPort) {
            ConnectionUtils.isValidPort(serverPort);
            ConnectionUtils.isValidPort(clientPort);

            try {
                serverController.startTransferToClient(serverPort, clientPort);
                log.info(String.format("Transfer from server %d to client %d started", serverPort, clientPort));
            } catch (NoSuchObjectException e) {
                log.warn(e.getMessage());
            }
        }

        @CommandLine.Command(name = "client", aliases = {"-client", "-c"})
        void transferToServer(@CommandLine.Option(names = "-ch", required = true) String serverHost,
                              @CommandLine.Option(names = "-cp", required = true) int clientPort,
                              @CommandLine.Option(names = "-id") int id,
                              @CommandLine.Option(names = "-sh", required = true) String anotherServerHost,
                              @CommandLine.Option(names = "-sp", required = true) int anotherServerPort) throws IOException {
            ConnectionUtils.isValidPort(clientPort);
            ConnectionUtils.isValidPort(anotherServerPort);
            ConnectionUtils.isValidHost(serverHost);
            ConnectionUtils.isValidHost(anotherServerHost);
            ConnectionUtils.isReachedHost(serverHost);
            ConnectionUtils.isReachedHost(anotherServerHost);
            try {
                clientController.startTransferToServer(serverHost, clientPort, id, anotherServerHost, anotherServerPort);
                log.info(String.format("Transfer from client %s: %d to server %s: %d started",
                        serverHost, clientPort, anotherServerHost, anotherServerPort));
            } catch (IOException e) {
                if (e.getMessage().equals("No such client"))
                    log.warn(e.getMessage());
                else log.error(e);
            }
        }
    }

    @CommandLine.Command(name = "break", aliases = {"-break", "br", "-br"})
    static class Break implements Runnable {
        private final SimpleServerController serverController = SimpleServerController.getInstance();
        private final SimpleClientController clientController = SimpleClientController.getInstance();

        @Override
        public void run() {
        }

        @CommandLine.Command(name = "server", aliases = {"-server", "-s"})
        void transferFromServer(@CommandLine.Option(names = "-p", required = true) int serverPort,
                                @CommandLine.Option(names = "-cp", required = true) int clientPort) {
            ConnectionUtils.isValidPort(serverPort);

            try {
                serverController.stopTransferToClient(serverPort, clientPort);
                log.info(String.format("Transfer from server %d stopped", serverPort));
            } catch (NoSuchObjectException e) {
                log.warn("Server on specified port " + serverPort + " is missing");
            }
        }

        @CommandLine.Command(name = "client", aliases = {"-client", "-c"})
        void transferToServer(@CommandLine.Option(names = "-ch", required = true) String serverHost,
                              @CommandLine.Option(names = "-cp", required = true) int clientPort,
                              @CommandLine.Option(names = "-id") int id,
                              @CommandLine.Option(names = "-sh", required = true) String anotherServerHost,
                              @CommandLine.Option(names = "-sp", required = true) int anotherServerPort) throws IOException {
            ConnectionUtils.isValidPort(clientPort);
            ConnectionUtils.isValidHost(serverHost);
            ConnectionUtils.isReachedHost(serverHost);
            try {
                clientController.stopTransferToServer(serverHost, clientPort, id, anotherServerHost, anotherServerPort);
                log.info(String.format("Transfer from client %s: %d to server %d stopped",
                        serverHost, clientPort, anotherServerPort));
            } catch (IOException e) {
                if (e.getMessage().equals("No such client"))
                    log.warn("Client with specified endpoint " + serverHost + ": " + clientPort + " is missing");
                else log.error("Close channel error", e);
            }
        }
    }
}
