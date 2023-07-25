package consoleControl;

import controllers.SimpleClientController;
import controllers.SimpleServerController;
import picocli.CommandLine;
import utils.ConnectionUtils;

import java.io.IOException;

@CommandLine.Command(name = "print", aliases = {"-print"}, subcommands = {Print.Begin.class, Print.Break.class})
public class Print implements Runnable {
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
        Runnable printServer(@CommandLine.Option(names = "-p", required = true) int port) {
            ConnectionUtils.isValidPort(port);

            return () -> {
                try {
                    serverController.printRawReceiveData(port);
                    System.out.printf("Printing for server %d started\n", port);
                } catch (IOException e) {
                    e.printStackTrace(); //todo логировать
                }
            };
        }

        @CommandLine.Command(name = "client", aliases = {"-client", "-c"})
        Runnable printClient(@CommandLine.Option(names = "-h", required = true) String serverHost,
                             @CommandLine.Option(names = "-p", required = true) int port) throws IOException {
            ConnectionUtils.isValidPort(port);
            ConnectionUtils.isValidHost(serverHost);
            ConnectionUtils.isReachedHost(serverHost);
//            ConnectionUtils.isRunServer(serverHost, port);
            return  () -> {
                try {
                    clientController.printRawReceiveData(port);
                    System.out.printf("Printing for client %s: %d started\n",serverHost, port);
                } catch (IOException e) {
                    e.printStackTrace(); //todo логировать
                }
            };
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
        Runnable printServer(@CommandLine.Option(names = "-p", required = true) int port) {
            ConnectionUtils.isValidPort(port);

            return () -> {
                try {
                    serverController.stopPrinting(port);
                    System.out.printf("Printing for server %d stopped\n", port);
                } catch (IOException e) {
                    e.printStackTrace(); //todo логировать
                }
            };
        }

        @CommandLine.Command(name = "client", aliases = {"-client", "-c"})
        Runnable printClient(@CommandLine.Option(names = "-h", required = true) String serverHost,
                             @CommandLine.Option(names = "-p", required = true) int port) throws IOException {
            ConnectionUtils.isValidPort(port);
            ConnectionUtils.isValidHost(serverHost);
            ConnectionUtils.isReachedHost(serverHost);
            ConnectionUtils.isRunServer(serverHost, port);
            return  () -> {
                try {
                    clientController.stopPrinting(port);
                    System.out.printf("Printing for client %s: %d stopped\n",serverHost, port);
                } catch (IOException e) {
                    e.printStackTrace(); //todo логировать
                }
            };
        }
    }
}
