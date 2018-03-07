package sample.connectAPI;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class ReceiverFile extends Connector {

    private String wantFile;

    ReceiverFile(String wantFile, String ipAddress, int portNumber) {
        super(ipAddress, portNumber);
        this.wantFile = wantFile;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(getIpAddress(), getPortNumber())) {
            byte[] header = wantFile.getBytes();

            //SEND name to server
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write('d');
            dataOutputStream.flush();
            dataOutputStream.write(header);
            //RECEIVE data from server

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            InputStream inputStream = socket.getInputStream();

            byte[] headerDownload = new byte[HEADER_SIZE];
            byte[] buf = new byte[1];
            int readed = 0;
            do {
                inputStream.read(buf, 0, 1);
                headerDownload[readed] = buf[0];
                readed++;
            } while (readed != HEADER_SIZE);

            int size = pullDownloadSize(new String(headerDownload));

            while (true) {
                int readBytes = inputStream.read(buf);
                if (readBytes < 0) {
                    break;
                }
                byteArrayOutputStream.write(buf);
            }
            byte[] downloadData = byteArrayOutputStream.toByteArray();
            byte[] dataArray = new byte[size];

            for (int i = 0; i < size; i++) {
                dataArray[i] = downloadData[i];
            }

            System.out.println(new String(headerDownload));

            int structureSize = pullStructureSize(new String(headerDownload));
            if (structureSize == 0) {
                makeFile(pullNameFromHeader(new String(headerDownload)), dataArray);
            } else {
                makeStructureFiles(structureSize, dataArray);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String pullNameFromHeader(String header) {
        String[] word = header.split("_");
        return word[0];
    }

    private int pullStructureSize(String header) {
        String[] word = header.split("_");
        return Integer.parseInt(word[word.length - 1]);
    }

    private int pullDownloadSize(String header) {
        String[] word = header.split("_");
        return Integer.parseInt(word[word.length - 2]) - HEADER_SIZE;
    }

    private void makeFile(String fileName, byte[] fileData) {
        try {
            File file = new File(fileName);
            Path path = Paths.get(file.getAbsolutePath());
            Files.write(path, fileData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeStructureFiles(int structureSize, byte[] data) {
        byte[] structureData = new byte[structureSize];
        System.arraycopy(data, 0, structureData, 0, structureSize);
        byte[] filesData = new byte[data.length - structureSize];
        System.arraycopy(data, 0 + structureSize, filesData, 0, filesData.length);

        String[] structure = new String(structureData).split("\n");

        int actualNesting = 0;
        int usedBytes = 0;

        LinkedList<String> folderList = new LinkedList<>();
        for (String row : structure) {
            int nesting = row.split("\t").length - 1;
            if (nesting < actualNesting) {
                folderList.removeLast();
                actualNesting--;
            }
            String pathPrefix = makePrefixPath(folderList);

            if ("d_".equals(row.replace("\t", "").substring(0, 2))) {//makeFolder
                String folderName = row.replace("\t", "").substring(2);
                new File(pathPrefix + folderName).mkdir();
                folderList.addLast(folderName);
                actualNesting++;
            } else {
                String[] preName = row.replace("\t", "").split(" ");
                String name = "";
                for (int i = 0; i < preName.length - 1; i++) {
                    name += preName[i];
                }
                int fileSize = Integer.parseInt(preName[preName.length - 1]);
                byte[] fileDataArray = new byte[fileSize];
                for (int i = 0; i < fileSize; i++) {
                    fileDataArray[i] = filesData[i + usedBytes];
                }
                usedBytes += fileSize;
                try {
                    File file = new File(pathPrefix + name);
                    Path path = Paths.get(file.getAbsolutePath());
                    Files.write(path, fileDataArray);
                } catch (IOException e) {
                    System.out.println("Error in save file. ReceiverFile.");
                }
            }
        }
    }

    private String makePrefixPath(LinkedList<String> folderList) {
        String path = "";
        for (String folder : folderList) {
            path = path + folder + "/";
        }
        return path;
    }
}
