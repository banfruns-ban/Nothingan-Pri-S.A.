package cat.copernic.projecte1Grup3.Usuaris.Client.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cat.copernic.projecte1Grup3.Usuaris.Client.Documents.clientDocuments;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.clientRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.clientRepositoryMongo;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service // Marca la classe com a Capa de Servei (Lògica de Negoci)
public class clientService {

    private static final Logger log = LoggerFactory.getLogger(clientService.class);
    // Constants d'estat per al compliment del RF82
    public static final String PENDENT = "PENDENT";
    public static final String VALIDAT = "VALIDAT";
    public static final String REBUTJAT = "REBUTJAT";

    private final clientRepository clientRepository;
    private final clientRepositoryMongo clientRepositoryMongo;
    private final PasswordEncoder passwordEncoder;

    // Indicem a Spring quin constructor ha d'utilitzar per a la injecció de dependències
    @Autowired
    public clientService(clientRepository clientRepository,
                         clientRepositoryMongo clientRepositoryMongo) {
        this(clientRepository, clientRepositoryMongo, new BCryptPasswordEncoder());
    }

    public clientService(clientRepository clientRepository,
                         clientRepositoryMongo clientRepositoryMongo,
                         PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.clientRepositoryMongo = clientRepositoryMongo;
        this.passwordEncoder = passwordEncoder != null ? passwordEncoder : new BCryptPasswordEncoder();
    }

    // ---------- LLISTAR I FILTRAR ----------
    public List<clientEntity> llistar(String q, String dni, String estat, String email,
                                      LocalDate dataNaixement) {
        // 1. Cerca general inicial a MySQL
        List<clientEntity> clients = clientRepository.findAll();

        // 2. Filtre general per text (nom, DNI/NIE, email o telèfon)
        if (q != null && !q.isBlank()) {
            String text = q.trim().toLowerCase();
            clients = clients.stream()
                    .filter(c -> conte(c.getNom(), text) || conte(c.getDni(), text)
                            || conte(c.getEmail(), text) || conte(c.getTelefon(), text))
                    .toList();
        }

        // 3. Filtre específic per DNI / NIE
        if (dni != null && !dni.isBlank()) {
            String d = dni.trim().toUpperCase();
            clients = clients.stream()
                    .filter(c -> c.getDni() != null && c.getDni().toUpperCase().contains(d))
                    .toList();
        }

        // 4. Filtre per estat de validació (PENDENT, VALIDAT, REBUTJAT)
        if (estat != null && !estat.isBlank()) {
            clients = clients.stream()
                    .filter(c -> estat.equals(c.getEstatValidacio()))
                    .toList();
        }

        // 5. Filtre per data de naixement exacta
        if (dataNaixement != null) {
            clients = clients.stream()
                    .filter(c -> dataNaixement.equals(c.getDataNaixement()))
                    .toList();
        }

        // 6. Filtre per correu electrònic
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

    // ---------- ALTA DE CLIENT (Doble Persistència) ----------
    @Transactional(rollbackFor = Exception.class) // Assegura que es guardi en totes dues BD o es cancel·li tot si hi ha un error
    public void alta(clientEntity client, MultipartFile fotoDniFrontal,
                     MultipartFile fotoDniPosterior, MultipartFile fotoCarnet) throws IOException {

        // Validació del format oficial de DNI/NIE amb Expressió Regular
        String dni = client.getDni() == null ? "" : client.getDni().trim().toUpperCase();
        if (!dni.matches("^([0-9]{8}|[XYZ][0-9]{7})[A-Z]$")) {
            throw new IllegalArgumentException(
                    "El DNI ha de tenir 8 xifres i una lletra al final (o ser un NIE vàlid)");
        }
        client.setDni(dni);

        if (clientRepository.existsById(dni)) {
            throw new IllegalArgumentException("Ja existeix un client amb aquest DNI");
        }
        String password = client.getPassword();
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La contrasenya ha de tenir com a mínim 8 caràcters");
        }
        client.setPassword(passwordEncoder.encode(password));
        if (!hiHa(fotoDniFrontal) || !hiHa(fotoDniPosterior) || !hiHa(fotoCarnet)) {
            throw new IllegalArgumentException("Cal pujar les 3 imatges (PNG o JPG, màxim 5MB)");
        }
        
        if (client.getDataCaducitatCarnet() == null || client.getDataCaducitatCarnet().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("El carnet de conduir no pot estar caducat");
        }
        
        validaImatge(fotoDniFrontal);
        validaImatge(fotoDniPosterior);
        validaImatge(fotoCarnet);

        // RF82: Tot registre extern neix en estat PENDENT per defecte
        client.setPremium(false);
        client.setEstatValidacio(PENDENT);
        clientRepository.saveAndFlush(client); // Guarda a MySQL

        // Guarda les 3 imatges en format binari dins de MongoDB
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
        clientRepositoryMongo.save(doc); // Guarda a MongoDB
        log.info("Client donat d'alta: {}", dni);
    }

