package com.example.hexeditor.components;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.nio.charset.StandardCharsets;

/**
 * ByteEditor
 */
public class ByteEditor extends VBox {

    /* ASCII and HEX tables */
    private final TableView<ObservableList<StringProperty>> asciiTable = new TableView<>();
    private final TableView<ObservableList<StringProperty>> hexTable = new TableView<>();

    /* ObservableList of StringProperty */
    private final ObservableList<ObservableList<StringProperty>> data = FXCollections.observableArrayList();

    /* Editor layout components */
    private final Spinner<Integer> posSpinner = new Spinner<>();
    private final TextField asciiTextField = new TextField();
    private final TextField hexTextField = new TextField();
    private final Button saveButton = new Button("Save");

    public ByteEditor() {
        // convert byte array to ob list
        // this.data = data;

        // config tables
        setupTable(asciiTable, false);
        setupTable(hexTable, true);
        syncSelection();

        // config edition layout
        setupEditorControls();

        // Ascii editor Vbox
        VBox asciiVbox = new VBox(
                new Label("ASCII editor"),
                asciiTable
        );
        asciiVbox.setAlignment(Pos.CENTER_LEFT);
        asciiVbox.setSpacing(10);

        // Hex editor Vbox
        VBox hexVbox = new VBox(
                new Label("HEX editor"),
                hexTable
        );
        hexVbox.setAlignment(Pos.CENTER_LEFT);
        hexVbox.setSpacing(10);

        // table layout
        SplitPane splitPane = new SplitPane(asciiVbox, hexVbox);
        splitPane.setDividerPositions(0.35);

        // main layout
        VBox mainVbox = new VBox(splitPane, createEditorPanel());
        mainVbox.setAlignment(Pos.CENTER);
        mainVbox.setSpacing(10);

        this.getChildren().add(mainVbox);
    }

    public void loadByteArray(byte[] bytes) {
        data.clear();
        int rowSize = 10;

        for (int i = 0; i < bytes.length; i += rowSize) {
            ObservableList<StringProperty> row = FXCollections.observableArrayList();
            for (int j = 0; j < rowSize && i+j < bytes.length ;j++) {
                byte b = bytes[i + j];
                String hex = Integer.toHexString(b);
                row.add(new SimpleStringProperty(hex));
            }

            data.add(row);
        }
    }

    public void refreshTables() {
        asciiTable.refresh();
        hexTable.refresh();
    }

    private void setupTable(TableView<ObservableList<StringProperty>> table, boolean isHex) {
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.setEditable(false);
        table.setItems(data);

        // index columns
        TableColumn<ObservableList<StringProperty>, String> rowIndexColumn = new TableColumn<>("#");
        rowIndexColumn.setCellValueFactory(row -> new SimpleStringProperty(
                String.valueOf(data.indexOf(row.getValue()) * 10) // Show the index in decimal format
        ));
        rowIndexColumn.setSortable(false);
        rowIndexColumn.setReorderable(false);
        table.getColumns().add(rowIndexColumn);

        // data columns
        for (int i = 0; i < 10; i++) {
            final int columnIndex = i;
            TableColumn<ObservableList<StringProperty>, String> column = new TableColumn<>(String.valueOf(i));
            column.setCellValueFactory(row -> {
                if (columnIndex < row.getValue().size()) {
                    String hexValue = row.getValue().get(columnIndex).get();
                    if (isHex) {
                        return new SimpleStringProperty(hexValue);
                    } else {
//                        int intValue = Integer.parseInt(hexValue, 16);
//                        char asciiChar = (char) intValue;
//                        return new SimpleStringProperty(String.valueOf((intValue >= 32 && intValue <= 128) ? String.valueOf(asciiChar) : ""));

                        // Convert correctly to utf 8 string representation
                        long longValue = Long.parseLong(hexValue, 16);
                        byte[] bytes = {(byte) longValue};
                        String asciiValue = new String(bytes, StandardCharsets.UTF_8);
                        return new SimpleStringProperty((longValue >= 32) ? asciiValue : "");
                    }
                }
                return new SimpleStringProperty(""); // no index errors
            });
            column.setSortable(false);
            column.setReorderable(false);
            table.getColumns().add(column);
        }

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void syncSelection() {
        ListChangeListener<TablePosition> listener = change -> {
            if (!change.getList().isEmpty()) {
                TablePosition<?, ?> tablePosition = change.getList().getFirst();
                int row = tablePosition.getRow();
                int col = tablePosition.getColumn();

                if (col > 0 && row < data.size() && col - 1 < data.get(row).size()) {
                    // sync the tables
                    hexTable.getSelectionModel().clearAndSelect(row, hexTable.getColumns().get(col));
                    asciiTable.getSelectionModel().clearAndSelect(row, asciiTable.getColumns().get(col));
                    // make both tables scroll to the same row
                    hexTable.scrollTo(row);
                    asciiTable.scrollTo(row);

                    // update spinner and text fields
                    posSpinner.getValueFactory().setValue(row * 10 + (col - 1));
                    String hex = data.get(row).get(col - 1).get();
                    hexTextField.setText(hex);

//                    int intValue = Integer.parseInt(hex, 16);
//                    char asciiChar = (char) intValue;
//                    asciiTextField.setText((intValue >= 32 && intValue <= 128) ? String.valueOf(asciiChar) : "");

                    // correct utf 8 character conversion
                    long longValue = Long.parseLong(hex, 16);
                    byte[] bytes = {(byte) longValue};
                    String asciiValue = new String(bytes, StandardCharsets.UTF_8);
                    asciiTextField.setText((longValue >= 32) ? asciiValue : "");
                }
            }
        };

        hexTable.getSelectionModel().getSelectedCells().addListener(listener);
        asciiTable.getSelectionModel().getSelectedCells().addListener(listener);
    }

    private void setupEditorControls() {
        posSpinner.setEditable(false);
        posSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));
        posSpinner.setDisable(true);

