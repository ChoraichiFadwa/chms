package com.example.doctorappointments.controller;

import com.example.doctorappointments.model.Doctor;
import com.example.doctorappointments.model.Speciality;
import com.example.doctorappointments.service.DoctorService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.ChoiceDialog;

import java.util.List;

public class DoctorsList {

    @FXML
    private TableView<Doctor> doctorTableView;

    @FXML
    private TableColumn<Doctor, String> doctorNameTableColumn;

    @FXML
    private TableColumn<Doctor, String> specialityTableColumn;

    @FXML
    private TableColumn<Doctor, String> telTableColumn;

    @FXML
    private TableColumn<Doctor, String> adresseTableColumn;
    @FXML
    private Button filterButton;
    @FXML
    private TextField searchField; // Bind this to the search functionality

    @FXML
    private TextField doctorNameField;

    @FXML
    private TextField doctorSpecialityField;

    @FXML
    private TextField doctorPhoneField;

    @FXML
    private TextField doctorAddressField;

    private ObservableList<Doctor> doctors;
    private DoctorService doctorService;  // Instance of DoctorService

    @FXML
    private void initialize() {
        // Instantiate the DoctorService
        doctorService = new DoctorService();

        // Fetch doctors from the service
        doctors = FXCollections.observableArrayList();
        doctors.addAll(doctorService.getAllDoctors());  // Assuming this method returns all doctors

        // Setting up the TableView columns
        doctorNameTableColumn.setCellValueFactory(cellData -> {
            Doctor doctor = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(doctor.getPrenom() + " " + doctor.getNom());
        });

        specialityTableColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSpecialityName()));  // Show the speciality name
        telTableColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTel()));
        adresseTableColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAdresse()));


        // Dynamically set column widths (optional)
        doctorNameTableColumn.prefWidthProperty().bind(doctorTableView.widthProperty().multiply(0.25));
        specialityTableColumn.prefWidthProperty().bind(doctorTableView.widthProperty().multiply(0.20));
        telTableColumn.prefWidthProperty().bind(doctorTableView.widthProperty().multiply(0.25));
        adresseTableColumn.prefWidthProperty().bind(doctorTableView.widthProperty().multiply(0.30));

        // Populate the TableView with the doctor data
        doctorTableView.setItems(doctors);

        filterButton.setOnMouseClicked(this::onFilterButtonClicked);

    }
    // Handle filter button click
    private void onFilterButtonClicked(MouseEvent event) {
        // Open a dialog or dropdown with all available specialties
        List<Speciality> specialties = DoctorService.getAllSpecialities();
        ChoiceDialog<Speciality> dialog = new ChoiceDialog<>(null, specialties);
        dialog.setTitle("Choose a Specialty");
        dialog.setHeaderText("Select the specialty to filter doctors by");
        dialog.setContentText("Specialty:");


        dialog.showAndWait().ifPresent(selectedSpeciality -> {
            filterDoctorsBySpecialty(selectedSpeciality.getNomSpeciality());
        });
    }

    private void filterDoctorsBySpecialty(String specialtyName) {
        ObservableList<Doctor> filteredDoctors = FXCollections.observableArrayList();

        for (Doctor doctor : doctors) {
            if (doctor.getSpecialityName().equalsIgnoreCase(specialtyName)) {
                filteredDoctors.add(doctor);
            }
        }

        doctorTableView.setItems(filteredDoctors);  // Display filtered list
    }
    private void handleRowSelection(MouseEvent event) {
        Doctor selectedDoctor = doctorTableView.getSelectionModel().getSelectedItem();
        if (selectedDoctor != null) {
            // Display selected doctor details in text fields
            doctorNameField.setText(selectedDoctor.getPrenom() + " " + selectedDoctor.getNom());
            doctorSpecialityField.setText(String.valueOf(selectedDoctor.getIdSpeciality())); // You can map this to the specialty name if needed
            doctorPhoneField.setText(selectedDoctor.getTel());
            doctorAddressField.setText(selectedDoctor.getAdresse());
        }
    }
}