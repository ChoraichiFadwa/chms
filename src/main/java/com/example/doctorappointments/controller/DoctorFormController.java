package com.example.doctorappointments.controller;

import com.example.doctorappointments.model.Doctor;
import com.example.doctorappointments.model.Speciality;
import com.example.doctorappointments.service.DoctorService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class DoctorFormController implements Initializable {

    @FXML
    private ComboBox<String> combo_speciality;

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField telField;

    @FXML
    private TextField adresseField;


    private Map<String, Integer> specialityMap = new HashMap<>();



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadSpecialities();
    }

    private void loadSpecialities() {
        ObservableList<Speciality> specialities = DoctorService.getAllSpecialities();
        ObservableList<Doctor> doctor = DoctorService.getDoctorDetails(1);
        System.out.println(doctor);

        ObservableList<Doctor> doctor2 = DoctorService.getDoctorDetails(1468490);
        System.out.println(doctor2);

        for (Speciality speciality : specialities) {
            combo_speciality.getItems().add(speciality.getNomSpeciality());
            specialityMap.put(speciality.getNomSpeciality(), speciality.getIdSpeciality());
        }

    }

    @FXML
    private void handleSubmitButtonAction() {
        String speciality = combo_speciality.getValue();
        if (speciality == null) {
            showAlert(Alert.AlertType.WARNING, "No speciality selected.");
            return;
        }

        Integer specialityId = specialityMap.get(speciality);
        if (specialityId == null) {
            showAlert(Alert.AlertType.WARNING, "Speciality ID not found.");
            return;
        }

        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String tel = telField.getText();
        String adresse = adresseField.getText();

        if (nom.isEmpty() || prenom.isEmpty() || tel.isEmpty() || adresse.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "All fields must be filled.");
            return;
        }

        // Add logic to handle the form submission
        System.out.println("Speciality: " + speciality);
        System.out.println("Speciality ID: " + specialityId);
        System.out.println("Nom: " + nom);
        System.out.println("Prenom: " + prenom);
        System.out.println("Tel: " + tel);
        System.out.println("Adresse: " + adresse);

        boolean done= DoctorService.insertDoctor(specialityId, nom, prenom, tel, adresse);
        if (done) {
            showAlert(Alert.AlertType.INFORMATION, "Doctor added successfully!");
        }
        else {
            showAlert(Alert.AlertType.ERROR, "Error in inserting the new doctor");
        }
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }
}