    public Optional<clientEntity> obtenir(String dni) {
        return clientRepository.findById(dni);
    }

    public Optional<clientDocuments> obtenirDocuments(String dni) {
        return clientRepositoryMongo.findById(dni);
    }

    // ---------- ACTUALITZACIÓ ----------
    @Transactional(rollbackFor = Exception.class)
    public void actualitzar(String dni, clientEntity dades, String estat, boolean premium,
                            MultipartFile fotoDniFrontal, MultipartFile fotoDniPosterior,
                            MultipartFile fotoCarnet) throws IOException {

        clientEntity c = clientRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));

        if (!VALIDAT.equals(estat) && !REBUTJAT.equals(estat) && !PENDENT.equals(estat)) {
            throw new IllegalArgumentException("Estat no vàlid");
        }

        // El correu només és duplicat si el té UN ALTRE client (DNI diferent)
        String email = dades.getEmail() == null ? "" : dades.getEmail().trim();
        if (!email.isEmpty() && clientRepository.existsByEmailIgnoreCaseAndDniNot(email, dni)) {
            throw new IllegalArgumentException("Aquest correu ja està en ús per un altre client");
        }

        validaImatge(fotoDniFrontal);
        validaImatge(fotoDniPosterior);
        validaImatge(fotoCarnet);

        c.setNom(dades.getNom());
        c.setEmail(email);
        if (dades.getPassword() != null && !dades.getPassword().isBlank()) {
            c.setPassword(passwordEncoder.encode(dades.getPassword()));
        }
        c.setTelefon(dades.getTelefon());
        c.setNacionalitat(dades.getNacionalitat());
        c.setAdreca(dades.getAdreca());
        c.setTipusLlicencia(dades.getTipusLlicencia());

        // Només se sobreescriuen les dates si el formulari les envia (evita pisar amb null)
        if (dades.getDataNaixement() != null) {
            c.setDataNaixement(dades.getDataNaixement());
        }
        if (dades.getDataCaducitatDNI() != null) {
            c.setDataCaducitatDNI(dades.getDataCaducitatDNI());
        }
        if (dades.getDataCaducitatCarnet() != null) {
            c.setDataCaducitatCarnet(dades.getDataCaducitatCarnet());
        }

        c.setEstatValidacio(estat);
        c.setPremium(premium);

        clientRepository.saveAndFlush(c);
        log.info("Client actualitzat: {}", dni);

        // Actualitza els fitxers a MongoDB només si s'han adjuntat imatges meves
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

    // Comprova format (PNG/JPEG) i restringeix la mida a 5MB màx.
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

    // ---------- ELIMINACIÓ DE CLIENT ----------
    @Transactional
    public void eliminar(String dni) {
        clientEntity c = clientRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));

        clientRepository.delete(c);
        clientRepository.flush(); // Si el client té reserves associades, salta l'excepció aquí i cancel·la l'esborrat

        clientRepositoryMongo.deleteById(dni); // Elimina de MongoDB només després de confirmar l'èxit a MySQL
        log.warn("Client eliminat: {}", dni);
    }

    // ---------- CANVI D'ESTAT DE VALIDACIÓ (RF82) ----------
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

    // ---------- CANVI D'ESTAT PREMIUM ----------
    @Transactional
    public void canviarPremium(String dni, boolean premium) {
        clientEntity c = clientRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));
        c.setPremium(premium);
        clientRepository.save(c);
    }
}