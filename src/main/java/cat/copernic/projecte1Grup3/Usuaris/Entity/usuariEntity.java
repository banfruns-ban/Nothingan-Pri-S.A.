/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Entity;

import jakarta.persistence.*;


/**
 *
 * @author Usuario
 */
@Entity
@Table(name = "usuaris")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class usuariEntity {

    @Id
    @Column(nullable = false, length = 9, name = "DNI" )
    private String dni;

    @Column(nullable = false,name = "nom"  )
    private String nom;

    @Column(nullable = false, unique = true, name="Correu")
    private String correu;

    // Constructor vacío
    public usuariEntity() {
    }

    // Constructor con todos los campos
    public usuariEntity(String dni, String nom, String correu) {
        this.dni = dni;
        this.nom = nom;
        this.correu = correu;
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

    public String getCorreu() {
        return correu;
    }

    public void setCorreu(String correu) {
        this.correu = correu;
    }
}
