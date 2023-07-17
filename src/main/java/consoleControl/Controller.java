package consoleControl;

import entity.Cached;

import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Controller {
    private static CommandCollection command;
    private static final Map<String, Object> mapExpression = new HashMap<>();

    public static Controller getControlInstance(String expression) {
        fillMap();
        setMapExpression(expression);
        if (mapExpression.get("targObj").equals("-s"))
            return SimpleServerController.getInstance();
        else if (mapExpression.get("targObj").equals("-c"))
            return SimpleClientController.getInstance();
        else throw new IllegalArgumentException("Illegal key expression");
    }

    private static void setMapExpression(String expression) {
        String[] tokens = expression.split(" +");
        command = CommandCollection.valueOf(tokens[0].substring(1).toUpperCase());
        mapExpression.replace("command", command);
        if (command == CommandCollection.EXIT)
            System.exit(0);
        mapExpression.replace("targObj", tokens[1]);
        if (tokens[2].matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"))
            mapExpression.replace("host", tokens[2]);
        else if (tokens[2].matches("^((6553[0-5])|(655[0-2][0-9])|(65[0-4][0-9]{2})|(6[0-4][0-9]{3})|([1-5][0-9]{4})|([0-5]{0,5})|([0-9]{1,4}))$"))
            mapExpression.replace("port", Integer.parseInt(tokens[2]));
        else mapExpression.replace("option", tokens[2]);
        if (tokens.length > 3) {
            if (tokens[3].matches("^((6553[0-5])|(655[0-2][0-9])|(65[0-4][0-9]{2})|(6[0-4][0-9]{3})|([1-5][0-9]{4})|([0-5]{0,5})|([0-9]{1,4}))$") &&
                    mapExpression.get("port") == null)
                mapExpression.replace("port", Integer.parseInt(tokens[3]));
            else if (tokens[3].matches("^((6553[0-5])|(655[0-2][0-9])|(65[0-4][0-9]{2})|(6[0-4][0-9]{3})|([1-5][0-9]{4})|([0-5]{0,5})|([0-9]{1,4}))$") &&
                    mapExpression.get("port") != null)
                mapExpression.replace("option", tokens[3]);
            else mapExpression.replace("option", tokens[3]);
        }
        if (tokens.length > 4)
            mapExpression.replace("option", tokens[4]);
    }

    private static void fillMap() {
        mapExpression.put("command", null);
        mapExpression.put("targObj", null);
        mapExpression.put("host", null);
        mapExpression.put("port", null);
        mapExpression.put("option", null);
    }

    public abstract Cached create() throws IOException;

    public abstract List<Cached> stop() throws NoSuchObjectException;

    public abstract List<?> get() throws NoSuchObjectException;

    public abstract List<?> remove() throws NoSuchObjectException;

    public abstract void read();

    public CommandCollection getCommand() {
        return command;
    }

    public Map<String, Object> getMapExpression() {
        return mapExpression;
    }

    public abstract void setEntity(LinkedBlockingQueue<?> entity);
}
