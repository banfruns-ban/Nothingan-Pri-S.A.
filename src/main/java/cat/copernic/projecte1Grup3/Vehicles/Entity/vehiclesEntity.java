/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Vehicles.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import org.hibernate.annotations.Audited.Table;

@Entity
@Table(name = "vehicle")
public class vehiclesEntity {
    @Id
    @Column(name = "matricula", nullable = false, length = 15)
    private String matricula;

    @Column(name = "marca", nullable = false, length = 50)
    private String marca;

    @Column(name = "model",nullable = false, length = 50)
    private String model;

    @Column(name = "descripcio",nullable = false, columnDefinition = "TEXT")
    private String descripcio;

    @Column(name = "preu",nullable = false, precision = 10, scale = 2)
    private BigDecimal preu;

    @Column(name = "fiança",nullable = false, precision = 10, scale = 2)
    private BigDecimal fiança;

    @Column(name = "estat", length = 30)
    private String estat;

    @Column(name = "actiu")
    private Boolean actiu;

    public vehiclesEntity() {
    }

    public vehiclesEntity(String matricula, String marca, String model, String descripcio, BigDecimal preu, BigDecimal fianca, String estat, Boolean actiu) {
        this.matricula = matricula;
        this.marca = marca;
        this.model = model;
        this.descripcio = descripcio;
        this.preu = preu;
        this.fiança = fiança;
        this.estat = estat;
        this.actiu = actiu;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDescripcio() {
        return descripcio;
    }

    public void setDescripcio(String descripcio) {
        this.descripcio = descripcio;
    }

    public BigDecimal getPreu() {
        return preu;
    }

    public void setPreu(BigDecimal preu) {
        this.preu = preu;
    }

    public BigDecimal getFiança() {
        return fiança;
    }

    public void setFiança(BigDecimal fianca) {
        this.fiança = fianca;
    }

    public String getEstat() {
        return estat;
    }

    public void setEstat(String estat) {
        this.estat = estat;
    }

    public Boolean getActiu() {
        return actiu;
    }

    public void setActiu(Boolean actiu) {
        this.actiu = actiu;
    }
};
