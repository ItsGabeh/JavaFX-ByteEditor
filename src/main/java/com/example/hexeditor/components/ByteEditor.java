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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * ByteEditor
 */
public class ByteEditor extends VBox {

    /* ASCII and HEX tables */
    private final TableView<byte[]> asciiTable = new TableView<>();
    private final TableView<byte[]> hexTable = new TableView<>();

    /* ObservableList of StringProperty */
    private final ObservableList<byte[]> data = FXCollections.observableArrayList(); // direct byte storage

    /* Editor layout components */
    private final Spinner<Integer> posSpinner = new Spinner<>();
    private final TextField asciiTextField = new TextField();
    private final TextField hexTextField = new TextField();
    private final Button saveButton = new Button("Save");

    public ByteEditor() {

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
            data.add(Arrays.copyOfRange(bytes, i, Math.min(i + rowSize, bytes.length)));
        }
    }

    private String byteToHex(byte b) {
        return String.format("%02X", b & 0xFF);
    }

    private String byteToAscii(byte b) {
//        return (b >= 32 && b <= 126) ? String.valueOf((char) b) : ".";
        return String.valueOf((char) b);
    }

    public void refreshTables() {
        asciiTable.refresh();
        hexTable.refresh();
    }

    public byte[] getBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (byte[] row : data) {
            baos.write(row, 0, row.length);
        }
        return baos.toByteArray();
    }

    private void setupTable(TableView<byte[]> table, boolean isHex) {
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.setEditable(false);
        table.setItems(data);

        TableColumn<byte[], String> rowIndexColumn = new TableColumn<>("#");
        rowIndexColumn.setCellValueFactory(row -> new SimpleStringProperty(
                String.valueOf(data.indexOf(row.getValue()) * 10)
        ));
        table.getColumns().add(rowIndexColumn);

        for (int i = 0; i < 10; i++) {
            final int columnIndex = i;
            TableColumn<byte[], String> column = new TableColumn<>(String.valueOf(i));

            column.setCellValueFactory(row -> {
                byte[] rowData = row.getValue();
                if (columnIndex < rowData.length) {
                    byte b = rowData[columnIndex];
                    return new SimpleStringProperty(isHex ? byteToHex(b) : byteToAscii(b));
                }
                return new SimpleStringProperty("");
            });

            table.getColumns().add(column);
        }

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void syncSelection() {
        ListChangeListener<TablePosition> tableListener = change -> {
            if (!change.getList().isEmpty()) {
                TablePosition<?, ?> tablePosition = change.getList().getFirst();
                int row = tablePosition.getRow();
                int col = tablePosition.getColumn() - 1;

                if (col >= 0 && row < data.size() && col < data.get(row).length) {
                    if (posSpinner.getValue() != row * 10 + col) {
                        posSpinner.getValueFactory().setValue(row * 10 + col);
                    }

                    syncTables(row, col + 1);

                    byte selectedByte = data.get(row)[col];
                    hexTextField.setText(byteToHex(selectedByte));
                    asciiTextField.setText(byteToAscii(selectedByte));
                }
            }
        };

        hexTable.getSelectionModel().getSelectedCells().addListener(tableListener);
        asciiTable.getSelectionModel().getSelectedCells().addListener(tableListener);

        posSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int row = newVal / 10;
                int col = newVal % 10;

                if (row < data.size() && col < data.get(row).length) {

                    if (!isTableSelected(row, col + 1)) {
                        syncTables(row, col + 1);
                    }

                    byte selectedByte = data.get(row)[col];
                    hexTextField.setText(byteToHex(selectedByte));
                    asciiTextField.setText(byteToAscii(selectedByte));
                }
            }
        });
    }

    private void syncTables(int row, int col) {
        hexTable.getSelectionModel().clearAndSelect(row, hexTable.getColumns().get(col));
        asciiTable.getSelectionModel().clearAndSelect(row, asciiTable.getColumns().get(col));

        hexTable.scrollTo(row);
        asciiTable.scrollTo(row);
    }

    private boolean isTableSelected(int row, int col) {
        TablePosition<?, ?> hexPos = hexTable.getSelectionModel().getSelectedCells().stream().findFirst().orElse(null);
        TablePosition<?, ?> asciiPos = asciiTable.getSelectionModel().getSelectedCells().stream().findFirst().orElse(null);
        return (hexPos != null && hexPos.getRow() == row && hexPos.getColumn() == col) ||
                (asciiPos != null && asciiPos.getRow() == row && asciiPos.getColumn() == col);
    }


    private void setupEditorControls() {
        posSpinner.setEditable(true);
        posSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));
        posSpinner.setDisable(true);

        data.addListener((ListChangeListener<byte[]>) c -> {
            int totalBytes = data.stream().mapToInt(row -> row.length).sum();
            int max = Math.max(0, totalBytes - 1);
            posSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, max, 0));
            posSpinner.setDisable(totalBytes == 0);
        });

        saveButton.setOnAction(event -> {
            int row = posSpinner.getValue() / 10;
            int col = posSpinner.getValue() % 10;

            if (row < data.size() && col < data.get(row).length) {
                String newHex = hexTextField.getText().toUpperCase();
                if (!newHex.matches("[0-9A-F]{2}")) return;

                data.get(row)[col] = (byte) Integer.parseInt(newHex, 16);
                asciiTable.refresh();
                hexTable.refresh();
            }
        });

        hexTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("[0-9A-Fa-f]{0,2}")) {
                if (newValue.length() == 2) {
                    asciiTextField.setText(byteToAscii((byte) Integer.parseInt(newValue, 16)));
                }
            } else {
                hexTextField.setText(oldValue);
            }
        });

        asciiTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() <= 1) {
                if (!newValue.isEmpty()) {
                    hexTextField.setText(byteToHex((byte) newValue.charAt(0)));
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
