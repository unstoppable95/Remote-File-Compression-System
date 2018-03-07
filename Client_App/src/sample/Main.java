package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class Main extends Application {

    private static final FileChooser fileChooser = new FileChooser();
    private static final DirectoryChooser directoryChooser = new DirectoryChooser();
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        String myAddresIP = "";
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while (e.hasMoreElements()) {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements()) {
                InetAddress i = (InetAddress) ee.nextElement();
                if (i.getHostAddress().substring(0, 3).equals("192")) {
                    myAddresIP = i.getHostAddress();
                }
            }
        }
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Client " + myAddresIP);
        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.show();
    }


    public static File openDirectoryChooser() {
        directoryChooser.setTitle("Choose directory:");
        File file = directoryChooser.showDialog(stage);
        System.out.println(file);
        return file;
    }

    public static File openFileChooser() {
        fileChooser.setTitle("Choose file:");

        File file = fileChooser.showOpenDialog(stage);
        System.out.println(file);
        return file;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
