package servers;

import check.LocalValidator;
import clients.AbstractClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;

public class LocalServer extends AbstractReceiveSrv {
    private final SIBMonitorSrv sibMonitorSrv = new SIBMonitorSrv(0);
    private final WITSServer witsServer = new WITSServer(0);
    private static final Logger log = LogManager.getLogger(LocalServer.class.getSimpleName());

    public LocalServer(int port) {
        super(port, 512, new LocalValidator());
    }

    public LocalServer(int port, int maxNumberOfClient) {
        super(port, maxNumberOfClient, 512, new LocalValidator());
    }

    @Override
    protected boolean validate(byte[] data) {
        boolean sibValidate = sibMonitorSrv.validate(data);
        boolean witsValidate = witsServer.validate(data);
        boolean transferValidate;
        transferValidate = Arrays.equals(Arrays.copyOfRange(data, 0, 4), new byte[]{-84, -19, 0, 5});
        if (!(sibValidate ^ witsValidate ^ transferValidate)) {
            log.info("Unknown client connection attempt...");
            return false;
        }
        return true;
    }

    @Override
    protected AbstractClient getClient(byte[] data) {
        AbstractClient client = null;
        if (sibMonitorSrv.getValidator().authenticate(data)) {
            return sibMonitorSrv.getClient(data);
        }
        if (witsServer.getValidator().authenticate(data)) {
            return witsServer.getClient(data);
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            client = (AbstractClient) ois.readObject();
            log.debug("Initialize data from client is correct");
        } catch (IOException | ClassNotFoundException e) {
            log.error("Unknown client", e);
        }
        return client;
    }
}
