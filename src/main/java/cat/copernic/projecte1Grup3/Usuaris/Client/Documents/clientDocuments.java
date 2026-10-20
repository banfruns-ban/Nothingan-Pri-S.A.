/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Client.Documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "client_documents")
public class clientDocuments {

    @Id
    private String dni;                 // mismo DNI que en MySQL

    private byte[] fotoDniFrontal;
    private String tipusFotoDniFrontal;     // image/png o image/jpeg

    private byte[] fotoDniPosterior;
    private String tipusFotoDniPosterior;

    private byte[] fotoCarnet;
    private String tipusFotoCarnet;

    public clientDocuments() {}
    
    



    public clientDocuments(String dni, byte[] fotoDniFrontal, String tipusFotoDniFrontal, byte[] fotoDniPosterior, String tipusFotoDniPosterior, byte[] fotoCarnet, String tipusFotoCarnet) {
        this.dni = dni;
        this.fotoDniFrontal = fotoDniFrontal;
        this.tipusFotoDniFrontal = tipusFotoDniFrontal;
        this.fotoDniPosterior = fotoDniPosterior;
        this.tipusFotoDniPosterior = tipusFotoDniPosterior;
        this.fotoCarnet = fotoCarnet;
        this.tipusFotoCarnet = tipusFotoCarnet;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public byte[] getFotoDniFrontal() {
        return fotoDniFrontal;
    }

    public void setFotoDniFrontal(byte[] fotoDniFrontal) {
        this.fotoDniFrontal = fotoDniFrontal;
    }

    public String getTipusFotoDniFrontal() {
        return tipusFotoDniFrontal;
    }

    public void setTipusFotoDniFrontal(String tipusFotoDniFrontal) {
        this.tipusFotoDniFrontal = tipusFotoDniFrontal;
    }

    public byte[] getFotoDniPosterior() {
        return fotoDniPosterior;
    }

    public void setFotoDniPosterior(byte[] fotoDniPosterior) {
        this.fotoDniPosterior = fotoDniPosterior;
    }

    public String getTipusFotoDniPosterior() {
        return tipusFotoDniPosterior;
    }

    public void setTipusFotoDniPosterior(String tipusFotoDniPosterior) {
        this.tipusFotoDniPosterior = tipusFotoDniPosterior;
    }

    public byte[] getFotoCarnet() {
        return fotoCarnet;
    }

    public void setFotoCarnet(byte[] fotoCarnet) {
        this.fotoCarnet = fotoCarnet;
    }

    public String getTipusFotoCarnet() {
        return tipusFotoCarnet;
    }

    public void setTipusFotoCarnet(String tipusFotoCarnet) {
        this.tipusFotoCarnet = tipusFotoCarnet;
    }
    
    
}