        data.addListener((ListChangeListener<ObservableList<StringProperty>>) c -> {
            int totalBytes = data.stream().mapToInt(ObservableList::size).sum();
            int max = Math.max(0, totalBytes - 1);
            posSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, max, 0));
            posSpinner.setDisable(totalBytes == 0);
        });

        posSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            int row = newValue / 10;
            int col = (newValue % 10) + 1;
            if (row < data.size() && col < hexTable.getColumns().size()) {
                hexTable.getSelectionModel().clearAndSelect(row, hexTable.getColumns().get(col));
                asciiTable.getSelectionModel().clearAndSelect(row, asciiTable.getColumns().get(col));
            }
        });

        saveButton.setOnAction(event -> {
            int row = posSpinner.getValue() / 10;
            int col = posSpinner.getValue() % 10;

            if (row < data.size() && col < data.get(row).size()) {
                String newHex = hexTextField.getText().toUpperCase();
                String newAscii = asciiTextField.getText();

                if (!newAscii.isEmpty()) {
                    newHex = String.format("%02X", (int) newAscii.charAt(0));
                }

                data.get(row).get(col).set(newHex);
                // Refresh needed
                asciiTable.refresh();
                hexTable.refresh();
            }
        });

        // add listeners to textfields
        hexTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("[0-9a-f]{0,2}")) {
                if (newValue.length() == 2) {
//                    int intValue = Integer.parseInt(newValue, 16);
//                    char asciiChar = (char) intValue;
//                    asciiTextField.setText((intValue >= 32 && intValue <= 126) ? String.valueOf(asciiChar) : "");

                    long longValue = Long.parseLong(newValue, 16);
                    byte[] bytes = {(byte) longValue};
                    String asciiValue = new String(bytes, StandardCharsets.UTF_8);
                    asciiTextField.setText(asciiValue.trim());
                }
            } else {
                hexTextField.setText(oldValue);
            }
        });

        asciiTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() <= 1) {
                if (!newValue.isEmpty()) {
//                    char asciiChar = newValue.charAt(0);
//                    hexTextField.setText(Integer.toHexString(asciiChar));

                    byte[] bytes = newValue.getBytes(StandardCharsets.UTF_8);
                    StringBuilder hexValue = new StringBuilder();
                    for (byte b : bytes) {
                        hexValue.append(String.format("%02X", b));
                    }
                    hexTextField.setText(hexValue.toString());
                }
            } else {
                asciiTextField.setText(oldValue);
            }
        });

        asciiTextField.disableProperty().bind(posSpinner.disabledProperty());
        hexTextField.disableProperty().bind(posSpinner.disabledProperty());
        saveButton.disableProperty().bind(posSpinner.disabledProperty());

    }

    private Region createEditorPanel() {
        HBox hBox = new HBox(
                new Label("Position"),
                posSpinner,
                new Label("ASCII"),
                asciiTextField,
                new Label("HEX"),
                hexTextField,
                saveButton
        );
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER);

        return hBox;
    }

}
