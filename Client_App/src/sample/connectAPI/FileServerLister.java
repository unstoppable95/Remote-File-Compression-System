package sample.connectAPI;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class FileServerLister extends Connector {

    private String[] list;

    FileServerLister(String ipAddress, int portNumber) {
        super(ipAddress, portNumber);
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

            byte[] bytes = {(byte) 'w'};
            dataOutputStream.write(bytes);
            dataOutputStream.flush();
            System.out.println("Send w request");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            InputStream inputStream = socket.getInputStream();

            byte[] buf = new byte[1];
            while (true) {
                int readBytes = inputStream.read(buf);
                if (readBytes < 0) {
                    break;
                }
                byteArrayOutputStream.write(buf, 0, readBytes);
            }

            byte[] dataArray = byteArrayOutputStream.toByteArray();

            list = decodeStream(dataArray);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] decodeStream(byte dataArray[]) {
        String downloadData = new String(dataArray);
        return downloadData.split("\n");
    }

    public String[] getList() {
        return list;
    }
}
