package cat.copernic.projecte1Grup3.Usuaris.Client.Entity;

import cat.copernic.projecte1Grup3.Usuaris.Entity.usuariEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "Clients")
@PrimaryKeyJoinColumn(name = "DNI")
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
    private String estatValidacio;

    // ---- Camps nous (sense nullable = false perquè els clients antics no els tenen) ----
    @Column(name = "telefon")
    private String telefon;

    @Column(name = "data_naixement")
    private LocalDate dataNaixement;

    @Column(name = "data_registre")
    private LocalDate dataRegistre;

    // La data de registre s'omple sola en guardar un client nou
    @PrePersist
    void alCrear() {
        if (dataRegistre == null) {
            dataRegistre = LocalDate.now();
        }
    }

    public clientEntity() {
        super();
    }

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

    // ---------- Getters i setters ----------
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