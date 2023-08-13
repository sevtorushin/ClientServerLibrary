package consoleControl;

import clients.simple.SimpleClientController;
import servers.simple.SimpleServerController;
import picocli.CommandLine;
import utils.ConnectionUtils;

import java.io.IOException;
import java.rmi.NoSuchObjectException;

@CommandLine.Command(name = "cache", aliases = {"-cache", "ch", "-ch"}, subcommands = {Cache.Begin.class, Cache.Break.class})
public class Cache implements Runnable {

    @Override
    public void run() {
    }

    @CommandLine.Command(name = "begin", aliases = {"-begin", "bg", "-bg"})
    static
    class Begin implements Runnable {
        private final SimpleServerController serverController = SimpleServerController.getInstance();
        private final SimpleClientController clientController = SimpleClientController.getInstance();

        @Override
        public void run() {
        }

        @CommandLine.Command(name = "server", aliases = {"-server", "-s"})
        void cacheServer(@CommandLine.Option(names = "-p", required = true) int port) {
            ConnectionUtils.isValidPort(port);

            try {
                serverController.startCaching(port);
                System.out.printf("Caching for server %d started\n", port);
            } catch (IOException e) {
                e.printStackTrace(); //todo логировать
            }
        }

        @CommandLine.Command(name = "client", aliases = {"-client", "-c"})
        void cacheClient(@CommandLine.Option(names = "-h", required = true) String serverHost,
                         @CommandLine.Option(names = "-p", required = true) int port,
                         @CommandLine.Option(names = "-id") int id) throws IOException {
            ConnectionUtils.isValidPort(port);
            ConnectionUtils.isValidHost(serverHost);
            ConnectionUtils.isReachedHost(serverHost);
            try {
                    clientController.startCaching(serverHost, port, id);
                System.out.printf("Caching for client %s: %d started\n", serverHost, port);
            } catch (IOException e) {
                e.printStackTrace(); //todo логировать
            }
        }
    }

    @CommandLine.Command(name = "break", aliases = {"-break", "br", "-br"})
    static
    class Break implements Runnable {
        private final SimpleServerController serverController = SimpleServerController.getInstance();
        private final SimpleClientController clientController = SimpleClientController.getInstance();

        @Override
        public void run() {
        }

        @CommandLine.Command(name = "server", aliases = {"-server", "-s"})
        void cacheServer(@CommandLine.Option(names = "-p", required = true) int port) {
            ConnectionUtils.isValidPort(port);

            try {
                serverController.stopCaching(port);
                System.out.printf("Caching for server %d stopped\n", port);
            } catch (NoSuchObjectException e) {
                System.err.println(e.getMessage());
            }
        }

        @CommandLine.Command(name = "client", aliases = {"-client", "-c"})
        void cacheClient(@CommandLine.Option(names = "-h", required = true) String serverHost,
                         @CommandLine.Option(names = "-p", required = true) int port,
                         @CommandLine.Option(names = "-id") int id) throws IOException {
            ConnectionUtils.isValidPort(port);
            ConnectionUtils.isValidHost(serverHost);
            ConnectionUtils.isReachedHost(serverHost);
            try {
                    clientController.stopCaching(serverHost, port, id);
                System.out.printf("Caching for client %s: %d stopped\n", serverHost, port);
            } catch (NoSuchObjectException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
