package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import sample.connectAPI.ConnectManager;

import java.io.File;


public class Controller {
    public Button buttonSend;
    public Button buttonList;
    public Button buttonDownload;
    public Button buttonChooseFile;
    public ListView<String> listFiles;
    public Button buttonChooseDirectory;

    private File file;
    private ConnectManager connectManager = new ConnectManager();
    private String[] listForListView;

    public void clickChooseFile(ActionEvent actionEvent) {
        actionEvent.getSource();
        System.out.println(actionEvent);
        file = Main.openFileChooser();
        if (file != null) {
            buttonChooseDirectory.setDisable(true);
            buttonChooseFile.setText(file.getAbsolutePath());
        }
    }

    public void clickSend(ActionEvent actionEvent) {
        actionEvent.getSource();
        System.out.println(actionEvent);
        if (file != null) {
            connectManager.sendFileToCompress(file);
            System.out.println("order file");
        }
        file = null;
        buttonChooseFile.setText("Choose File");
        buttonChooseDirectory.setText("Choose Directory");
        buttonChooseFile.setDisable(false);
        buttonChooseDirectory.setDisable(false);
    }

    public void clickList(ActionEvent actionEvent) {
        actionEvent.getSource();
        System.out.println(actionEvent);

        listForListView = connectManager.getListFileToDownload();

        String[] listToDisplay = new String[listForListView.length];
        for (int i = 0; i < listForListView.length; i++) {
            String[] words = listForListView[i].split("_");
            listToDisplay[i] = words[0] + ".zip   NEED MEMORY= " + makeBytesSizeString(words[words.length - 2]);
        }

        prepareDataToListView(listToDisplay);
    }

    public void clickDownload(ActionEvent actionEvent) {
        actionEvent.getSource();
        System.out.println(actionEvent);
        if (listFiles.getSelectionModel().getSelectedItem() != null) {
            String lines = listForListView[listFiles.getSelectionModel().getSelectedIndex()];
            connectManager.requestFileToDecompress(lines);
        } else {
            System.out.println("Not choose file from list.");
        }
    }

    private void prepareDataToListView(String lines[]) {
        ObservableList<String> listTest = FXCollections.observableArrayList(lines);
        setListText(listTest);
    }

    private void setListText(ObservableList<String> list) {
        listFiles.setItems(list);
    }

    public void clickChooseDirectory(ActionEvent actionEvent) {
        actionEvent.getSource();
        System.out.println(actionEvent);
        file = Main.openDirectoryChooser();
        if (file != null) {
            buttonChooseFile.setDisable(true);
            buttonChooseDirectory.setText(file.getAbsolutePath());
            System.out.println("order directory");
        }
    }

    private String makeBytesSizeString(String sizeWord) {
        String[] prefixMultiplicator = {"KB", "MB", "GB"};
        int size = Integer.parseInt(sizeWord);
        if (size < 1000) {
            return size + "";
        } else if (size < 1000000) {
            int integ = (int) (size / 1000);
            int rest = size % 1000;
            return integ + "," + rest + " " + prefixMultiplicator[0];
        } else if (size < 1000000000) {
            int integ = (int) (size / 1000000);
            int rest = size % 1000000;
            rest = (int) (rest / 1000);
            return integ + "," + rest + " " + prefixMultiplicator[1];
        } else {
            int integ = (int) (size / 1000000000);
            int rest = size % 1000000000;
            rest = (int) (rest / 1000);
            return integ + "," + rest + " " + prefixMultiplicator[2];
        }
    }

}
