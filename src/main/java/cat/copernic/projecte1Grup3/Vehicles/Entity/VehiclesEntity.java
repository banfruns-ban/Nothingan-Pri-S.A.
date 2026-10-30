package cat.copernic.projecte1Grup3.Vehicles.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Table(name = "vehicle")
public class VehiclesEntity {

    @Id
    @NotBlank(message = "La matrícula és obligatòria")
    @Pattern(regexp = "^[0-9]{4}[A-Z]{3}$|^[A-Z]{1,2}[0-9]{4}[A-Z]{1,2}$", message = "Format de matrícula invàlid (Ex: 1234ABC)")
    @Column(name = "matricula", nullable = false, length = 15)
    private String matricula;

    @NotBlank(message = "La marca és obligatòria")
    @Size(max = 50, message = "La marca no pot superar els 50 caràcters")
    @Column(name = "marca", nullable = false, length = 50)
    private String marca;

    @NotBlank(message = "El model és obligatori")
    @Size(max = 50, message = "El model no pot superar els 50 caràcters")
    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @NotNull(message = "L'any de matriculació és obligatori")
    @Min(value = 1900, message = "L'any ha de ser posterior a 1900")
    @Max(value = 2100, message = "L'any no és vàlid")
    @Column(name = "any_vehicle", nullable = false)
    private Integer anyMatriculacio;

    @NotBlank(message = "La descripció és obligatòria")
    @Column(name = "descripcio", nullable = false, columnDefinition = "TEXT")
    private String descripcio;

    @NotBlank(message = "Selecciona un tipus de vehicle")
    @Column(name = "tipus_vehicle", length = 30)
    private String tipusVehicle;

    @PositiveOrZero(message = "El límit de quilometratge ha de ser positiu o zero")
    @Column(name = "limit_quilometratge")
    private Integer limitQuilometratge;

    @NotNull(message = "El preu per hora és obligatori")
    @DecimalMin(value = "0.01", message = "El preu ha de ser superior a 0")
    @Column(name = "preu", nullable = false, precision = 10, scale = 2)
    private BigDecimal preuHora;

    @NotNull(message = "La fiança és obligatòria")
    @DecimalMin(value = "0.00", message = "La fiança no pot ser negativa")
    @Column(name = "fiança", nullable = false, precision = 10, scale = 2)
    private BigDecimal fianca;

    @NotNull(message = "El mínim de dies és obligatori")
    @Min(value = 1, message = "El mínim ha de ser com a mínim 1 dia")
    @Column(name = "min_dies_lloguer", nullable = false)
    private Integer minDiesLloguer;

    @NotNull(message = "El màxim de dies és obligatori")
    @Min(value = 1, message = "El màxim ha de ser com a mínim 1 dia")
    @Column(name = "max_dies_lloguer", nullable = false)
    private Integer maxDiesLloguer;

    @NotBlank(message = "L'estat operatiu és obligatori")
    @Column(name = "estat", length = 30)
    private String estat;

    @Column(name = "actiu", nullable = false)
    private Boolean actiu;

    @Column(name = "comentaris_privats", columnDefinition = "TEXT")
    private String comentarisPrivats;

    @Column(name = "foto", length = 255)
    private String foto;

    /**
     * Constructor per defecte requerit per JPA.
     */
    public VehiclesEntity() {
    }

    /**
     * Constructor complet sense foto.
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

    /**
     * Constructor complet amb foto.
     */
    public VehiclesEntity(String matricula, String marca, String model, Integer anyMatriculacio, 
                          String descripcio, String tipusVehicle, Integer limitQuilometratge, 
                          BigDecimal preuHora, BigDecimal fianca, Integer minDiesLloguer, 
                          Integer maxDiesLloguer, String estat, Boolean actiu, String comentarisPrivats, String foto) {
        this(matricula, marca, model, anyMatriculacio, descripcio, tipusVehicle, limitQuilometratge, 
             preuHora, fianca, minDiesLloguer, maxDiesLloguer, estat, actiu, comentarisPrivats);
        this.foto = foto;
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

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
}