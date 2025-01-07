module wikifont.wikifontinterface {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    
    requires org.apache.logging.log4j;
    requires com.google.gson;
    opens wikifont.wikifontinterface to javafx.fxml;
    exports wikifont.wikifontinterface;
}
