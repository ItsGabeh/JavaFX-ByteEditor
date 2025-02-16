package com.example.hexeditor.components;

import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FilePicker extends HBox {
    private File current = null;
    private final ByteEditor byteEditor;

    public FilePicker(ByteEditor byteEditor) {
        this.byteEditor = byteEditor;

        // configure the HBox
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(10));

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
            File file = fileChooser.showOpenDialog(this.getScene().getWindow());
            if (file != null) {
                current = file;
                System.out.println("Current file: " + current.getAbsolutePath());
                fileName.setText(current.getAbsolutePath());
            }
        });

        this.getChildren().addAll(new Label("File"),fileName, openButton);
    }

    // If a copy of the file is necessary
    public File getFile() {
        return new File(current.getAbsolutePath());
    }

    // Read the file content and returns a bytes with that content
    public byte[] readFile() throws IOException {
        if (current != null) {
            return Files.readAllBytes(current.toPath());
        }

        return null;
    }
}
