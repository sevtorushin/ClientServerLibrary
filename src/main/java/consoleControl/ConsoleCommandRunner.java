package consoleControl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConsoleCommandRunner implements Runnable{
    private final ExecutorService service = Executors.newCachedThreadPool();
    private final Scanner scanner = new Scanner(System.in);
    private final CommandLine cmd;

    private static final Logger log = LogManager.getLogger(ConsoleCommandRunner.class.getSimpleName());

    public ConsoleCommandRunner(Object topLevelCommand) {
        cmd = new CommandLine(topLevelCommand);
        cmd.setExecutionStrategy(new CommandLine.RunLast());
    }

    @Override
    public void run() {
        String expression;
        while (true) {
            expression = scanner.nextLine();
            String[] exp;
            if (expression.isBlank()) {
                exp = new String[0];
                log.debug("Empty command entered");
            } else {
                exp = expression.split(" ");
                log.debug("User enters this command: " + expression);
            }

            cmd.execute(exp);
            Integer level1Result = getLevel1Object(cmd);
            Object level2Result = getLevel2Object(cmd);

            if (level2Result != null) {
                if (level2Result instanceof Runnable) {
                    Future<?> f = service.submit((Runnable) level2Result);
                    log.debug("Task processing begin");
                    f.isDone();
                }
            }
            if (level1Result != null) {
                log.debug("Exit. Exit code = " + level1Result);
                System.exit(level1Result);
            }
        }
    }

    public void addCommand(Object[] commands) {
        for (Object command : commands) {
            cmd.addSubcommand(command);
        }
    }

    private Integer getLevel1Object(CommandLine cmd) {
        CommandLine.ParseResult parseResult = cmd.getParseResult();
        if (parseResult.subcommand() != null) {
            CommandLine sub = parseResult.subcommand().commandSpec().commandLine();
            return sub.getExecutionResult();
        } else return null;
    }

    private Object getLevel2Object(CommandLine cmd) {
        CommandLine.ParseResult parseResult = cmd.getParseResult();
        if (parseResult.subcommand() != null) {
            if (parseResult.subcommand().subcommand() != null) {
                CommandLine sub = parseResult.subcommand().subcommand().commandSpec().commandLine();
                return sub.getExecutionResult();
            } else return null;
        } else return null;
    }
}
