module com.pya.javamusicplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires mp3agic;
    requires javafx.media;
    requires com.jfoenix;
    requires java.sql;

    opens com.pya.javamusicplayer to javafx.fxml;
    exports com.pya.javamusicplayer;
}