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
public class usuariEntity {
    
   @Id
@Column(name = "dni", nullable = false, unique = true, length = 9)
@NotBlank(message = "El DNI no pot estar buit")
@Pattern(
    regexp = "^[0-9]{8}[A-Za-z]$",
    message = "El DNI ha de tenir 8 xifres i una lletra al final"
)
private String dni;

@Column(nullable = false, length = 50)
@NotBlank(message = "El nom no pot estar buit")
@Size(min = 2, max = 50, message = "El nom ha de tenir entre 2 i 50 caràcters")
private String nom;

@Column(nullable = false, unique = true, length = 254)
@NotBlank(message = "El correu no pot estar buit")
@Email(message = "El format del correu electrònic no és vàlid")
private String email;

@Column(nullable = true, length = 255)
@Size(min = 8, message = "La contrasenya ha de tenir com a mínim 8 caràcters")
private String password;

    // Getters y Setters
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // Constructor buit
    public usuariEntity() {
    }

    // Constructor de 3 paràmetres (per a classes com clientEntity)
    public usuariEntity(String dni, String nom, String email) {
        this.dni = dni;
        this.nom = nom;
        this.email = email;
    }

    // Constructor de 4 paràmetres (per a classes amb contrasenya com agentEntity)
    public usuariEntity(String dni, String nom, String email, String password) {
        this.dni = dni;
        this.nom = nom;
        this.email = email;
        this.password = password;
    }
}