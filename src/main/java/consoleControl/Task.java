package consoleControl;

import clients.simple.SimpleClientController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import servers.simple.SimpleServerController;
import picocli.CommandLine;
import utils.ConnectionUtils;

import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.util.List;

@CommandLine.Command(name = "task", aliases = {"-task"})
public class Task implements Runnable {

    private final SimpleServerController serverController = SimpleServerController.getInstance();
    private final SimpleClientController clientController = SimpleClientController.getInstance();

    private static final Logger log = LogManager.getLogger(Task.class.getSimpleName());

    @Override
    public void run() {
    }

    @CommandLine.Command(name = "server", aliases = {"-server", "-s"})
    void taskServer(@CommandLine.Option(names = "-p", required = true) int port,
                    @CommandLine.Option(names = "-get") String name,
                    @CommandLine.Option(names = "-all") boolean all) throws NoSuchObjectException {
        ConnectionUtils.isValidPort(port);

        List<String> tasks = serverController.getRunnableTasks(port);
        printTask(name, all, tasks);
        log.debug("Result printed in console");
    }

    @CommandLine.Command(name = "client", aliases = {"-client", "-c"})
    void taskClient(@CommandLine.Option(names = "-h", required = true) String serverHost,
                    @CommandLine.Option(names = "-p", required = true) int port,
                    @CommandLine.Option(names = "-id") int id,
                    @CommandLine.Option(names = "-get") String name,
                    @CommandLine.Option(names = "-all") boolean all) throws IOException {
        ConnectionUtils.isValidPort(port);
        ConnectionUtils.isValidHost(serverHost);

        List<String> tasks;
            tasks = clientController.getRunnableTasks(serverHost, port, id);
        printTask(name, all, tasks);
        log.debug("Result printed in console");
    }

    private void printTask(String name, boolean all, List<String> tasks) {
        String command;
        if (all && name == null) {
            if (tasks.isEmpty())
                System.out.println("none");
            else
                tasks.forEach(System.out::println);
        } else if (!all && name != null) {
            command = tasks.stream()
                    .filter(cmd -> cmd.toLowerCase().equals(name.toLowerCase()))
                    .findFirst()
                    .orElse("none");
            System.out.println(command);
        }
    }
}
