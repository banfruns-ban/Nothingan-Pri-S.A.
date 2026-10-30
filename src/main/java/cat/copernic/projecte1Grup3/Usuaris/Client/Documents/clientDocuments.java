package cat.copernic.projecte1Grup3.Usuaris.Client.Documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// Define que esta clase se guarda como un documento dentro de la colección 'client_documents' en MongoDB
@Document(collection = "client_documents")
public class clientDocuments {

    @Id
    private String dni; // Mismo DNI de la base de datos relacional (MySQL) para vincular los datos

    private byte[] fotoDniFrontal;        // Array de bytes con el contenido de la foto frontal del DNI
    private String tipusFotoDniFrontal;     // Tipo MIME (image/png, image/jpeg) para la cabecera HTTP

    private byte[] fotoDniPosterior;      // Array de bytes con el contenido de la foto trasera del DNI
    private String tipusFotoDniPosterior;   // Tipo MIME de la foto trasera

    private byte[] fotoCarnet;            // Array de bytes con la imagen del carnet de conducir
    private String tipusFotoCarnet;         // Tipo MIME del carnet de conducir

    // Constructor vacío por defecto
    public clientDocuments() {}

    // Constructor parametrizado completo
    public clientDocuments(String dni, byte[] fotoDniFrontal, String tipusFotoDniFrontal, 
                           byte[] fotoDniPosterior, String tipusFotoDniPosterior, 
                           byte[] fotoCarnet, String tipusFotoCarnet) {
        this.dni = dni;
        this.fotoDniFrontal = fotoDniFrontal;
        this.tipusFotoDniFrontal = tipusFotoDniFrontal;
        this.fotoDniPosterior = fotoDniPosterior;
        this.tipusFotoDniPosterior = tipusFotoDniPosterior;
        this.fotoCarnet = fotoCarnet;
        this.tipusFotoCarnet = tipusFotoCarnet;
    }

    // Getters y Setters
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