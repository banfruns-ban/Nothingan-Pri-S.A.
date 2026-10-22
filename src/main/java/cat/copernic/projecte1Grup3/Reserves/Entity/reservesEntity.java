package cat.copernic.projecte1Grup3.Reserves.Entity;

import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.hibernate.annotations.Audited.Table;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

@Entity
@Table(name = "Reserves")
public class reservesEntity {
  @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codi_reserva")
    private Long codiReserva;

    @ManyToOne
    @JoinColumn(name = "DNI_client", referencedColumnName = "DNI")
    private clientEntity client;

    @ManyToOne
    @JoinColumn(name = "Matricula_vehicle",
                referencedColumnName = "matricula")
    private VehiclesEntity vehicle;

    @Column(name = "data_inici")
    private LocalDate dataInici;

    @Column(name = "data_fi")
    private LocalDate dataFi;

    @Column(name = "hora_inici")
    private LocalTime horaInici;

    @Column(name = "hora_fi")
    private LocalTime horaFi;

    @Column(name = "cost_total", precision = 10, scale = 2)
    private BigDecimal costTotal;

    @Column(name = "fiança", precision = 10, scale = 2)
    private BigDecimal fianca;

    @Column(name = "data_lliurament")
    private LocalDate dataLliurament;

    @Column(name = "estat")
    private String estat;

    @Column(name = "data_retorn")
    private LocalDate dataRetorn;

    @Column(name = "observacions", columnDefinition = "TEXT")
    private String observacions;

    public reservesEntity() {
    }
     
    
    public reservesEntity(Long codiReserva, clientEntity client, VehiclesEntity vehicle, LocalDate dataInici, LocalDate dataFi, LocalTime horaInici, LocalTime horaFi, BigDecimal costTotal, BigDecimal fianca, LocalDate dataLliurament, String estat, LocalDate dataRetorn, String observacions) {
        this.codiReserva = codiReserva;
        this.client = client;
        this.vehicle = vehicle;
        this.dataInici = dataInici;
        this.dataFi = dataFi;
        this.horaInici = horaInici;
        this.horaFi = horaFi;
        this.costTotal = costTotal;
        this.fianca = fianca;
        this.dataLliurament = dataLliurament;
        this.estat = estat;
        this.dataRetorn = dataRetorn;
        this.observacions = observacions;
    }
    
    public Long getCodiReserva() {
        return codiReserva;
    }

    public void setCodiReserva(Long codiReserva) {
        this.codiReserva = codiReserva;
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

    public LocalTime getHoraInici() {
        return horaInici;
    }

    public void setHoraInici(LocalTime horaInici) {
        this.horaInici = horaInici;
    }

    public LocalTime getHoraFi() {
        return horaFi;
    }

    public void setHoraFi(LocalTime horaFi) {
        this.horaFi = horaFi;
    }

    public BigDecimal getCostTotal() {
        return costTotal;
    }

    public void setCostTotal(BigDecimal costTotal) {
        this.costTotal = costTotal;
    }

    public BigDecimal getFianca() {
        return fianca;
    }

    public void setFianca(BigDecimal fianca) {
        this.fianca = fianca;
    }

    public LocalDate getDataLliurament() {
        return dataLliurament;
    }

    public void setDataLliurament(LocalDate dataLliurament) {
        this.dataLliurament = dataLliurament;
    }

    public String getEstat() {
        return estat;
    }

    public void setEstat(String estat) {
        this.estat = estat;
    }

    public LocalDate getDataRetorn() {
        return dataRetorn;
    }

    public void setDataRetorn(LocalDate dataRetorn) {
        this.dataRetorn = dataRetorn;
    }

    public String getObservacions() {
        return observacions;
    }

    public void setObservacions(String observacions) {
        this.observacions = observacions;
    }  
}
