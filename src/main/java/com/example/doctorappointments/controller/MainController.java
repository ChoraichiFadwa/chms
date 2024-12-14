package com.example.doctorappointments.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    // Method to load the first page
    public void loadPage1(Stage stage) {
        loadPage(stage, "appointment-list.fxml");
    }

    // Method to load the second page
    public void loadPage2(Stage stage) {
        loadPage(stage, "doctor-form.fxml");
    }

    // Method to load the third page
    public void loadPage3(Stage stage) {
        loadPage(stage, "doctors-list.fxml");
    }

    // Helper method to load any page based on its FXML file
    public void loadPage(Stage stage, String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);  // Set the new scene on the existing stage
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
