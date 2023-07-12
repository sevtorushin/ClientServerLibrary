package consoleControl;

import clients.SimpleClient;
import servers.SimpleServer;

import java.nio.channels.SocketChannel;
import java.rmi.NoSuchObjectException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Operation implements Runnable {
    static ExecutorService service = Executors.newCachedThreadPool();
    private List<SimpleServer> servers = new LinkedList<>();
    private List<SimpleClient> clients = new LinkedList<>();
    private Scanner scanner = new Scanner(System.in);
    private String expression;
    private CommandCollection command;

    @Override
    public void run() {
        Control control;
        Object ob;
        while (true) {
            try {
                expression = scanner.nextLine();
                control = Control.getControlInstance(expression);
                if (control instanceof SimpleServerControl)
                    control.setEntity(servers);
                else if (control instanceof SimpleClientControl)
                    control.setEntity(clients);
                else throw new UnknownFormatConversionException("Unknown object format for cast to Control");
                command = control.getCommand();
                switch (command) {
                    case START:
                        ob = control.start();
                        if (ob instanceof SimpleServer)
                            service.submit((SimpleServer) ob);
                        break;

                    case STOP:
                        try {
                            control.stop();
                        } catch (NoSuchObjectException e) {
                            System.err.println(e.getMessage());
                        }
                        break;

                    case GET:
                        try {
                            ob = control.get();
                        } catch (NoSuchObjectException e) {
                            System.err.println(e.getMessage());
                            continue;
                        }
                        if (ob instanceof Collection) {
                            if (((Collection<?>) ob).isEmpty())
                                System.out.println("none");
                            else ((Collection<?>) ob).forEach(System.out::println);
                        } else System.out.println(ob);
                        break;
//
                    case REMOVE:
                        try {
                            control.remove();
                        } catch (NoSuchObjectException e) {
                            System.err.println(e.getMessage());
                        }
                        break;
//
                    case READ:
                        Runnable task = (Runnable) control.read();
                        service.submit(task);
                        break;

                    case EXIT:
                        return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<SimpleServer> getServers() {
        return servers;
    }

    public List<SimpleClient> getClients() {
        return clients;
    }
}
