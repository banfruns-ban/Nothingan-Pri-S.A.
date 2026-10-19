/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Ressenya.Entity;

import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;


@Entity
@Table(name = "Ressenya")
public class ressenyaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ressenya")
    private Long idRessenya;

    
    @Column(name = "puntuacio", nullable = false)
    private int puntuacio;

    @Column(name = "comentari")
    private String comentari;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DNI_client", nullable = false)
    private clientEntity client;

    public ressenyaEntity() {
    }

    public ressenyaEntity(int puntuacio, String comentari, LocalDate data, clientEntity client) {
        this.puntuacio = puntuacio;
        this.comentari = comentari;
        this.data = data;
        this.client = client;
    }

    public Long getIdRessenya() { return idRessenya; }
    public void setIdRessenya(Long idRessenya) { this.idRessenya = idRessenya; }

    public int getPuntuacio() { return puntuacio; }
    public void setPuntuacio(int puntuacio) { this.puntuacio = puntuacio; }

    public String getComentari() { return comentari; }
    public void setComentari(String comentari) { this.comentari = comentari; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public clientEntity getClient() { return client; }
    public void setClient(clientEntity client) { this.client = client; }
}
