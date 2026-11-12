package cat.copernic.projecte1Grup3.Usuaris.Client.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cat.copernic.projecte1Grup3.Usuaris.Client.Documents.ClientDocuments;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
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
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepositoryMongo;

@Service
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);
    public static final String PENDENT = "PENDENT";
    public static final String VALIDAT = "VALIDAT";
    public static final String REBUTJAT = "REBUTJAT";

    private final ClientRepository clientRepository;
    private final ClientRepositoryMongo clientRepositoryMongo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ClientService(ClientRepository clientRepository,
                         ClientRepositoryMongo clientRepositoryMongo) {
        this(clientRepository, clientRepositoryMongo, new BCryptPasswordEncoder());
    }

    public ClientService(ClientRepository clientRepository,
                         ClientRepositoryMongo clientRepositoryMongo,
                         PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.clientRepositoryMongo = clientRepositoryMongo;
        this.passwordEncoder = passwordEncoder != null ? passwordEncoder : new BCryptPasswordEncoder();
    }

    // ---------- LLISTAR I FILTRAR ----------
    public List<ClientEntity> llistar(String q, String dni, String estat, String email,
                                      LocalDate dataNaixement) {
        List<ClientEntity> clients = clientRepository.findAll();

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
    @Transactional(rollbackFor = Exception.class)
    public void alta(ClientEntity client, MultipartFile fotoDniFrontal,
                     MultipartFile fotoDniPosterior, MultipartFile fotoCarnet) throws IOException {

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

        client.setPremium(false);
        client.setEstatValidacio(PENDENT);
        clientRepository.saveAndFlush(client);

        ClientDocuments doc = new ClientDocuments();
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
        log.info("Client donat d'alta: {}", dni);
    }

    public Optional<ClientEntity> obtenir(String dni) {
        return clientRepository.findById(dni);
    }

    public Optional<ClientDocuments> obtenirDocuments(String dni) {
        return clientRepositoryMongo.findById(dni);
    }

    // ---------- ACTUALITZACIÓ (PANEL AGENT) ----------
    @Transactional(rollbackFor = Exception.class)
    public void actualitzar(String dni, ClientEntity dades, String estat, boolean premium,
                            MultipartFile fotoDniFrontal, MultipartFile fotoDniPosterior,
                            MultipartFile fotoCarnet) throws IOException {

        ClientEntity c = clientRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));

        if (!VALIDAT.equals(estat) && !REBUTJAT.equals(estat) && !PENDENT.equals(estat)) {
            throw new IllegalArgumentException("Estat no vàlid");
        }

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

        if (hiHa(fotoDniFrontal) || hiHa(fotoDniPosterior) || hiHa(fotoCarnet)) {
            ClientDocuments doc = clientRepositoryMongo.findById(dni).orElseGet(() -> {
                ClientDocuments nou = new ClientDocuments();
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

    // ---------- PERFIL DEL CLIENT (AUTOGESTIÓ) ----------
    public Optional<ClientEntity> obtenirPerEmail(String email) {
        return clientRepository.findByEmailIgnoreCase(email);
    }

    @Transactional(rollbackFor = Exception.class)
    public void actualitzarPerfilClient(String emailActual, ClientEntity dades) {
        ClientEntity c = clientRepository.findByEmailIgnoreCase(emailActual)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));

        String nouEmail = dades.getEmail() == null ? "" : dades.getEmail().trim();
        if (!nouEmail.isEmpty() && clientRepository.existsByEmailIgnoreCaseAndDniNot(nouEmail, c.getDni())) {
            throw new IllegalArgumentException("Aquest correu ja està en ús per un altre client");
        }

        c.setNom(dades.getNom());
        c.setEmail(nouEmail);
        if (dades.getPassword() != null && !dades.getPassword().isBlank()) {
            if (dades.getPassword().length() < 8) {
                throw new IllegalArgumentException("La contrasenya ha de tenir com a mínim 8 caràcters");
            }
            c.setPassword(passwordEncoder.encode(dades.getPassword()));
        }
        c.setTelefon(dades.getTelefon());
        c.setNacionalitat(dades.getNacionalitat());
        c.setAdreca(dades.getAdreca());
        c.setTipusLlicencia(dades.getTipusLlicencia());

        if (dades.getDataNaixement() != null) {
            c.setDataNaixement(dades.getDataNaixement());
        }
        if (dades.getDataCaducitatDNI() != null) {
            c.setDataCaducitatDNI(dades.getDataCaducitatDNI());
        }

        clientRepository.saveAndFlush(c);
        log.info("Perfil actualitzat pel client: {}", c.getDni());
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

    // ---------- ELIMINACIÓ DE CLIENT ----------
    @Transactional
    public void eliminar(String dni) {
        ClientEntity c = clientRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));

        clientRepository.delete(c);
        clientRepository.flush();

        clientRepositoryMongo.deleteById(dni);
        log.warn("Client eliminat: {}", dni);
    }

    // ---------- CANVI D'ESTAT DE VALIDACIÓ (RF82) ----------
    @Transactional
    public void canviarEstat(String dni, String estat) {
        if (!VALIDAT.equals(estat) && !REBUTJAT.equals(estat) && !PENDENT.equals(estat)) {
            throw new IllegalArgumentException("Estat no vàlid");
        }
        ClientEntity c = clientRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));
        
        c.setEstatValidacio(estat);
        clientRepository.save(c);
    }

    // ---------- CANVI D'ESTAT PREMIUM ----------
    @Transactional
    public void canviarPremium(String dni, boolean premium) {
        ClientEntity c = clientRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));
        c.setPremium(premium);
        clientRepository.save(c);
    }
}