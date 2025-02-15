module com.example.hexeditor {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.hexeditor to javafx.fxml;
    exports com.example.hexeditor;
}