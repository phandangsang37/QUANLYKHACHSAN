package com.example.hotel;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

public class Main extends Application {
    public static void main(String[] args){
        launch(args);
    }
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Quản lý khách sạn");
        FXMLLoader loader = new FXMLLoader(
            Main.class.getResource("/com/example/hotel/fxml/login.fxml")
        );
        Scene scene = new Scene(loader.load(),800,500);
        stage.setScene(scene);
        stage.show();
    }
   

}