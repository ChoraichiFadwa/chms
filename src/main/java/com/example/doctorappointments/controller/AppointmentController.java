package com.example.doctorappointments.controller;

import com.example.doctorappointments.model.Appointment;
import com.example.doctorappointments.service.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.List;

public class AppointmentController {

    @FXML
    private TableView<Appointment> AppointmentTableView;

    @FXML
    private TableColumn<Appointment, String> PatientNameTableColumn;
    @FXML
    private TableColumn<Appointment, String> AppointmentDateTableColumn;
    @FXML
    private TableColumn<Appointment, String> AppointmentTimeTableColumn;
    @FXML
    private TableColumn<Appointment, String> ServiceTableColumn;
    @FXML
    private TableColumn<Appointment, String> StatusTableColumn;
    @FXML
    private TableColumn<Appointment, Void> ActionsTableColumn;


    @FXML
    private Pagination pagination;

    private ObservableList<Appointment> appointments;
    private int rowsPerPage = 14;

    @FXML
    private void initialize() {


        List<Appointment> appointmentsFromDB = Appointment.getAllAppointments();
        appointments = FXCollections.observableArrayList(appointmentsFromDB);
//        appointments = FXCollections.observableArrayList(
//                new Appointment(1, 101, 201, Timestamp.valueOf("2025-05-01 12:24:18"), 100.0, 0, "Scheduled", "Checkup"),
//                new Appointment(2, 102, 202, Timestamp.valueOf("2024-12-05 10:00:00"), 150.0, 1, "Completed", "Routine"),
//                new Appointment(3, 103, 203, Timestamp.valueOf("2025-06-10 14:00:00"), 200.0, 0, "Scheduled", "Consultation"),
//                new Appointment(4, 104, 204, Timestamp.valueOf("2024-12-15 09:30:00"), 120.0, 1, "Completed", "Follow-up"),
//                new Appointment(5, 105, 205, Timestamp.valueOf("2024-12-25 11:00:00"), 130.0, 0, "Scheduled", "Checkup"),
//                new Appointment(6, 106, 206, Timestamp.valueOf("2025-07-01 15:30:00"), 140.0, 0, "Scheduled", "Routine"),
//                new Appointment(7, 107, 207, Timestamp.valueOf("2025-07-10 10:15:00"), 110.0, 1, "Completed", "Checkup")
//        );



        AppointmentTableView.setItems(appointments);

//        PatientNameTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty("Patient " + cellData.getValue().getIDPatient()));
        PatientNameTableColumn.setCellValueFactory(cellData -> {
            Appointment appointment = cellData.getValue();
            String patientFullName = getPatientFullNameById(appointment.getIDPatient());
            return new SimpleStringProperty(patientFullName);
        });
        AppointmentDateTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate()));
        AppointmentTimeTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedTime()));
        ServiceTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getService()));
        StatusTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));


        pagination.setPageCount((int) Math.ceil(appointments.size() / (double) rowsPerPage));
        pagination.setCurrentPageIndex(0);
        pagination.setMaxPageIndicatorCount(5);

        pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> updateTableView(newValue.intValue()));
        updateTableView(pagination.getCurrentPageIndex());

        ActionsTableColumn.setCellFactory(param -> new TableCell<Appointment, Void>() {
            private final Button ordonnanceTestButton = new Button("Ordonnance Test");
            private final Button ordonnanceVisiteButton = new Button("Ordonnance Visite");
            private final Button reporterButton = new Button("Reporter");
            private final Button annulerButton = new Button("Annuler");

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {

                    ordonnanceTestButton.getStyleClass().add("button-action");
                    ordonnanceVisiteButton.getStyleClass().add("button-action");
                    reporterButton.getStyleClass().add("button-action");
                    annulerButton.getStyleClass().add("button-action");

                    ordonnanceTestButton.getStyleClass().add("button-ordonnance");
                    ordonnanceVisiteButton.getStyleClass().add("button-ordonnance");
                    reporterButton.getStyleClass().add("button-reporter");
                    annulerButton.getStyleClass().add("button-annuler");

                    HBox hBox = new HBox(10);
                    hBox.setAlignment(Pos.CENTER);
                    hBox.getChildren().addAll(ordonnanceTestButton, ordonnanceVisiteButton, reporterButton, annulerButton);

                    ordonnanceTestButton.setOnAction(event -> handleOrdonnanceTest(getTableRow().getItem()));
                    ordonnanceVisiteButton.setOnAction(event -> handleOrdonnanceVisite(getTableRow().getItem()));
                    reporterButton.setOnAction(event -> handleReporter(getTableRow().getItem()));
                    annulerButton.setOnAction(event -> handleAnnuler(getTableRow().getItem()));

                    setGraphic(hBox);
                }
            }
        });
    }

    private String getPatientFullNameById(int patientId) {
        String fullName = "";
        String query = "SELECT Nom, Prenom FROM patient WHERE IDPatient = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, patientId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String nom = resultSet.getString("Nom");
                String prenom = resultSet.getString("Prenom");
                fullName = prenom + " " + nom;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fullName;
    }


    private void updateTableView(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, appointments.size());
        AppointmentTableView.setItems(FXCollections.observableArrayList(appointments.subList(fromIndex, toIndex)));
    }

    private void handleOrdonnanceTest(Appointment appointment) {
        System.out.println("Ordonnance Test clicked for: " + appointment.getIDPatient());
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/doctorappointments/prescription-test-form.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Test Prescription Form" + appointment.getIDPatient());
            stage.setScene(new Scene(root, 1100, 700));
            stage.show();

            // Pass the appointment ID to the next interface
            TestFormController controller = fxmlLoader.getController();
            controller.setAppointmentID(appointment.getIDAppointment());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleOrdonnanceVisite(Appointment appointment) {
        System.out.println("Ordonnance Visite clicked for: " + appointment.getIDPatient());

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/doctorappointments/prescription-medical-form.fxml"));

            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Medical Prescription Form" + appointment.getIDPatient());
            stage.setScene(new Scene(root, 1100, 700));
            stage.show();

            // Pass the appointment ID to the next interface
            MedicationFormController controller = fxmlLoader.getController();
            controller.setAppointmentID(appointment.getIDAppointment());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleReporter(Appointment appointment) {
        System.out.println("Reporter clicked for: " + appointment.getIDPatient());

    }

    private void handleAnnuler(Appointment appointment) {
        System.out.println("Annuler clicked for: " + appointment.getIDPatient());
    }

    public void handleCreateMedicalPrescription() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("prescription-medical-form.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Medical Prescription Form");
            stage.setScene(new Scene(root, 1100, 700));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleCreateTestPrescription() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("prescription-test-form.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Test Prescription Form");
            stage.setScene(new Scene(root, 1100, 700));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
