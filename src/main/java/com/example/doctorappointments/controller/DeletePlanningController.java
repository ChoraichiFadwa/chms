package com.example.doctorappointments.controller;

import com.example.doctorappointments.model.DoctorDTO;
import com.example.doctorappointments.service.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.example.doctorappointments.service.DoctorDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeletePlanningController {

    @FXML
    private ComboBox<String> doctorComboBox; // Pour choisir le médecin

    @FXML
    private DatePicker datePicker; // Pour choisir la date

    @FXML
    private ListView<String> slotsListView; // Liste des créneaux disponibles

    @FXML
    private Button deleteButton; // Bouton de suppression

    @FXML
    private Button loadSlotsButton; // Bouton pour charger les créneaux

    private ObservableList<String> availableSlots = FXCollections.observableArrayList();

    private Map<String, Integer> doctorMap = new HashMap<>(); // Associe les noms aux IDs

    public void initialize() {
        try {
            DoctorDAO doctorDAO = new DoctorDAO();
            List<DoctorDTO> doctors = doctorDAO.getAllDoctors();

            for (DoctorDTO doctor : doctors) {
                String displayName = "Dr " + doctor.getName() + " - " + doctor.getSpeciality();
                doctorComboBox.getItems().add(displayName);
                doctorMap.put(displayName, doctor.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Charger les créneaux disponibles selon le médecin et la date
    @FXML
    private void loadAvailableSlots() {
        String selectedDoctor = doctorComboBox.getSelectionModel().getSelectedItem();
        String selectedDate = (datePicker.getValue() != null) ? datePicker.getValue().toString() : null;

        if (selectedDoctor == null || selectedDate == null) {
            slotsListView.setItems(FXCollections.observableArrayList("Veuillez sélectionner un médecin et une date."));
            deleteButton.setDisable(true);
            return;
        }

        // Récupérer l'ID du médecin depuis le doctorMap
        Integer doctorId = doctorMap.get(selectedDoctor);

        if (doctorId == null) {
            // Si l'ID du médecin n'est pas trouvé, afficher un message d'erreur
            showAlert("Erreur", "Médecin introuvable.");
            return;
        }

        availableSlots.clear(); // Vider la liste des créneaux précédents

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Requête SQL mise à jour pour utiliser l'ID du médecin
            String query = """
           SELECT p.Date_Start
           FROM planning p
           WHERE p.IDDoctor = ? AND p.Date = ? AND p.Availability = 1
        """;

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, doctorId); // Passer l'ID du médecin
            statement.setString(2, selectedDate); // Passer la date sélectionnée

            ResultSet resultSet = statement.executeQuery();

            // Ajouter les créneaux à la liste
            while (resultSet.next()) {
                availableSlots.add(resultSet.getString("Date_Start"));
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Si aucun créneau n'est disponible, afficher un message
        if (availableSlots.isEmpty()) {
            showAlert("Aucun créneau disponible", "Il n'y a pas de créneau disponible pour ce médecin et cette date.");
        } else {
            slotsListView.setItems(availableSlots);
        }

        // Activer ou désactiver le bouton de suppression en fonction des créneaux disponibles
        deleteButton.setDisable(availableSlots.isEmpty());
    }


    // Supprimer le ou les créneaux sélectionnés
    @FXML
    private void deleteSelectedSlots() {
        ObservableList<String> selectedSlots = slotsListView.getSelectionModel().getSelectedItems();

        if (selectedSlots.isEmpty()) {
            return; // Si aucun créneau n'est sélectionné, ne rien faire
        }

        String selectedDoctor = doctorComboBox.getSelectionModel().getSelectedItem();
        String selectedDate = (datePicker.getValue() != null) ? datePicker.getValue().toString() : null;

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Supprimer les créneaux sélectionnés
            String query = """
                DELETE FROM planning
                WHERE IDDoctor = (SELECT IDDoctor FROM doctor WHERE Nom = ?) AND Date = ? AND Date_Start = ?
            """;

            PreparedStatement statement = connection.prepareStatement(query);

            for (String timeSlot : selectedSlots) {
                statement.setString(1, selectedDoctor);
                statement.setString(2, selectedDate);
                statement.setString(3, timeSlot);
                statement.executeUpdate();
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Mettre à jour la liste après suppression
        availableSlots.removeAll(selectedSlots);
        deleteButton.setDisable(availableSlots.isEmpty()); // Désactiver le bouton si plus de créneaux

        showAlert("Suppression réussie", "Les créneaux sélectionnés ont été supprimés avec succès.");
    }

    // Méthode pour afficher une alerte
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
