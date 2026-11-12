package cat.copernic.projecte1Grup3.Usuaris.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Entitat pare per als usuaris del sistema.
 */
@Entity
@Table(name = "usuaris")
@Inheritance(strategy = InheritanceType.JOINED)
public class UsuariEntity {

    // El format del DNI/NIE es valida a clientService.alta(...), no aquí,
    // perquè els registres antics es puguin editar sense errors.
    @Id
    @Column(name = "dni", nullable = false, unique = true, length = 9)
    @NotBlank(message = "El DNI no pot estar buit")
    private String dni;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "El nom no pot estar buit")
    @Size(min = 2, max = 50, message = "El nom ha de tenir entre 2 i 50 caràcters")
    private String nom;

    @Column(nullable = false, unique = true, length = 254)
    @NotBlank(message = "El correu no pot estar buit")
    @Email(message = "El format del correu electrònic no és vàlid")
    private String email;

    // Opcional (els clients no en tenen). Si n'hi ha, mínim 8 caràcters.
    // El regex accepta null i cadena buida; només falla si hi ha 1-7 caràcters.
    @Column(nullable = true, length = 255)
    @Pattern(
            regexp = "^$|^.{8,}$",
            message = "La contrasenya ha de tenir com a mínim 8 caràcters"
    )
    private String password;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UsuariEntity() {
    }

    public UsuariEntity(String dni, String nom, String email) {
        this.dni = dni;
        this.nom = nom;
        this.email = email;
    }

    public UsuariEntity(String dni, String nom, String email, String password) {
        this.dni = dni;
        this.nom = nom;
        this.email = email;
        this.password = password;
    }
}
