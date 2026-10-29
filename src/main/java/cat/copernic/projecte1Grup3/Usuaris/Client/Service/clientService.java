/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Client.Service;
import cat.copernic.projecte1Grup3.Usuaris.Client.Documents.clientDocuments;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.clientRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.clientRepositoryMongo;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author User
 */
@Service
public class clientService {
    
 public static final String PENDENT = "PENDENT";
    public static final String VALIDAT = "VALIDAT";
    public static final String REBUTJAT = "REBUTJAT";

    private final clientRepository clientRepository;
    private final clientRepositoryMongo clientRepositoryMongo;

    public clientService(clientRepository clientRepository,
            clientRepositoryMongo clientRepositoryMongo) {
        this.clientRepository = clientRepository;
        this.clientRepositoryMongo = clientRepositoryMongo;
    }

    // ---------- Listar y buscar ----------
    public List<clientEntity> llistar(String q, String dni, String estat, String email,
            LocalDate dataNaixement) {
        // 1. Cerca general (nom, DNI/NIE, email o telèfon)
        List<clientEntity> clients = clientRepository.findAll();
        if (q != null && !q.isBlank()) {
            String text = q.trim().toLowerCase();
            clients = clients.stream()
                    .filter(c -> conte(c.getNom(), text) || conte(c.getDni(), text)
                            || conte(c.getEmail(), text) || conte(c.getTelefon(), text))
                    .toList();
        }

        // 2. Filtre per DNI / NIE
        if (dni != null && !dni.isBlank()) {
            String d = dni.trim().toUpperCase();
            clients = clients.stream()
                    .filter(c -> c.getDni() != null && c.getDni().toUpperCase().contains(d))
                    .toList();
        }

        // 3. Filtre per estat
        if (estat != null && !estat.isBlank()) {
            clients = clients.stream()
                    .filter(c -> estat.equals(c.getEstatValidacio()))
                    .toList();
        }

        // 4. Filtre per data de naixement exacta
        if (dataNaixement != null) {
            clients = clients.stream()
                    .filter(c -> dataNaixement.equals(c.getDataNaixement()))
                    .toList();
        }
        
        // 5. Filtre per email (parcial, sense distingir majúscules)
        if (email != null && !email.isBlank()) {
            String e = email.trim().toLowerCase();
            clients = clients.stream()
                    .filter(c -> conte(c.getEmail(), e))
                    .toList();
        }
        return clients;
    }

    private boolean conte(String valor, String text) {
        return valor != null && valor.toLowerCase().contains(text);
    }

    // ---------- Alta ----------
    @Transactional(rollbackFor = Exception.class)
    public void alta(clientEntity client, MultipartFile fotoDniFrontal,
            MultipartFile fotoDniPosterior, MultipartFile fotoCarnet) throws IOException {

        // El format del DNI/NIE es valida aquí (no a l'entitat) perquè els registres antics es puguin editar
        String dni = client.getDni() == null ? "" : client.getDni().trim().toUpperCase();
        if (!dni.matches("^([0-9]{8}|[XYZ][0-9]{7})[A-Z]$")) {
            throw new IllegalArgumentException(
                    "El DNI ha de tenir 8 xifres i una lletra al final (o ser un NIE vàlid)");
        }
        client.setDni(dni);

        if (clientRepository.existsById(dni)) {
            throw new IllegalArgumentException("Ja existeix un client amb aquest DNI");
        }
        if (client.getPassword() == null || client.getPassword().length() < 8) {
            throw new IllegalArgumentException("La contrasenya ha de tenir com a mínim 8 caràcters");
        }
        if (!hiHa(fotoDniFrontal) || !hiHa(fotoDniPosterior) || !hiHa(fotoCarnet)) {
            throw new IllegalArgumentException("Cal pujar les 3 imatges (PNG o JPG, màxim 5MB)");
        }
        validaImatge(fotoDniFrontal);
        validaImatge(fotoDniPosterior);
        validaImatge(fotoCarnet);

        client.setPremium(false);
        client.setEstatValidacio(PENDENT);
        clientRepository.saveAndFlush(client);

        clientDocuments doc = new clientDocuments();
        doc.setDni(dni);
        if (hiHa(fotoDniFrontal)) {
            doc.setFotoDniFrontal(fotoDniFrontal.getBytes());
            doc.setTipusFotoDniFrontal(fotoDniFrontal.getContentType());
        }
        if (hiHa(fotoDniPosterior)) {
            doc.setFotoDniPosterior(fotoDniPosterior.getBytes());
            doc.setTipusFotoDniPosterior(fotoDniPosterior.getContentType());
        }
        if (hiHa(fotoCarnet)) {
            doc.setFotoCarnet(fotoCarnet.getBytes());
            doc.setTipusFotoCarnet(fotoCarnet.getContentType());
        }
        clientRepositoryMongo.save(doc);
    }

