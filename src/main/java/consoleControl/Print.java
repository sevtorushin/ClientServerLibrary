package consoleControl;

import clients.simple.SimpleClientController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servers.simple.SimpleServerController;
import picocli.CommandLine;
import utils.ConnectionUtils;

import java.io.IOException;
import java.rmi.NoSuchObjectException;

@CommandLine.Command(name = "print", aliases = {"-print"}, subcommands = {Print.Begin.class, Print.Break.class})
public class Print implements Runnable {

    private static final Logger log = LogManager.getLogger(Print.class.getSimpleName());

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
        void printServer(@CommandLine.Option(names = "-p", required = true) int port) {
            ConnectionUtils.isValidPort(port);

            try {
                serverController.printRawReceiveData(port);
                log.info(String.format("Printing for server %d started", port));
            } catch (NoSuchObjectException e) {
                log.warn("Server on specified port " + port + " is missing");
            }
        }

        @CommandLine.Command(name = "client", aliases = {"-client", "-c"})
        void printClient(@CommandLine.Option(names = "-h", required = true) String serverHost,
                         @CommandLine.Option(names = "-p", required = true) int port,
                         @CommandLine.Option(names = "-id") int id) throws IOException {
            ConnectionUtils.isValidPort(port);
            ConnectionUtils.isValidHost(serverHost);
            ConnectionUtils.isReachedHost(serverHost);
            try {
                clientController.printRawReceiveData(serverHost, port, id);
                log.info(String.format("Printing for client %s: %d started", serverHost, port));
            } catch (NoSuchObjectException e) {
                log.warn("Client with specified endpoint " + serverHost + ": " + port + " is missing");
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
        void printServer(@CommandLine.Option(names = "-p", required = true) int port) {
            ConnectionUtils.isValidPort(port);

            try {
                serverController.stopPrinting(port);
                log.info(String.format("Printing for server %d stopped", port));
            } catch (NoSuchObjectException e) {
                log.warn("Server on specified port " + port + " is missing");
            }
        }

        @CommandLine.Command(name = "client", aliases = {"-client", "-c"})
        void printClient(@CommandLine.Option(names = "-h", required = true) String serverHost,
                         @CommandLine.Option(names = "-p", required = true) int port,
                         @CommandLine.Option(names = "-id") int id) throws IOException {
            ConnectionUtils.isValidPort(port);
            ConnectionUtils.isValidHost(serverHost);
            ConnectionUtils.isReachedHost(serverHost);
            ConnectionUtils.isRunServer(serverHost, port);
            try {
                clientController.stopPrinting(serverHost, port, id);
                log.info(String.format("Printing for client %s: %d stopped", serverHost, port));
            } catch (NoSuchObjectException e) {
                log.warn("Client with specified endpoint " + serverHost + ": " + port + " is missing");
            }
        }
    }
}
