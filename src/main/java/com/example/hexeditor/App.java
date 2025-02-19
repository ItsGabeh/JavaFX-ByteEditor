package com.example.hexeditor;

import com.example.hexeditor.components.ByteEditor;
import com.example.hexeditor.components.Utils;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Arrays;


public class App extends Application {
    private final ByteEditor byteEditor = new ByteEditor(); // This byteEditor is used to see original file
    private final ByteEditor encryptedByteEditor = new ByteEditor(); // This byteEditor is used to see encrypted file
    private final ByteEditor decryptedByteEditor = new ByteEditor();
    private final ByteEditor hashByteEditor = new ByteEditor();
    private byte[] encryptedBytes;// stores the encrypted bytes for general use
    TextArea logArea = new TextArea();
    private File file;

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(createContent(), 800, 850);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Hex Editor");
        stage.show();
    }

    private Region createContent() {
        byteEditor.setPadding(new Insets(10));
        byteEditor.setMaxHeight(250);
        VBox content = new VBox(createFilePicker(), byteEditor, createOperationsRegion(), createLogRegion());
        content.setAlignment(Pos.CENTER);
        content.setSpacing(10);

        return content;
    }

    private Region createFilePicker() {
        // create a text field
        Button openButton = new Button("Open");
        TextField fileName = new TextField();

        // configure text field
        fileName.setPromptText("Select a file");
        fileName.setPrefWidth(300);
        fileName.setMinWidth(300);
        fileName.setMaxWidth(300);
        fileName.setDisable(true);
        fileName.setEditable(false);

        // extension filter for file chooser
        // FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text Files", "*.txt");

        // configure the button
        openButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open File");
//            fileChooser.getExtensionFilters().add(extFilter);
            File f = fileChooser.showOpenDialog(null);
            if (f != null) {
                file = f;
                try {
                    byteEditor.loadByteArray(readFile());
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Error loading file", ButtonType.OK);
                    alert.showAndWait();
                }
                fileName.setText(file.getAbsolutePath());
            }
        });

        HBox hBox = new HBox(new Label("File"),fileName, openButton);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10));
        return hBox;
    }

    private Region createOperationsRegion() {
        TabPane tabPane = new TabPane();
        Tab encryptionTab = new Tab("Encryption");
        Tab decryptionTab = new Tab("Decryption");
        Tab hashTab = new Tab("Hash");

        tabPane.setMaxHeight(270);
        encryptionTab.setClosable(false);
        decryptionTab.setClosable(false);
        hashTab.setClosable(false);

        encryptionTab.setContent(createEncryptionRegion());
        decryptionTab.setContent(createDecryptionRegion());
        hashTab.setContent(createHashRegion());
        tabPane.getTabs().addAll(encryptionTab, decryptionTab, hashTab);

        VBox vBox = new VBox(tabPane);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10));
        return vBox;
    }

    private Region createLogRegion() {
        logArea.setEditable(false);
        logArea.setWrapText(true);

        Button cleanButton = new Button("Clean");
        VBox vBox = new VBox(logArea, cleanButton);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10));
        vBox.setMaxHeight(200);
        vBox.setMinHeight(200);
        return vBox;
    }

    private Region createEncryptionRegion() {
        encryptedByteEditor.setPadding(new Insets(10));
        encryptedByteEditor.setMaxHeight(250);

        // bottom HBox
        TextField password = new TextField("0000");
        Button encryptButton = new Button("Encrypt");
        Button saveButton = new Button("Save");
        HBox encryptionHbox = new HBox(
                new Label("Password"),
                password,
                encryptButton,
                saveButton
        );
        encryptionHbox.setAlignment(Pos.CENTER);
        encryptionHbox.setSpacing(10);

        encryptButton.setOnAction(e -> {
            String encryptedPassword = password.getText();
            byte[] read = null;
            // Read the file
            try {
                read = readFile();
//                System.out.println(Arrays.toString(read));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            encryptedBytes = Utils.encrypt(read, encryptedPassword);// change this to read the file
//            System.out.println(encrypted);
//            encryptedBytes = encrypted.getBytes();
//            System.out.println(Arrays.toString(encryptedBytes));
//            encryptedByteEditor.loadByteArray(encryptedBytes);
//            encryptedString = encrypted;
//            System.out.println(encryptedString);
//            encryptedByteEditor.loadByteArray(encryptedString.getBytes());
//            for (byte b : encryptedBytes) {
//                System.out.print(b);
//                System.out.print(" ");
//            }
            // System.out.println(encryptedString);
            encryptedByteEditor.loadByteArray(encryptedBytes);
            // Utils.printLogTo(logArea);
            Utils.clearLog();
        });

        saveButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File");
            // fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            // fileChooser.setInitialFileName("encrypted" + LocalDate.now().toString() + ".txt");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Documents"));
            File f = fileChooser.showSaveDialog(null);
            if (f != null) {
                try (FileOutputStream fos = new FileOutputStream(f)){
                    fos.write(encryptedBytes);
                    System.out.println("Saved file: " + f.getAbsolutePath());
                }  catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // main region
        VBox encryptionRegion = new VBox(encryptedByteEditor, encryptionHbox);
        encryptionRegion.setAlignment(Pos.CENTER);
        encryptionRegion.setSpacing(10);

        return encryptionRegion;
    }

    private Region createDecryptionRegion() {
        decryptedByteEditor.setPadding(new Insets(10));
        decryptedByteEditor.setMaxHeight(250);
        TextField password = new TextField("0000");
        Button decrypButton = new Button("Decrypt");
        Button saveButton = new Button("Save");
        HBox decryptionHbox = new HBox(
                new Label("Password"),
                password,
                decrypButton,
                saveButton
        );

        decryptionHbox.setAlignment(Pos.CENTER);
        decryptionHbox.setSpacing(10);

        decrypButton.setOnAction(e -> {
            String decryptedPassword = password.getText();
//            byte[] result = Utils.decrypt(encryptedBytes, decryptedPassword);
//            for (byte b : result) {
//                System.out.print(b);
//                System.out.print(" ");
//            }
            decryptedByteEditor.loadByteArray(Utils.decrypt(encryptedBytes, decryptedPassword));
            // Utils.printLogTo(logArea);
            // Utils.clearLog();
//            System.out.println(encryptedString.getBytes());
//            System.out.println(Utils.decrypt(encryptedString.getBytes(), decryptedPassword));
        });

        VBox decryptionRegion = new VBox(decryptedByteEditor, decryptionHbox);
        decryptionRegion.setAlignment(Pos.CENTER);
        decryptionRegion.setSpacing(10);

        return decryptionRegion;
    }

    private Region createHashRegion() {
        hashByteEditor.setPadding(new Insets(10));
        hashByteEditor.setMaxHeight(250);

        TextField hashText = new TextField("");
        hashText.setEditable(false);
        Button hashButton = new Button("Hash");
        hashButton.setOnAction(e -> {
            byte[] read = null;
            // Read the file
            try {
                read = readFile();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            String hash = Utils.hash(read);
            hashByteEditor.loadByteArray(hash.getBytes());
            hashText.setText(hash);
        });
        HBox hashHbox = new HBox(hashText, hashButton);
        hashHbox.setAlignment(Pos.CENTER);
        hashHbox.setSpacing(10);

        VBox hashRegion = new VBox(hashByteEditor, hashHbox);
        hashRegion.setAlignment(Pos.CENTER);
        hashRegion.setSpacing(10);
        return hashRegion;
    }

    private Region createTestInputRegion() {
        // Create a Label
        Label label = new Label("This is a test input area");
        // Create a text field
        TextField textField = new TextField();
        // Create a button to submit the text
        Button button = new Button("Test");

        button.setOnAction(e -> {
            String text = textField.getText();
            byteEditor.loadByteArray(text.getBytes());
        });

        HBox hbox = new HBox(label, textField, button);
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(10);
        return hbox;
    }

    // Fill the data list with hexadecimal data
    private void byteToHexObservableList(byte[] bytes, ObservableList<ObservableList<StringProperty>> data) {
        data.clear();
        for (int i = 0; i < bytes.length; i += 10) {
            ObservableList<StringProperty> row = FXCollections.observableArrayList();
            for (int j = 0; j < 10 && i + j < bytes.length; j++) {
                byte b = bytes[i + j];
                row.add(new SimpleStringProperty(String.format("%02X", b)));
            }
            data.add(row);
        }
    }

    public byte[] readFile() throws IOException {
        if (file != null) {
            return Files.readAllBytes(file.toPath());
        }

        return null;
    }

    public static void main(String[] args) {
        launch();
    }
}