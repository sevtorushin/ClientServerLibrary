package servers;

import check.WITSValidator;
import clients.AbstractClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WITSServer extends AbstractReceiveSrv {
    private static final Logger log = LogManager.getLogger(WITSServer.class.getSimpleName());

    public WITSServer(int port) {
        super(port, 512, new WITSValidator());
    }

    public WITSServer(int port, int maxNumberOfClient) {
        super(port, maxNumberOfClient, 512, new WITSValidator());
    }

    @Override
    protected boolean validate(byte[] data) {
        return getValidator().authenticate(data);
    }

    @Override
    protected AbstractClient getClient(byte[] data) {
        String clientId = "Wits";
        AbstractClient client = new WITSServer.WitsClient(null, 0, clientId);
        client.setSessionKey("constant");
        return client;
    }

    private static class WitsClient extends AbstractClient {

        public WitsClient(String host, int port, String id) {
            super(host, port, id, null);
        }
    }
}
