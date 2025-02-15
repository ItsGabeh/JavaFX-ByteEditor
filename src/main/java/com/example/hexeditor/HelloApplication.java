package com.example.hexeditor;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class HelloApplication extends Application {

    /* Define the variables used in this class */
    // Data is used by the tableView
    private final ObservableList<ObservableList<StringProperty>> data = FXCollections.observableArrayList();

    // Selected rows of the ascii and hex editors
    private SimpleIntegerProperty editorSelectedRow = new SimpleIntegerProperty(-1);
    private SimpleIntegerProperty editorSelectedColumn = new SimpleIntegerProperty(-1);

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(createContent(), 800, 600);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Hex Editor");
        stage.show();
    }

    private Region createContent() {
        VBox content = new VBox(createTestInputRegion(),createByteEditor());
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
            byteToHexObservableList(text.getBytes());
        });

        HBox hbox = new HBox(label, textField, button);
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(10);
        return hbox;
    }

    /* Create the byte editor */
    private Region createByteEditor() {
        // Create the table view of ASCII Characters
        TableView<ObservableList<StringProperty>> asciiTable = new TableView<>();
        // Create the table view of Hex Characters
        TableView<ObservableList<StringProperty>> hexTable = new TableView<>();

        // configure the table views
        setupTable(asciiTable, false);
        setupTable(hexTable, true);

        // Create the Label for edit options
        Label positionLabel = new Label("Position");
        // Create a spinner for select the edition position
        Spinner<Integer> positionSpinner = new Spinner<>();
        // Create a label to show the ascii value
        Label asciiLabel = new Label("Ascii");
        // Create a text field to modify the ascii value
        TextField asciiTextField = new TextField();
        // Create a label to show the hex value
        Label hexLabel = new Label("Hex");
        // Create a text field to modify the hex value
        TextField hexTextField = new TextField();
        // Create a button to submit the modification

        // configure the edit options
        positionSpinner.setEditable(false);
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0);
        positionSpinner.setValueFactory(valueFactory);
        positionSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            int row = newValue / 10;
            int col = (newValue % 10) + 1;

            if (row < data.size() && col < asciiTable.getColumns().size()) {
                asciiTable.getSelectionModel().clearAndSelect(row, asciiTable.getColumns().get(col));
                asciiTable.scrollTo(row);
                hexTable.getSelectionModel().clearAndSelect(row, hexTable.getColumns().get(col));
                hexTable.scrollTo(row);
            }

        });
        positionSpinner.setDisable(true);

        data.addListener((ListChangeListener<? super ObservableList<StringProperty>>) change -> {
            int totalBytes = data.stream().mapToInt(ObservableList::size).sum();
            int maxIndex = Math.max(0, totalBytes - 1);
            positionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, maxIndex, 0));
            positionSpinner.setDisable(totalBytes == 0);
        });

        asciiTable.getSelectionModel().getSelectedCells().addListener( (ListChangeListener<? super TablePosition>) change -> {
            if (!change.getList().isEmpty()) {
                TablePosition position = change.getList().getFirst();
                int row = position.getRow();
                int col = position.getColumn();
                if (col > 0 && row < data.size() && col -1 < data.get(row).size()) {
                    editorSelectedRow.set(row);
                    editorSelectedColumn.set(col - 1);
                    String selected = data.get(row).get(col - 1 ).getValue();
                    hexTextField.setText(selected);

                    try {
                        int intValue = Integer.parseInt(selected, 16);
                        char asciiChar = (char) intValue;
                        String asciiValue = (intValue >= 32 && intValue <= 126) ? String.valueOf(asciiChar) : "."; // Sustituir caracteres no imprimibles
                        asciiTextField.setText(asciiValue);
                    } catch (NumberFormatException e) {
                        asciiTextField.setText("");
                    }
                }
            }
        });

        // Put the edit options together
        HBox editHbox = new HBox(positionLabel, positionSpinner, asciiLabel, asciiTextField, hexLabel, hexTextField);
        editHbox.setAlignment(Pos.CENTER);
        editHbox.setSpacing(10);

        // Put the tables together
        HBox tablesHbox = new HBox(asciiTable, hexTable);
        tablesHbox.setSpacing(10);
        tablesHbox.setPadding(new Insets(10));
        tablesHbox.setAlignment(Pos.CENTER);

        VBox vbox = new VBox(tablesHbox, editHbox);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);

        return vbox;
    }

    // This sets the table underlying data list and creates the corresponding columns
    private void setupTable(TableView<ObservableList<StringProperty>> table, boolean isHex) {
        // config table
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.setEditable(false);
        table.setItems(data);

        // Add row index column
        TableColumn<ObservableList<StringProperty>, String> rowIndexColumn = new TableColumn<>("#");
        rowIndexColumn.setCellValueFactory( row -> new SimpleStringProperty(
                String.format("%02X", data.indexOf(row.getValue()) * 10)
        ));
        rowIndexColumn.setSortable(false);
        rowIndexColumn.setReorderable(false);
        table.getColumns().add(rowIndexColumn);

        // add data columns
        for (int i = 0; i < 10; i++) {
            final int columnIndex = i;
            TableColumn<ObservableList<StringProperty>, String> column = new TableColumn<>(""+i);
            column.setCellValueFactory( row -> {
                if (columnIndex < row.getValue().size()) {
                    String hexValue = row.getValue().get(columnIndex).get();
                    if (isHex) {
                        return new SimpleStringProperty(hexValue);
                    } else {
                        int integerValue = Integer.parseInt(hexValue, 16);
                        char asciiChar = (char) integerValue;
                        return new SimpleStringProperty(String.valueOf(asciiChar));
                    }
                } else {
                    return new SimpleStringProperty("");
                }

            });
            column.setSortable(false);
            column.setReorderable(false);
            table.getColumns().add(column);
        }

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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