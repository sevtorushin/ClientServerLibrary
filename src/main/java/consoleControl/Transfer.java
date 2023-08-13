package consoleControl;

import clients.simple.SimpleClientController;
import servers.simple.SimpleServerController;
import picocli.CommandLine;
import utils.ConnectionUtils;

import java.io.IOException;
import java.rmi.NoSuchObjectException;

@CommandLine.Command(name = "transfer", aliases = {"-transfer", "tr", "-tr"}, subcommands = {Transfer.Begin.class, Transfer.Break.class})
public class Transfer implements Runnable {
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
                System.out.printf("Transfer from server %d to client %d started\n", serverPort, clientPort);
            } catch (IOException e) {
                e.printStackTrace(); //todo логировать
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
                System.out.printf("Transfer from client %s: %d to server %s: %d started\n",
                        serverHost, clientPort, anotherServerHost, anotherServerPort);
            } catch (IOException e) {
                e.printStackTrace(); //todo логировать
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
                System.out.printf("Transfer from server %d stopped\n", serverPort);
            } catch (NoSuchObjectException e) {
                System.err.println(e.getMessage());
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
                System.out.printf("Transfer from client %s: %d to server %d stopped\n",
                        serverHost, clientPort, anotherServerPort);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
