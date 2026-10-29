package cat.copernic.projecte1Grup3.Reserves.Entity;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.Id;

@Document(collection = "HistoricReserves")
public class historicReserva {

    @Id
    private String id; // ID técnico interno de MongoDB (_id)

    @Field("idReserva")
    private String idReserva; // Clave primaria de negocio del diagrama

    @Field("clientDni")
    private String clientDni; // DNI del cliente asociado

    @Field("matriculaVehicle")
    private String matriculaVehicle; // Matrícula del vehículo alquilado

    @Field("dataInici")
    private LocalDateTime dataInici; // Fecha y hora de inicio de la reserva

    @Field("dataFi")
    private LocalDateTime dataFi; // Fecha y hora de finalización

    @Field("preuTotal")
    private Double preuTotal; // Importe total de la reserva

    // Constructor vacío (requerido por Spring Data)
    public historicReserva() {
    }

    public historicReserva(String idReserva, String clientDni, String matriculaVehicle, 
                           LocalDateTime dataInici, LocalDateTime dataFi, Double preuTotal) {
        this.idReserva = idReserva;
        this.clientDni = clientDni;
        this.matriculaVehicle = matriculaVehicle;
        this.dataInici = dataInici;
        this.dataFi = dataFi;
        this.preuTotal = preuTotal;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(String idReserva) {
        this.idReserva = idReserva;
    }

    public String getClientDni() {
        return clientDni;
    }

    public void setClientDni(String clientDni) {
        this.clientDni = clientDni;
    }

    public String getMatriculaVehicle() {
        return matriculaVehicle;
    }

    public void setMatriculaVehicle(String matriculaVehicle) {
        this.matriculaVehicle = matriculaVehicle;
    }

    public LocalDateTime getDataInici() {
        return dataInici;
    }

    public void setDataInici(LocalDateTime dataInici) {
        this.dataInici = dataInici;
    }

    public LocalDateTime getDataFi() {
        return dataFi;
    }

    public void setDataFi(LocalDateTime dataFi) {
        this.dataFi = dataFi;
    }

    public Double getPreuTotal() {
        return preuTotal;
    }

    public void setPreuTotal(Double preuTotal) {
        this.preuTotal = preuTotal;
    }
}
