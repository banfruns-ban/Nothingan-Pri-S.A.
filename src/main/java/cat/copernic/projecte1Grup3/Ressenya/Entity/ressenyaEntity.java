/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Ressenya.Entity;

import cat.copernic.projecte1Grup3.Reserves.Entity.reservesEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Ressenya")
public class ressenyaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ressenya")
    private Long idRessenya;

    @ManyToOne
    @JoinColumn(name = "Matricula_vehicle",
            referencedColumnName = "matricula")
    private VehiclesEntity vehicle;

    @ManyToOne
    @JoinColumn(name = "DNI_client", referencedColumnName = "DNI")
    private clientEntity client;

    @ManyToOne
    @JoinColumn(name = "codi_reserva")
    private reservesEntity reserva;

    @Min(1)
    @Max(5)
    @Column(nullable = false ,name  = "Puntuacio")
    private int puntuacio;

    @Column(name = "comentari")
    private String comentari;

   @Column(nullable = false)
    private LocalDateTime dataCreacio;

    public ressenyaEntity() {
    }

    public ressenyaEntity(int puntuacio, String comentari, LocalDateTime dataCreacio, clientEntity client, VehiclesEntity vehicle) {
        this.puntuacio = puntuacio;
        this.comentari = comentari;
        this.dataCreacio = dataCreacio;
        this.client = client;
        this.vehicle= vehicle;
    }

    public Long getIdRessenya() {
        return idRessenya;
    }

    public void setIdRessenya(Long idRessenya) {
        this.idRessenya = idRessenya;
    }

    public int getPuntuacio() {
        return puntuacio;
    }

    public void setPuntuacio(int puntuacio) {
        this.puntuacio = puntuacio;
    }

    public String getComentari() {
        return comentari;
    }

    public void setComentari(String comentari) {
        this.comentari = comentari;
    }

    public LocalDateTime getDataCreacio() {
        return dataCreacio;
    }

    public void setDataCreacio(LocalDateTime dataCreacio) {
        this.dataCreacio = dataCreacio;
    }

    public clientEntity getClient() {
        return client;
    }

    public void setClient(clientEntity client) {
        this.client = client;
    }
        public VehiclesEntity getVehicle() {
        return vehicle;
    }

    public void setVehicle(VehiclesEntity vehicle) {
        this.vehicle = vehicle;
    }
}
