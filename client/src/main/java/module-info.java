module ru.edu.spbstu.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.fasterxml.jackson.databind;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires ru.edu.spbstu.model;
    requires ru.edu.spbstu.clientComponents;

    opens ru.edu.spbstu.client to javafx.fxml;
    opens ru.edu.spbstu.client.controllers to javafx.fxml;
    exports ru.edu.spbstu.client;
}