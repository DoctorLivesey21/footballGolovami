module com.example.footballgolovami {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.footballgolovami to javafx.fxml;
    exports com.example.footballgolovami;
}