package sample.connectAPI;

public abstract class Connector implements Runnable {
    private String ipAddress;
    private int portNumber;

    public static final int HEADER_SIZE = 110;

    Connector(String ipAddress, int portNumber) {
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
    }

    String getIpAddress() {
        return ipAddress;
    }

    int getPortNumber() {
        return portNumber;
    }
}
