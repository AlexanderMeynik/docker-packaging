package ru.edu.spbstu.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ru.edu.spbstu.client.controllers.LoginFormController;
import ru.edu.spbstu.client.utils.ClientProperties;

import java.io.*;
import java.net.URL;
import java.util.*;

public class ClientApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws IOException {

        Properties props=ClientProperties.getProperties();
        String language=props.getProperty("Language");
        Locale loc=Locale.of("en","UK");
        if(language.equals("RU"))
            loc=Locale.of("ru","Ru");

        ResourceBundle bundle=ResourceBundle.getBundle("local",loc);
        stage=primaryStage;
        URL fxmlUrl = this.getClass()
                .getResource("/fxmls/login_register.fxml");
        if(fxmlUrl==null)
        {
            System.out.println("Not found");
            return;
        }
        FXMLLoader fmxlLoader = new FXMLLoader(getClass().getResource("/fxmls/login_register.fxml"),bundle);
        Parent window = (Pane) fmxlLoader.load();
        LoginFormController conC = fmxlLoader.<LoginFormController>getController();
        conC.setBundle(bundle);

        Scene scene = new Scene(window);
        primaryStage.setScene(scene);
        boolean isSkipped =conC.skipLoginViaToken();
        scene.setUserData(fmxlLoader);//сохраним fmlloader как userdata в сцену
        if(!isSkipped) {
            primaryStage.show();
        }
        primaryStage.setTitle(bundle.getString("FirstForm"));
    }
    public static Stage getStage()
    {
        return stage;
    }
}





