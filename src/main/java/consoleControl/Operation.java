package consoleControl;

import clients.SimpleClient;
import entity.Cached;
import servers.SimpleServer;

import java.rmi.NoSuchObjectException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Operation implements Runnable {
    static ExecutorService service = Executors.newCachedThreadPool();
    private final LinkedBlockingQueue<SimpleServer> serverPool;
    private final LinkedBlockingQueue<SimpleClient> clientPool;
    private final Scanner scanner;

    public Operation() {
        SimpleServerController serverController = SimpleServerController.getInstance();
        SimpleClientController clientController = SimpleClientController.getInstance();
        serverPool = serverController.getServers();
        clientPool = clientController.getClients();
        scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        Controller controller;
        String expression;
        CommandCollection command;
        while (true) {
            try {
                expression = scanner.nextLine();
                controller = /*Controller.getControlInstance(expression)*/ null;
                command = controller.getCommand();
                switch (command) {
                    case START:
                        Cached entity = controller.create();
                        service.submit((Runnable) entity);
                        break;

                    case STOP:
                        try {
                            controller.stop();
                        } catch (NoSuchObjectException e) {
                            System.err.println(e.getMessage());
                        }
                        break;

                    case GET:
                        List<?> result = controller.get();
                        if (result.isEmpty())
                            System.out.println("none");
                        else result.forEach(System.out::println);
                        break;
//
                    case REMOVE:
                        try {
                            controller.remove();
                        } catch (NoSuchObjectException e) {
                            System.err.println(e.getMessage());
                        }
                        break;
//
                    case READ:
                        service.submit(controller::read);
                        break;

                    case EXIT:
                        return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public LinkedBlockingQueue<SimpleServer> getServers() {
        return serverPool;
    }

    public LinkedBlockingQueue<SimpleClient> getClients() {
        return clientPool;
    }
}
