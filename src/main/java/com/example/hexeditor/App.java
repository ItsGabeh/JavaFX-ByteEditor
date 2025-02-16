package com.example.hexeditor;

import com.example.hexeditor.components.ByteEditor;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


public class App extends Application {
    // Data is used by the tableView
    private final ObservableList<ObservableList<StringProperty>> data = FXCollections.observableArrayList();
    private final ByteEditor byteEditor = new ByteEditor();
    private File file;

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(createContent(), 1080, 600);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Hex Editor");
        stage.show();
    }

    private Region createContent() {
        byteEditor.setPadding(new Insets(10));
        byteEditor.setMaxHeight(250);
        VBox content = new VBox(createFilePicker(), byteEditor);
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
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text Files", "*.txt");

        // configure the button
        openButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open File");
            fileChooser.getExtensionFilters().add(extFilter);
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

        HBox hBox = new HBox(new Label("File"),openButton, fileName);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10));
        return hBox;
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
    private void byteToHexObservableList(byte[] bytes) {
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