package consoleControl;

import clients.simple.SimpleClientController;
import servers.simple.SimpleServerController;
import picocli.CommandLine;
import utils.ConnectionUtils;

import java.io.IOException;
import java.rmi.NoSuchObjectException;

@CommandLine.Command(name = "task", aliases = {"-task"})
public class Task implements Runnable {

    private final SimpleServerController serverController = SimpleServerController.getInstance();
    private final SimpleClientController clientController = SimpleClientController.getInstance();

    @Override
    public void run() {
    }

    @CommandLine.Command(name = "server", aliases = {"-server", "-s"})
    void taskServer(@CommandLine.Option(names = "-p", required = true) int port,
                    @CommandLine.Option(names = "-get") String name,
                    @CommandLine.Option(names = "-all") boolean all) throws NoSuchObjectException {
        ConnectionUtils.isValidPort(port);

        HandlersCommand command;
        if (all && name == null)
            serverController.getRunnableTasks(port).forEach(System.out::println);
        else if (!all && name != null) {
            command = serverController.getRunnableTasks(port).stream()
                    .filter(cmd -> cmd.name().equals(name.toUpperCase())).findFirst().orElse(null);
            if (command==null)
                System.out.println("none");
            else System.out.println(command);
        }
    }

    @CommandLine.Command(name = "client", aliases = {"-client", "-c"})
    void taskClient(@CommandLine.Option(names = "-h", required = true) String serverHost,
                    @CommandLine.Option(names = "-p", required = true) int port,
                    @CommandLine.Option(names = "-get") String name,
                    @CommandLine.Option(names = "-all") boolean all) throws IOException {
        ConnectionUtils.isValidPort(port);
        ConnectionUtils.isValidHost(serverHost);

        HandlersCommand command;
        if (all && name == null)
            clientController.getRunnableTasks(port).forEach(System.out::println);
        else if (!all && name != null) {
            command = clientController.getRunnableTasks(port).stream()
                    .filter(cmd -> cmd.name().equals(name.toUpperCase())).findFirst().orElse(null);
            if (command==null)
                System.out.println("none");
            else System.out.println(command);
        }
    }
}
