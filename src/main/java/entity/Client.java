package entity;

import java.util.Objects;

public class Client {
    private String id;
    private String sessionKey;
    private String host;
    byte[] data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(id, client.id) &&
                Objects.equals(sessionKey, client.sessionKey) &&
                Objects.equals(host, client.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sessionKey, host);
    }

    @Override
    public String toString() {
        return "Client{" +
                "id='" + id + '\'' +
                ", sessionKey='" + sessionKey + '\'' +
                ", host='" + host + '\'' +
                '}';
    }
}
