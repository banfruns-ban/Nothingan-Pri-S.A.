package cat.copernic.projecte1Grup3.Usuaris.Client.Entity;

import cat.copernic.projecte1Grup3.Usuaris.Entity.usuariEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity // Marca la clase como entidad persistente de JPA
@Table(name = "Clients") // Asocia esta entidad con la tabla "Clients" en la base de datos MySQL
@PrimaryKeyJoinColumn(
    name = "DNI",
    referencedColumnName = "dni"
) // Mapeo de herencia: comparte la clave primaria 'dni' con la entidad padre usuariEntity
public class clientEntity extends usuariEntity {

    @Column(name = "data_caducitat_DNI", nullable = false)
    private String dataCaducitatDNI;

    @Column(name = "nacionalitat", nullable = false)
    private String nacionalitat;

    @Column(name = "adreca", nullable = false)
    private String adreca;

    @Column(name = "tipus_llicencia", nullable = false)
    private String tipusLlicencia;

    @Column(name = "premium", nullable = false)
    private boolean premium;

    @Column(name = "estat_validacio", nullable = false)
    private String estatValidacio; // Clave para el RF82 (PENDENT, VALIDAT, REBUTJAT)

    // Campos opcionales (sin nullable = false) para compatibilidad con registros preexistentes
    @Column(name = "telefon")
    private String telefon;

    @Column(name = "data_naixement")
    private LocalDate dataNaixement;

    @Column(name = "data_registre")
    private LocalDate dataRegistre;

    // Se ejecuta automáticamente antes de persistir un nuevo registro en la BD
    @PrePersist
    void alCrear() {
        if (dataRegistre == null) {
            dataRegistre = LocalDate.now();
        }
    }

    // Constructor sin argumentos por defecto para JPA
    public clientEntity() {
        super();
    }

    // Constructor completo que inicializa también la superclase (usuariEntity)
    public clientEntity(String dataCaducitatDNI,
                        String nacionalitat,
                        String adreca,
                        String tipusLlicencia,
                        boolean premium,
                        String estatValidacio,
                        String dni,
                        String nom,
                        String email) {

        super(dni, nom, email);

        this.dataCaducitatDNI = dataCaducitatDNI;
        this.nacionalitat = nacionalitat;
        this.adreca = adreca;
        this.tipusLlicencia = tipusLlicencia;
        this.premium = premium;
        this.estatValidacio = estatValidacio;
    }

    // Getters y Setters
    public String getDataCaducitatDNI() { return dataCaducitatDNI; }
    public void setDataCaducitatDNI(String dataCaducitatDNI) { this.dataCaducitatDNI = dataCaducitatDNI; }

    public String getNacionalitat() { return nacionalitat; }
    public void setNacionalitat(String nacionalitat) { this.nacionalitat = nacionalitat; }

    public String getAdreca() { return adreca; }
    public void setAdreca(String adreca) { this.adreca = adreca; }

    public String getTipusLlicencia() { return tipusLlicencia; }
    public void setTipusLlicencia(String tipusLlicencia) { this.tipusLlicencia = tipusLlicencia; }

    public boolean isPremium() { return premium; }
    public void setPremium(boolean premium) { this.premium = premium; }

    public String getEstatValidacio() { return estatValidacio; }
    public void setEstatValidacio(String estatValidacio) { this.estatValidacio = estatValidacio; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public LocalDate getDataNaixement() { return dataNaixement; }
    public void setDataNaixement(LocalDate dataNaixement) { this.dataNaixement = dataNaixement; }

    public LocalDate getDataRegistre() { return dataRegistre; }
    public void setDataRegistre(LocalDate dataRegistre) { this.dataRegistre = dataRegistre; }
}