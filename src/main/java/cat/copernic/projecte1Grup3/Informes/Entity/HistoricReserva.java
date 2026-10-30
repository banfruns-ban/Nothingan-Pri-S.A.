package cat.copernic.projecte1Grup3.Informes.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "historic_reserves")
public class HistoricReserva {

    @Id
    private Long idReserva;

    private LocalDateTime dataReserva; // data de creació de la reserva
    private LocalDate dataInici;
    private LocalDate dataFi;

    @Indexed
    private String clientDni;
    private String clientNom;

    private String vehicleMatricula;
    private String vehicleMarca;
    private String vehicleModel;

    private BigDecimal importTotal; // només lloguer, sense fiança
    private EstatHistoric estat;
    private LocalDateTime dataUltimEvent;

    public HistoricReserva() {
    }

    public Long getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(Long idReserva) {
        this.idReserva = idReserva;
    }

    public LocalDateTime getDataReserva() {
        return dataReserva;
    }

    public void setDataReserva(LocalDateTime dataReserva) {
        this.dataReserva = dataReserva;
    }

    public LocalDate getDataInici() {
        return dataInici;
    }

    public void setDataInici(LocalDate dataInici) {
        this.dataInici = dataInici;
    }

    public LocalDate getDataFi() {
        return dataFi;
    }

    public void setDataFi(LocalDate dataFi) {
        this.dataFi = dataFi;
    }

    public String getClientDni() {
        return clientDni;
    }

    public void setClientDni(String clientDni) {
        this.clientDni = clientDni;
    }

    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    public String getVehicleMatricula() {
        return vehicleMatricula;
    }

    public void setVehicleMatricula(String v) {
        this.vehicleMatricula = v;
    }

    public String getVehicleMarca() {
        return vehicleMarca;
    }

    public void setVehicleMarca(String vehicleMarca) {
        this.vehicleMarca = vehicleMarca;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public BigDecimal getImportTotal() {
        return importTotal;
    }

    public void setImportTotal(BigDecimal importTotal) {
        this.importTotal = importTotal;
    }

    public EstatHistoric getEstat() {
        return estat;
    }

    public void setEstat(EstatHistoric estat) {
        this.estat = estat;
    }

    public LocalDateTime getDataUltimEvent() {
        return dataUltimEvent;
    }

    public void setDataUltimEvent(LocalDateTime d) {
        this.dataUltimEvent = d;
    }
}