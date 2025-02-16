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
import javafx.stage.Stage;


public class App extends Application {
    // Data is used by the tableView
    private final ObservableList<ObservableList<StringProperty>> data = FXCollections.observableArrayList();
    private final ByteEditor byteEditor = new ByteEditor();

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(createContent(), 800, 600);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Hex Editor");
        stage.show();
    }

    private Region createContent() {
        VBox vBox = new VBox(byteEditor);
        vBox.setPadding(new Insets(10));
        vBox.setMaxHeight(250);
        VBox content = new VBox(createTestInputRegion(), vBox);
        content.setAlignment(Pos.CENTER);
        content.setSpacing(10);

        return content;
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

    public static void main(String[] args) {
        launch();
    }
}