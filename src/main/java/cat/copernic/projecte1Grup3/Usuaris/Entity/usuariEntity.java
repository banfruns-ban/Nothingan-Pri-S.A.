package cat.copernic.projecte1Grup3.Usuaris.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuaris")
@Inheritance(strategy = InheritanceType.JOINED)
public class usuariEntity {

    @Id
    @Column(nullable = false, length = 9, name = "DNI")
    private String dni;

    @Column(nullable = false, name = "nom")
    private String nom;

    @Column(nullable = false, unique = true, name = "email")
    private String email;

    // Constructor vacío
    public usuariEntity() {
    }

    // Constructor con todos los campos
    public usuariEntity(String dni, String nom, String email) {
        this.dni = dni;
        this.nom = nom;
        this.email = email;
    }

    // Getters y Setters
    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}