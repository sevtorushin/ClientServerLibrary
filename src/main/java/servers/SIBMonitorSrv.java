package servers;

import check.SibValidator;
import clients.AbstractClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SIBMonitorSrv extends AbstractReceiveSrv {
    private static final Logger log = LogManager.getLogger(SIBMonitorSrv.class.getSimpleName());

    public SIBMonitorSrv(int port) {
        super(port, 22, new SibValidator());
    }

    public SIBMonitorSrv(int port, int maxNumberOfClient) {
        super(port, maxNumberOfClient, 22, new SibValidator());
    }

    @Override
    protected boolean validate(byte[] data) {
        boolean contains = clientPool.stream().anyMatch(client -> client.getHost().equals("127.0.0.1"));
        if (contains) {
            log.info("Starting a second Sib Monitor client was rejected");
            return false;
        }
        return getValidator().authenticate(data);
    }

    @Override
    protected AbstractClient getClient(byte[] data) {
        String clientId = "SibReceiver";
        AbstractClient client = new SibClient("", 0, clientId);
        client.setSessionKey("constant");
        return client;
    }

    private static class SibClient extends AbstractClient {

        public SibClient(String host, int port, String id) {
            super(host, port, id, null);
        }

        @Override
        protected void loadSessionKey() {

        }

        @Override
        protected boolean authorize() {
            return false;
        }
    }
}
