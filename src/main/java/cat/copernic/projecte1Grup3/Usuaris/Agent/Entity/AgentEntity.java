package cat.copernic.projecte1Grup3.Usuaris.Agent.Entity;

import cat.copernic.projecte1Grup3.Usuaris.Entity.UsuariEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "agents")
public class AgentEntity extends UsuariEntity {

    @Column(nullable = false)
    private String hora_entrada;

    @Column(nullable = false)
    private String hora_sortida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(nullable = true)
    private String foto;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "data_naixement", nullable = true)
    private LocalDate dataNaixement;

    // ===== GETTERS Y SETTERS =====
    public String getHora_entrada() {
        return hora_entrada;
    }

    public void setHora_entrada(String hora_entrada) {
        this.hora_entrada = hora_entrada;
    }

    public String getHora_sortida() {
        return hora_sortida;
    }

    public void setHora_sortida(String hora_sortida) {
        this.hora_sortida = hora_sortida;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public LocalDate getDataNaixement() {
        return dataNaixement;
    }

    public void setDataNaixement(LocalDate dataNaixement) {
        this.dataNaixement = dataNaixement;
    }

    // ===== CONSTRUCTORES =====
    public AgentEntity() {
        super();
    }

    public AgentEntity(String hora_entrada, String hora_sortida, Rol rol, String dni, String nom, String email, String password) {
        super(dni, nom, email, password);
        this.hora_entrada = hora_entrada;
        this.hora_sortida = hora_sortida;
        this.rol = rol;
    }

    public AgentEntity(String hora_entrada, String hora_sortida, Rol rol, LocalDate dataNaixement, String dni, String nom, String email, String password) {
        super(dni, nom, email, password);
        this.hora_entrada = hora_entrada;
        this.hora_sortida = hora_sortida;
        this.rol = rol;
        this.dataNaixement = dataNaixement;
    }
}