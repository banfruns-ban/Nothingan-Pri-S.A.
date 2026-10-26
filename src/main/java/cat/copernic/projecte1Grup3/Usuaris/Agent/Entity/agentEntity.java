package cat.copernic.projecte1Grup3.Usuaris.Agent.Entity;

import cat.copernic.projecte1Grup3.Usuaris.Entity.usuariEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 *
 * @author Usuario
 */
@Entity
@Table(name = "agents")
public class agentEntity extends usuariEntity {

    @Column(nullable = false)
    private String hora_entrada;
    
    @Column(nullable = false)
    private String hora_sortida;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    // Getters y Setters
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

    public agentEntity() {
        super();
    }
    
    public agentEntity(String hora_entrada, String hora_sortida, Rol rol, String dni, String nom, String email) {
        super(dni, nom, email);
        this.hora_entrada = hora_entrada;
        this.hora_sortida = hora_sortida;
        this.rol = rol;
    }
    
}