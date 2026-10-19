
package cat.copernic.projecte1Grup3.Usuaris.Client.Entity;

import cat.copernic.projecte1Grup3.Ressenya.Entity.ressenyaEntity;
import cat.copernic.projecte1Grup3.Usuaris.Entity.usuariEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "tipus_llicencia" ,nullable = false)
    private String tipusLlicencia;

    @Column(name = "premium", nullable = false)
    private boolean premium;

    @Column(name = "estat_validacio", nullable = false)
    private String estatValidacio;

    
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
                        String correu) {

        super(dni, nom, correu);

        this.dataCaducitatDNI = dataCaducitatDNI;
        this.nacionalitat = nacionalitat;
        this.adreca = adreca;
        this.tipusLlicencia = tipusLlicencia;
        this.premium = premium;
        this.estatValidacio = estatValidacio;
    }

    // Getters y setters
    public String getDataCaducitatDNI() {
        return dataCaducitatDNI;
    }

    public void setDataCaducitatDNI(String dataCaducitatDNI) {
        this.dataCaducitatDNI = dataCaducitatDNI;
    }

    public String getNacionalitat() {
        return nacionalitat;
    }

    public void setNacionalitat(String nacionalitat) {
        this.nacionalitat = nacionalitat;
    }

    public String getAdreca() {
        return adreca;
    }

    public void setAdreca(String adreca) {
        this.adreca = adreca;
    }

    public String getTipusLlicencia() {
        return tipusLlicencia;
    }

    public void setTipusLlicencia(String tipusLlicencia) {
        this.tipusLlicencia = tipusLlicencia;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public String getEstatValidacio() {
        return estatValidacio;
    }

    public void setEstatValidacio(String estatValidacio) {
        this.estatValidacio = estatValidacio;
    }
    
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ressenyaEntity> ressenyes = new ArrayList<>();

    public List<ressenyaEntity> getRessenyes() { return ressenyes; }
    public void setRessenyes(List<ressenyaEntity> ressenyes) { this.ressenyes = ressenyes; }

    public void addRessenya(ressenyaEntity r) {
        ressenyes.add(r);
        r.setClient(this);
}
}
