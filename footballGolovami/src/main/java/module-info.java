module com.example.footballgolovami {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens com.example.footballgolovami to javafx.fxml;
    exports com.example.footballgolovami;
}