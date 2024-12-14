package com.example.doctorappointments.service;
import com.example.doctorappointments.model.AppointmentDetails;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AppointmentService {
    public static AppointmentDetails getAppointmentById(int appointmentId) {
        AppointmentDetails appointment = null;

        String query = "SELECT appointment.*, doctor.IDDoctor as doctorID, doctor.IDSpeciality as doctorSpeciality, doctor.Nom as doctorNom, doctor.Prenom as doctorPrenom, doctor.Tel as doctorTel, doctor.Adresse as doctorAdresse, patient.IDPatient as patientID, patient.Nom as patientNom, patient.Prenom as patientPrenom, patient.Sexe as patientSexe, patient.BirthDate as patientBirthDate, patient.Adresse as patientAdresse, patient.Tel as patientTel, patient.IDInsurance as patientInsurance, patient.CIN as patientCIN, patient.Ville as patientVille FROM appointment, `doctor`, `patient` WHERE appointment.IDDoctor = doctor.IDDoctor and appointment.IDPatient=patient.IDPatient and IDAppointment = ?";



        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, appointmentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                appointment = new AppointmentDetails(
                        rs.getInt("IDAppointment"),
                        rs.getInt("doctorID"),
                        rs.getInt("patientID"),
                        rs.getTimestamp("AppointmentDate"),
                        rs.getString("doctorNom") + " " + rs.getString("doctorPrenom"),
                        rs.getString("patientNom") + " " + rs.getString("patientPrenom")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving appointment: " + e.getMessage());
        }

        return appointment;
    }

}