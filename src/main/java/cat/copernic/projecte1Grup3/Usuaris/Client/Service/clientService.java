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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service // Marca la clase como Capa de Servicio (Lógica de Negocio)
public class clientService {

    private static final Logger log = LoggerFactory.getLogger(clientService.class);
    // Constantes de estado para el cumplimiento del RF82
    public static final String PENDENT = "PENDENT";
    public static final String VALIDAT = "VALIDAT";
    public static final String REBUTJAT = "REBUTJAT";
    

    private final clientRepository clientRepository;
    private final clientRepositoryMongo clientRepositoryMongo;
    private final PasswordEncoder passwordEncoder;

    // Inyección de dependencias por constructor de ambos repositorios (Relacional + NoSQL)
    public clientService(clientRepository clientRepository,
                         clientRepositoryMongo clientRepositoryMongo) {
        this(clientRepository, clientRepositoryMongo, new BCryptPasswordEncoder());
    }

    public clientService(clientRepository clientRepository,
                         clientRepositoryMongo clientRepositoryMongo,
                         PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.clientRepositoryMongo = clientRepositoryMongo;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------- LISTAR Y FILTRAR ----------
    public List<clientEntity> llistar(String q, String dni, String estat, String email,
                                     LocalDate dataNaixement) {
        // 1. Búsqueda general inicial en MySQL
        List<clientEntity> clients = clientRepository.findAll();

        // 2. Filtro general por texto (nombre, DNI/NIE, email o teléfono)
        if (q != null && !q.isBlank()) {
            String text = q.trim().toLowerCase();
            clients = clients.stream()
                    .filter(c -> conte(c.getNom(), text) || conte(c.getDni(), text)
                            || conte(c.getEmail(), text) || conte(c.getTelefon(), text))
                    .toList();
        }

        // 3. Filtro específico por DNI / NIE
        if (dni != null && !dni.isBlank()) {
            String d = dni.trim().toUpperCase();
            clients = clients.stream()
                    .filter(c -> c.getDni() != null && c.getDni().toUpperCase().contains(d))
                    .toList();
        }

        // 4. Filtro por estado de validación (PENDENT, VALIDAT, REBUTJAT)
        if (estat != null && !estat.isBlank()) {
            clients = clients.stream()
                    .filter(c -> estat.equals(c.getEstatValidacio()))
                    .toList();
        }

        // 5. Filtro por fecha de nacimiento exacta
        if (dataNaixement != null) {
            clients = clients.stream()
                    .filter(c -> dataNaixement.equals(c.getDataNaixement()))
                    .toList();
        }

        // 6. Filtro por correo electrónico
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

    // ---------- ALTA DE CLIENTE (Doble Persistencia) ----------
    @Transactional(rollbackFor = Exception.class) // Asegura que se guarde en ambas BD o se cancele todo si hay un error
    public void alta(clientEntity client, MultipartFile fotoDniFrontal,
                     MultipartFile fotoDniPosterior, MultipartFile fotoCarnet) throws IOException {

        // Validación del formato oficial de DNI/NIE con Expresión Regular
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

        // RF82: Todo registro externo nace en estado PENDENT por defecto
        client.setPremium(false);
        client.setEstatValidacio(PENDENT);
        clientRepository.saveAndFlush(client); // Guarda en MySQL

        // Guarda las 3 imágenes en formato binario dentro de MongoDB
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
        clientRepositoryMongo.save(doc); // Guarda en MongoDB
        log.info("Client donat d'alta: {}", dni);
    }

    public Optional<clientEntity> obtenir(String dni) {
        return clientRepository.findById(dni);
    }

    public Optional<clientDocuments> obtenirDocuments(String dni) {
        return clientRepositoryMongo.findById(dni);
    }

// ---------- ACTUALIZACIÓN ----------
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

    // Actualiza los archivos en MongoDB solo si se adjuntaron imágenes nuevas
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

    // Comprueba formato (PNG/JPEG) y restringe el tamaño a 5MB máx.
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

    // ---------- ELIMINACIÓN DE CLIENTE ----------
    @Transactional
    public void eliminar(String dni) {
        clientEntity c = clientRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));

        clientRepository.delete(c);
        clientRepository.flush(); // Si el cliente tiene reservas asociadas, salta la excepción aquí y cancela el borrado

        clientRepositoryMongo.deleteById(dni); // Elimina de MongoDB solo tras confirmar el éxito en MySQL
        log.warn("Client eliminat: {}", dni);
    }

    // ---------- CAMBIO DE ESTADO DE VALIDACIÓN (RF82) ----------
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

    // ---------- CAMBIO DE ESTADO PREMIUM ----------
    @Transactional
    public void canviarPremium(String dni, boolean premium) {
        clientEntity c = clientRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));
        c.setPremium(premium);
        clientRepository.save(c);
    }
}