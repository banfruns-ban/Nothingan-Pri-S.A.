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

@Service
public class clientService {

    public static final String PENDENT = "PENDENT";
    public static final String VALIDAT = "VALIDAT";
    public static final String REBUTJAT = "REBUTJAT";

    private static final long MAX_IMATGE = 5L * 1024 * 1024;

    private final clientRepository clientRepository;
    private final clientRepositoryMongo clientRepositoryMongo;

    public clientService(clientRepository clientRepository,
                         clientRepositoryMongo clientRepositoryMongo) {
        this.clientRepository = clientRepository;
        this.clientRepositoryMongo = clientRepositoryMongo;
    }

    // =====================================================
    // ALTA
    // =====================================================

    @Transactional(rollbackFor = Exception.class)
    public void alta(clientEntity client, MultipartFile fotoDniFrontal,
                     MultipartFile fotoDniPosterior, MultipartFile fotoCarnet) throws IOException {

        if (client.getDni() == null || client.getDni().isBlank()) {
            throw new IllegalArgumentException("El DNI és obligatori");
        }
        client.setDni(client.getDni().trim().toUpperCase());

        if (clientRepository.existsById(client.getDni())) {
            throw new IllegalArgumentException("Ja existeix un client amb aquest DNI");
        }

        // En l'alta les 3 imatges són obligatòries
        if (!hiHa(fotoDniFrontal) || !hiHa(fotoDniPosterior) || !hiHa(fotoCarnet)) {
            throw new IllegalArgumentException("Cal pujar les 3 imatges (PNG o JPG, màxim 5MB)");
        }
        validaImatge(fotoDniFrontal);
        validaImatge(fotoDniPosterior);
        validaImatge(fotoCarnet);

        // Valors que decideix el servidor, no el formulari
        client.setPremium(false);
        client.setEstatValidacio(PENDENT);

        // Es llegeixen els bytes abans de guardar res: si falla, no hi ha res a desfer
        clientDocuments doc = new clientDocuments();
        doc.setDni(client.getDni());
        doc.setFotoDniFrontal(fotoDniFrontal.getBytes());
        doc.setTipusFotoDniFrontal(fotoDniFrontal.getContentType());
        doc.setFotoDniPosterior(fotoDniPosterior.getBytes());
        doc.setTipusFotoDniPosterior(fotoDniPosterior.getContentType());
        doc.setFotoCarnet(fotoCarnet.getBytes());
        doc.setTipusFotoCarnet(fotoCarnet.getContentType());

        clientRepository.saveAndFlush(client);   // MySQL
        clientRepositoryMongo.save(doc);         // MongoDB
    }

    // =====================================================
    // LISTAR Y BUSCAR
    // =====================================================

    public List<clientEntity> llistar(String q, String dni, String estat,
                                      LocalDate dataNaixement) {
        List<clientEntity> clients = clientRepository.findAll();

        if (q != null && !q.isBlank()) {
            String text = q.trim().toLowerCase();
            clients = clients.stream()
                    .filter(c -> conte(c.getNom(), text) || conte(c.getDni(), text)
                            || conte(c.getEmail(), text) || conte(c.getTelefon(), text))
                    .toList();
        }

        if (dni != null && !dni.isBlank()) {
            String d = dni.trim().toUpperCase();
            clients = clients.stream()
                    .filter(c -> c.getDni() != null && c.getDni().toUpperCase().contains(d))
                    .toList();
        }

        if (estat != null && !estat.isBlank()) {
            clients = clients.stream()
                    .filter(c -> estat.equals(c.getEstatValidacio()))
                    .toList();
        }

        if (dataNaixement != null) {
            clients = clients.stream()
                    .filter(c -> dataNaixement.equals(c.getDataNaixement()))
                    .toList();
        }
        return clients;
    }

    public Optional<clientEntity> obtenir(String dni) {
        return clientRepository.findById(dni);
    }

    public Optional<clientDocuments> obtenirDocuments(String dni) {
        return clientRepositoryMongo.findById(dni);
    }

    // =====================================================
    // EDICIÓN
    // =====================================================

    @Transactional(rollbackFor = Exception.class) //<-- si algo sale mal, vuelve atras
    public void actualitzar(String dni, clientEntity dades, String estat, boolean premium,
                            MultipartFile fotoDniFrontal, MultipartFile fotoDniPosterior,
                            MultipartFile fotoCarnet) throws IOException {

        clientEntity c = clientRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));

        validaEstat(estat);
        validaImatge(fotoDniFrontal);
        validaImatge(fotoDniPosterior);
        validaImatge(fotoCarnet);

        c.setNom(dades.getNom());//Cada línea es borrar un dato viejo y escribir el nuevo:
        c.setEmail(dades.getEmail());
        c.setTelefon(dades.getTelefon());
        c.setDataNaixement(dades.getDataNaixement());
        c.setNacionalitat(dades.getNacionalitat());
        c.setAdreca(dades.getAdreca());
        c.setTipusLlicencia(dades.getTipusLlicencia());
        c.setDataCaducitatDNI(dades.getDataCaducitatDNI());
        c.setEstatValidacio(estat);
        c.setPremium(premium);
        clientRepository.saveAndFlush(c); //<-lo hace ya

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

    // =====================================================
    // ELIMINAR, ESTADO Y PREMIUM
    // =====================================================

    @Transactional
    public void eliminar(String dni) {
        clientRepository.deleteById(dni);
        clientRepositoryMongo.deleteById(dni);
    }

    @Transactional
    public void canviarEstat(String dni, String estat) {
        validaEstat(estat);
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

    // =====================================================
    // AUXILIARES
    // =====================================================

    private boolean conte(String valor, String text) {
        return valor != null && valor.toLowerCase().contains(text);
    }

    private boolean hiHa(MultipartFile f) {//<--un archivo subido por el usuario desde un formulario
        return f != null && !f.isEmpty();
    }

    private void validaEstat(String estat) {
        if (!VALIDAT.equals(estat) && !REBUTJAT.equals(estat) && !PENDENT.equals(estat)) {
            throw new IllegalArgumentException("Estat no vàlid");
        }
    }

    // Si no hi ha fitxer, no valida res (a l'edició les imatges són opcionals)
    private void validaImatge(MultipartFile f) {
        if (!hiHa(f)) {
            return;
        }
        String tipus = f.getContentType();
        boolean tipusValid = "image/png".equals(tipus) || "image/jpeg".equals(tipus);
        if (!tipusValid || f.getSize() > MAX_IMATGE) { //<-- si la imagen no es valida o supera el tamaño
            throw new IllegalArgumentException("Les imatges han de ser PNG o JPG de màxim 5MB");
        }
    }
}