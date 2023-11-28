import consoleControl.ConsoleCommandRunner;
import consoleControl.TopLevelCommand;

public class Main {

    public static void main(String[] args) {
        ConsoleCommandRunner runner = new ConsoleCommandRunner(new TopLevelCommand());
        runner.run();
    }
}