    public Optional<clientEntity> obtenir(String dni) {
        return clientRepository.findById(dni);
    }

    public Optional<clientDocuments> obtenirDocuments(String dni) {
        return clientRepositoryMongo.findById(dni);
    }

    @Transactional(rollbackFor = Exception.class)
    public void actualitzar(String dni, clientEntity dades, String estat, boolean premium,
            MultipartFile fotoDniFrontal, MultipartFile fotoDniPosterior,
            MultipartFile fotoCarnet) throws IOException {

        clientEntity c = clientRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));

        if (!VALIDAT.equals(estat) && !REBUTJAT.equals(estat) && !PENDENT.equals(estat)) {
            throw new IllegalArgumentException("Estat no vàlid");
        }
        validaImatge(fotoDniFrontal);
        validaImatge(fotoDniPosterior);
        validaImatge(fotoCarnet);

        c.setNom(dades.getNom());
        c.setEmail(dades.getEmail());
        c.setTelefon(dades.getTelefon());
        c.setDataNaixement(dades.getDataNaixement());
        c.setNacionalitat(dades.getNacionalitat());
        c.setAdreca(dades.getAdreca());
        c.setTipusLlicencia(dades.getTipusLlicencia());
        c.setDataCaducitatDNI(dades.getDataCaducitatDNI());
        c.setEstatValidacio(estat);
        c.setPremium(premium);
        
        clientRepository.saveAndFlush(c);

        if (hiHa(fotoDniFrontal) || hiHa(fotoDniPosterior) || hiHa(fotoCarnet)) {
            clientDocuments doc = clientRepositoryMongo.findById(dni).orElseGet(() -> {
                clientDocuments nou = new clientDocuments();
                nou.setDni(dni);
                return nou;
            });
            if (hiHa(fotoDniFrontal)) {
                doc.setFotoDniFrontal(fotoDniFrontal.getBytes());
                doc.setTipusFotoDniFrontal(fotoDniFrontal.getContentType());
            }
            if (hiHa(fotoDniPosterior)) {
                doc.setFotoDniPosterior(fotoDniPosterior.getBytes());
                doc.setTipusFotoDniPosterior(fotoDniPosterior.getContentType());
            }
            if (hiHa(fotoCarnet)) {
                doc.setFotoCarnet(fotoCarnet.getBytes());
                doc.setTipusFotoCarnet(fotoCarnet.getContentType());
            }
            clientRepositoryMongo.save(doc);
            
        }
    }

    private boolean hiHa(MultipartFile f) {
        return f != null && !f.isEmpty();
    }

    private void validaImatge(MultipartFile f) {
        if (!hiHa(f)) {
            return;
        }
        String tipus = f.getContentType();
        boolean tipusValid = "image/png".equals(tipus) || "image/jpeg".equals(tipus);
        if (!tipusValid || f.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Les imatges han de ser PNG o JPG de màxim 5MB");
        }
    }
   
     @Transactional
    public void eliminar(String dni) {
    clientEntity c = clientRepository.findById(dni)
            .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));
    
    clientRepository.delete(c);
    clientRepository.flush();                  // si hay reservas asociadas, el error salta aquí

    clientRepositoryMongo.deleteById(dni);     // solo se llega aquí si MySQL ha ido bien
}
       
    
   

    @Transactional
    public void canviarEstat(String dni, String estat) {
        if (!VALIDAT.equals(estat) && !REBUTJAT.equals(estat) && !PENDENT.equals(estat)) {
            throw new IllegalArgumentException("Estat no vàlid");
        }
        clientEntity c = clientRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));
        c.setEstatValidacio(estat);
        clientRepository.save(c);
    }

    @Transactional
    public void canviarPremium(String dni, boolean premium) {
        clientEntity c = clientRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));
        c.setPremium(premium);
        clientRepository.save(c);
    }
}