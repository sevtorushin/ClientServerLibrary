package consoleControl;

import picocli.CommandLine;

@CommandLine.Command(name = "highLevelCommand",
        subcommands = {
                Start.class,
                Stop.class,
                Get.class,
                Remove.class,
                Cache.class,
                Print.class,
                Task.class,
                Transfer.class,
                Exit.class
        })
public class TopLevelCommand implements Runnable {

    @Override
    public void run(){
    }
}
