/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Client.Logic;

import cat.copernic.projecte1Grup3.Usuaris.Client.Documents.clientDocuments;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.clientRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.clientRepositoryMongo;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<clientEntity> llistar(String q, String estat) {
        List<clientEntity> clients = (q == null || q.isBlank())
                ? clientRepository.findAll()
                : clientRepository.findByNomContainingIgnoreCaseOrDniContainingIgnoreCase(q.trim(), q.trim());

        if (estat != null && !estat.isBlank()) {
            clients = clients.stream()
                    .filter(c -> estat.equals(c.getEstatValidacio()))
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

    // ---------- Editar ----------
    // Solo se copian los campos editables. DNI, premium y estat NO se tocan.
    @Transactional
    public void actualitzar(String dni, clientEntity dades) {
        clientEntity c = clientRepository.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));

        c.setNom(dades.getNom());
        c.setCorreu(dades.getCorreu());
        c.setNacionalitat(dades.getNacionalitat());
        c.setAdreca(dades.getAdreca());
        c.setTipusLlicencia(dades.getTipusLlicencia());
        c.setDataCaducitatDNI(dades.getDataCaducitatDNI());
        clientRepository.save(c);
    }

    // ---------- Borrar (MySQL y Mongo) ----------
    // Primero MySQL: si Mongo falla, queda un documento huérfano (inofensivo),
    // pero nunca un cliente sin sus imágenes.
    @Transactional
    public void eliminar(String dni) {
        clientRepository.deleteById(dni);
        clientRepositoryMongo.deleteById(dni);
    }

    // ---------- Validación del administrador ----------
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
