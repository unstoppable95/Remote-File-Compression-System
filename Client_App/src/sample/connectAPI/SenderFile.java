package sample.connectAPI;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

public class SenderFile extends Connector {

    private byte[] header;
    private byte[] fileData;

    SenderFile(byte[] header, byte[] fileData, String ipAddress, int portNumber) {
        super(ipAddress, portNumber);
        this.header = header;
        this.fileData = fileData;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(getIpAddress(), getPortNumber())) {

            if (socket.isConnected()) {
                System.out.println("Socket is connect");
            } else {
                return;
            }

            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            if (fileData == null) {
                System.out.println("I cant get bytes from files");
                return;
            }
            //SEND sign, header and file to server
            dataOutputStream.write('c');
            dataOutputStream.flush();
            System.out.println("send c");
            dataOutputStream.write(header);
            dataOutputStream.flush();
            System.out.println("send header");
            dataOutputStream.write(fileData);
            dataOutputStream.flush();
            System.out.println("send file");
            dataOutputStream.close();

        } catch (ConnectException e) {
            System.out.println("Timeout SenderFile");
        } catch (IOException e) {
            System.out.println("Error in SenderFile. Is not connect.");
        }
    }
}
