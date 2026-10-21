package cat.copernic.projecte1Grup3.Usuaris.Client.Logic;
 
import cat.copernic.projecte1Grup3.Usuaris.Client.Documents.clientDocuments;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.clientRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.clientRepositoryMongo;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
<<<<<<< src/main/java/cat/copernic/projecte1Grup3/Usuaris/Client/Logic/clientLogic.java
 
=======
import java.time.LocalDate;
>>>>>>> src/main/java/cat/copernic/projecte1Grup3/Usuaris/Client/Logic/clientLogic.java
@Service
public class clientLogic {
 
    public static final String PENDENT = "PENDENT";
    public static final String VALIDAT = "VALIDAT";
    public static final String REBUTJAT = "REBUTJAT";
 
    private final clientRepository clientRepository;
    private final clientRepositoryMongo clientRepositoryMongo;
 
    public clientLogic(clientRepository clientRepository,
                       clientRepositoryMongo clientRepositoryMongo) {
        this.clientRepository = clientRepository;
        this.clientRepositoryMongo = clientRepositoryMongo;
    }
 
    // ---------- Listar y buscar ----------
    public List<clientEntity> llistar(String q, String dni, String estat,
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
        return clients;
    }
 
    private boolean conte(String valor, String text) {
        return valor != null && valor.toLowerCase().contains(text);
    }
 
    public Optional<clientEntity> obtenir(String dni) {
        return clientRepository.findById(dni);
    }
 
    public Optional<clientDocuments> obtenirDocuments(String dni) {
        return clientRepositoryMongo.findById(dni);
    }
    

    





>>>>>>> src/main/java/cat/copernic/projecte1Grup3/Usuaris/Client/Logic/clientLogic.java
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