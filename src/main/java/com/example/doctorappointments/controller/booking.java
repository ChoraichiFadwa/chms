package com.example.doctorappointments.controller;

import com.example.doctorappointments.service.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class booking {

    @FXML
    private TextField fullNameField; // Full Name input field
    @FXML
    private TextField phoneNumberField; // Phone Number input field
    @FXML
    private ComboBox<String> specialityComboBox; // Speciality ComboBox
    @FXML
    private ComboBox<String> doctorComboBox; // Doctor ComboBox
    @FXML
    private ComboBox<String> serviceComboBox; // Service ComboBox
    @FXML
    private DatePicker appointmentDatePicker; // DatePicker for selecting the appointment date
    @FXML
    private ComboBox<String> hoursComboBox; // ComboBox for available hours
    @FXML
    private Button createAppointmentButton; // Button to create appointment
    private int patientId;
    private int selectedServiceId;
    @FXML
    private TextField priceField;


    @FXML
    public void initialize() {
        loadServices();
        loadSpecialities();


        specialityComboBox.setOnAction(event -> {
            String selectedSpeciality = specialityComboBox.getSelectionModel().getSelectedItem();
            if (selectedSpeciality != null) {
                loadDoctors(selectedSpeciality);
                doctorComboBox.getSelectionModel().clearSelection(); // Reset doctor selection
            }
        });

        serviceComboBox.setOnAction(event -> {
            String selectedServiceName = serviceComboBox.getSelectionModel().getSelectedItem();
            if (selectedServiceName != null) {
                // Fetch IDService from the database based on selected NameService
                String query = "SELECT IDService FROM service WHERE NameService = ?";
                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement statement = connection.prepareStatement(query)) {

                    statement.setString(1, selectedServiceName);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            int selectedServiceId = resultSet.getInt("IDService");
                            this.selectedServiceId=selectedServiceId;
                            System.out.println("Selected Service ID: " + selectedServiceId);
                            // You can now use the IDService for further processing
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to retrieve service ID. Please try again.");
                }
            }
        });


        doctorComboBox.setOnAction(event -> {
            String selectedDoctorName = doctorComboBox.getSelectionModel().getSelectedItem();
            if (selectedDoctorName != null) {
                ObservableList<LocalDate> availableDates = getAvailableDates(selectedDoctorName);
                if (!availableDates.isEmpty()) {
                    configureDatePicker(availableDates);
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "No Availability", "No available dates for the selected doctor.");
                }
            }
        });

        appointmentDatePicker.setOnAction(event -> {
            LocalDate selectedDate = appointmentDatePicker.getValue();
            String selectedDoctorName = doctorComboBox.getSelectionModel().getSelectedItem();

            if (selectedDate != null && selectedDoctorName != null) {
                ObservableList<String> availableHours = getAvailableHours(selectedDoctorName, selectedDate);
                if (!availableHours.isEmpty()) {
                    hoursComboBox.setItems(availableHours);
                } else {
                    hoursComboBox.getItems().clear(); // Clear previous entries
                    showAlert(Alert.AlertType.INFORMATION, "No Available Hours", "No available hours for the selected date.");
                }
            }
        });
    }

    @FXML
    public void loadServices() {
        String query = "SELECT NameService FROM service";
        serviceComboBox.getItems().clear();
        ObservableList<String> services = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                services.add(resultSet.getString("NameService"));
            }

            serviceComboBox.setItems(services);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load services. Please try again.");
        }
    }

    @FXML
    public void searchPatientByFullName() {
        String fullName = fullNameField.getText();
        String[] nameParts = fullName.split(" ");

        if (nameParts.length < 2) {
            phoneNumberField.setText("Please enter both first and last name.");
            return;
        }

        String lastName = nameParts[0];
        String firstName = nameParts[1];

        String query = "SELECT IDPatient, Tel FROM patient WHERE Nom = ? AND Prenom = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, lastName);
            statement.setString(2, firstName);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int patientId = resultSet.getInt("IDPatient");
                String phoneNumber = resultSet.getString("Tel");

                phoneNumberField.setText(phoneNumber);
                this.patientId = patientId;
            } else {
                phoneNumberField.setText("No patient found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            phoneNumberField.setText("Error connecting to database.");
        }
    }

    @FXML
    public void loadSpecialities() {
        String query = "SELECT NomSpeciality FROM speciality";
        ObservableList<String> specialities = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                specialities.add(resultSet.getString("NomSpeciality"));
            }

            specialityComboBox.setItems(specialities);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void createAppointment() {
        String fullName = fullNameField.getText();
        String selectedSpeciality = specialityComboBox.getSelectionModel().getSelectedItem();
        String selectedDoctorName = doctorComboBox.getSelectionModel().getSelectedItem();
        LocalDate selectedDate = appointmentDatePicker.getValue();
        String selectedHour = hoursComboBox.getSelectionModel().getSelectedItem();
        String priceText = priceField.getText();

        // Vérification si tous les champs sont vides
        if ((fullName == null || fullName.isEmpty()) &&
                (selectedSpeciality == null) &&
                (selectedDoctorName == null) &&
                (selectedDate == null) &&
                (selectedHour == null || selectedHour.isEmpty()) &&
                (priceText == null || priceText.isEmpty())) {
            showAlert(Alert.AlertType.ERROR, "Form Incomplete", "All fields are empty. Please fill in the form.");
            return;
        }

        // Vérifications individuelles
        if (fullName == null || fullName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Full Name", "Full name is required.");
            return;
        }

        if (selectedSpeciality == null) {
            showAlert(Alert.AlertType.ERROR, "Invalid Speciality", "Please select a speciality.");
            return;
        }

        if (selectedDoctorName == null) {
            showAlert(Alert.AlertType.ERROR, "Invalid Doctor", "Please select a doctor.");
            return;
        }

        if (selectedDate == null) {
            showAlert(Alert.AlertType.ERROR, "Invalid Date", "Please select a date for the appointment.");
            return;
        }

        if (selectedHour == null || selectedHour.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Time", "Please select a time slot.");
            return;
        }

        if (priceText == null || priceText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Price", "Price is required.");
            return;
        }

        // Vérification que le nom complet contient au moins un prénom et un nom
        String[] nameParts = fullName.split(" ");
        if (nameParts.length < 2) {
            showAlert(Alert.AlertType.ERROR, "Invalid Full Name", "Please enter both first and last name.");
            return;
        }

        // Conversion du prix en double avec gestion des erreurs
        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Price", "Price must be a valid number.");
            return;
        }

        String lastName = nameParts[0];
        String firstName = nameParts[1];

        // Vérification du nom du docteur
        String[] doctorNameParts = selectedDoctorName.split(" ");
        if (doctorNameParts.length < 2) {
            showAlert(Alert.AlertType.ERROR, "Invalid Doctor Name", "Please enter both first and last name for the doctor.");
            return;
        }
        String doctorLastName = doctorNameParts[0];
        String doctorFirstName = doctorNameParts[1];

        // Conversion de l'heure sélectionnée en LocalTime
        String[] times = selectedHour.split(" - ");
        String startTimeString = times[0].trim();
        LocalTime selectedTime = LocalTime.parse(startTimeString);

        // Combinaison de la date et de l'heure
        LocalDateTime appointmentDateTime = LocalDateTime.of(selectedDate, selectedTime);

        // Requête d'insertion dans la base de données
        String insertQuery = """
        INSERT INTO appointment (IDPatient, IDDoctor, AppointmentDate, Price, Paye, Status, IDService)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertQuery)) {

            if (this.patientId == -1) {
                showAlert(Alert.AlertType.ERROR, "Invalid Patient", "No valid patient ID found.");
                return;
            }

            int doctorId = getDoctorIdByName(doctorFirstName, doctorLastName);

            if (doctorId == -1) {
                showAlert(Alert.AlertType.ERROR, "Doctor Not Found", "No doctor found with the provided name.");
                return;
            }

            statement.setInt(1, this.patientId);
            statement.setInt(2, doctorId);
            statement.setTimestamp(3, Timestamp.valueOf(appointmentDateTime));
            statement.setDouble(4, price);
            statement.setInt(5, 0);
            statement.setString(6, "scheduled");
            statement.setInt(7, selectedServiceId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                markTimeSlotAsUnavailable(appointmentDateTime, doctorId);
                showAlert(Alert.AlertType.INFORMATION, "Appointment Created", "Your appointment has been scheduled successfully.");
                resetForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to schedule the appointment. Please try again.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error connecting to the database. Please try again.");
        }
    }


    // Method to clear the form
    private void resetForm() {
        fullNameField.clear();
        specialityComboBox.getSelectionModel().clearSelection();
        doctorComboBox.getSelectionModel().clearSelection();
        appointmentDatePicker.setValue(null);
        hoursComboBox.getSelectionModel().clearSelection();
        priceField.clear();
        serviceComboBox.getSelectionModel().clearSelection();
        phoneNumberField.clear();
    }
    // Method to mark the selected time slot as unavailable
    private void markTimeSlotAsUnavailable(LocalDateTime appointmentDateTime, int doctorId) {
// Extract date and time from appointmentDateTime
        LocalDate appointmentDate = appointmentDateTime.toLocalDate();
        LocalTime appointmentTime = appointmentDateTime.toLocalTime();

        String query = """
        UPDATE planning
        SET availability = 0
        WHERE Date = ? AND Date_Start = ? AND IDDoctor = ?
    """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Set the parameters for the query
            statement.setDate(1, Date.valueOf(appointmentDate));      // Compare Date column
            statement.setTime(2, Time.valueOf(appointmentTime));      // Compare Date_Start column
            statement.setInt(3, doctorId);                            // Compare id_doctor

            // Execute the update
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Planning availability updated to 0.");
            } else {
                System.out.println("No matching record found to update availability.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update availability.");
        }
    }


    private int getDoctorIdByName(String firstName, String lastName) {
        int doctorId = -1;
        String query = "SELECT IDDoctor FROM doctor WHERE Nom = ? AND Prenom = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, lastName);
            statement.setString(2, firstName);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                doctorId = resultSet.getInt("IDDoctor");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return doctorId;
    }

    private void loadDoctors(String specialityName) {
        String query = """
                SELECT CONCAT(Nom, ' ', Prenom) AS DoctorName
                FROM doctor
                WHERE IDSpeciality = (
                    SELECT IDSpeciality FROM speciality WHERE NomSpeciality = ?)
                """;

        ObservableList<String> doctors = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, specialityName);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                doctors.add(resultSet.getString("DoctorName"));
            }

            doctorComboBox.setItems(doctors);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ObservableList<LocalDate> getAvailableDates(String doctorName) {
        String[] doctorNameParts = doctorName.split(" ");
        if (doctorNameParts.length < 2) {
            return FXCollections.observableArrayList();
        }

        String lastName = doctorNameParts[0];
        String firstName = doctorNameParts[1];

        String query = """
            SELECT Date
            FROM planning
            WHERE availability = 1 AND IDDoctor = (
                SELECT IDDoctor FROM doctor WHERE Nom = ? AND Prenom = ?)
            """;

        ObservableList<LocalDate> availableDates = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, lastName);
            statement.setString(2, firstName);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                availableDates.add(resultSet.getDate("Date").toLocalDate());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return availableDates;
    }

    private ObservableList<String> getAvailableHours(String doctorName, LocalDate selectedDate) {
        String[] doctorNameParts = doctorName.split(" ");
        if (doctorNameParts.length < 2) {
            return FXCollections.observableArrayList();
        }

        String lastName = doctorNameParts[0];
        String firstName = doctorNameParts[1];

        String query = """
    SELECT Date_Start, Date_Fin
    FROM planning
    WHERE availability = 1
      AND IDDoctor = (SELECT IDDoctor FROM doctor WHERE Nom = ? AND Prenom = ?)
      AND Date = ?
    """;

        ObservableList<String> availableHours = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, lastName);
            statement.setString(2, firstName);
            statement.setDate(3, Date.valueOf(selectedDate));

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                LocalTime startTime = resultSet.getTime("Date_Start").toLocalTime();
                LocalTime endTime = resultSet.getTime("Date_Fin").toLocalTime();
                availableHours.add(startTime.toString() + " - " + endTime.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return availableHours;
    }

    private void configureDatePicker(ObservableList<LocalDate> availableDates) {
        appointmentDatePicker.setDayCellFactory(datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || !availableDates.contains(date));
            }
        });
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
