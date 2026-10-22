package cat.copernic.projecte1Grup3.Vehicles.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Entitat JPA que representa la taula 'vehicle' a la base de dades MySQL.
 * Configurada per coincidir amb els noms de les columnes de la BDD (incloent 'fiança').
 */
@Entity
@Table(name = "vehicle")
public class VehiclesEntity {

    @Id
    @Column(name = "matricula", nullable = false, length = 15)
    private String matricula;

    @Column(name = "marca", nullable = false, length = 50)
    private String marca;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @Column(name = "any_vehicle", nullable = false)
    private Integer anyMatriculacio;

    @Column(name = "descripcio", nullable = false, columnDefinition = "TEXT")
    private String descripcio;

    @Column(name = "tipus_vehicle", length = 30)
    private String tipusVehicle;

    @Column(name = "limit_quilometratge")
    private Integer limitQuilometratge;

    @Column(name = "preu", nullable = false, precision = 10, scale = 2)
    private BigDecimal preuHora;

    // Columna real a la BDD de MySQL: 'fiança' (amb 'ç')
    @Column(name = "fiança", nullable = false, precision = 10, scale = 2)
    private BigDecimal fianca;

    @Column(name = "min_dies_lloguer", nullable = false)
    private Integer minDiesLloguer;

    @Column(name = "max_dies_lloguer", nullable = false)
    private Integer maxDiesLloguer;

    @Column(name = "estat", length = 30)
    private String estat;

    @Column(name = "actiu", nullable = false)
    private Boolean actiu;

    @Column(name = "comentaris_privats", columnDefinition = "TEXT")
    private String comentarisPrivats;

    /**
     * Constructor per defecte requerit per JPA.
     */
    public VehiclesEntity() {
    }

    /**
     * Constructor complet.
     */
    public VehiclesEntity(String matricula, String marca, String model, Integer anyMatriculacio, 
                          String descripcio, String tipusVehicle, Integer limitQuilometratge, 
                          BigDecimal preuHora, BigDecimal fianca, Integer minDiesLloguer, 
                          Integer maxDiesLloguer, String estat, Boolean actiu, String comentarisPrivats) {
        this.matricula = matricula;
        this.marca = marca;
        this.model = model;
        this.anyMatriculacio = anyMatriculacio;
        this.descripcio = descripcio;
        this.tipusVehicle = tipusVehicle;
        this.limitQuilometratge = limitQuilometratge;
        this.preuHora = preuHora;
        this.fianca = fianca;
        this.minDiesLloguer = minDiesLloguer;
        this.maxDiesLloguer = maxDiesLloguer;
        this.estat = estat;
        this.actiu = actiu;
        this.comentarisPrivats = comentarisPrivats;
    }

    // --- GETTERS I SETTERS ---

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

    public Integer getAnyMatriculacio() {
        return anyMatriculacio;
    }

    public void setAnyMatriculacio(Integer anyMatriculacio) {
        this.anyMatriculacio = anyMatriculacio;
    }

    public String getDescripcio() {
        return descripcio;
    }

    public void setDescripcio(String descripcio) {
        this.descripcio = descripcio;
    }

    public String getTipusVehicle() {
        return tipusVehicle;
    }

    public void setTipusVehicle(String tipusVehicle) {
        this.tipusVehicle = tipusVehicle;
    }

    public Integer getLimitQuilometratge() {
        return limitQuilometratge;
    }

    public void setLimitQuilometratge(Integer limitQuilometratge) {
        this.limitQuilometratge = limitQuilometratge;
    }

    public BigDecimal getPreuHora() {
        return preuHora;
    }

    public void setPreuHora(BigDecimal preuHora) {
        this.preuHora = preuHora;
    }

    public BigDecimal getFianca() {
        return fianca;
    }

    public void setFianca(BigDecimal fianca) {
        this.fianca = fianca;
    }

    public Integer getMinDiesLloguer() {
        return minDiesLloguer;
    }

    public void setMinDiesLloguer(Integer minDiesLloguer) {
        this.minDiesLloguer = minDiesLloguer;
    }

    public Integer getMaxDiesLloguer() {
        return maxDiesLloguer;
    }

    public void setMaxDiesLloguer(Integer maxDiesLloguer) {
        this.maxDiesLloguer = maxDiesLloguer;
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

    public String getComentarisPrivats() {
        return comentarisPrivats;
    }

    public void setComentarisPrivats(String comentarisPrivats) {
        this.comentarisPrivats = comentarisPrivats;
    }
}