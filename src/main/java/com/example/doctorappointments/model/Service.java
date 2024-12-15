package com.example.doctorappointments.model;

public class Service {
    private int idService;
    private String NameService;

    public Service(int idService, String nameService) {
        this.idService = idService;
        this.NameService = nameService;
    }

    public int getIdService() {
        return idService;
    }

    public String getNameService() {
        return NameService;
    }

    @Override
    public String toString() {
        return NameService; // This will display the service name in the ComboBox
    }
}
