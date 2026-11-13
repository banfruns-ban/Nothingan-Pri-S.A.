package cat.copernic.projecte1Grup3.Vehicles.Service;

import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Repository.VehiclesRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VehiclesService {

    private final VehiclesRepository vehiclesRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public VehiclesService(VehiclesRepository vehiclesRepository) {
        this.vehiclesRepository = vehiclesRepository;
    }

    @Transactional(readOnly = true)
    public List<VehiclesEntity> llistarTots() {
        return vehiclesRepository.findAll();
    }

    /**
     * Filtra els vehicles segons el text de cerca, categoria (tipus) i estat d'activació.
     */
    @Transactional(readOnly = true)
    public List<VehiclesEntity> filtrarVehicles(String cerca, String tipus, Boolean actiu) {
        if ((cerca == null || cerca.isBlank()) && 
            (tipus == null || tipus.isBlank()) && 
            actiu == null) {
            return vehiclesRepository.findAll();
        }
        return vehiclesRepository.filtrarVehicles(cerca, tipus, actiu);
    }

    @Transactional(readOnly = true)
    public Optional<VehiclesEntity> buscarPerMatricula(String matricula) {
        return vehiclesRepository.findById(matricula);
    }

    @Transactional
    public VehiclesEntity guardarVehicle(VehiclesEntity vehicle) {
        if (vehicle.getActiu() == null) {
            Optional<VehiclesEntity> existent = vehiclesRepository.findById(vehicle.getMatricula());
            if (existent.isPresent()) {
                vehicle.setActiu(existent.get().getActiu());
            } else {
                vehicle.setActiu(true);
            }
        }
        return vehiclesRepository.save(vehicle);
    }

    /**
     * Actualitza un vehicle existent permetent també modificar la seva matrícula (PK).
     */
    @Transactional
    public VehiclesEntity actualitzarVehicle(String matriculaOriginal, VehiclesEntity dadesActualitzades) {
        VehiclesEntity antic = vehiclesRepository.findById(matriculaOriginal)
                .orElseThrow(() -> new IllegalArgumentException("No s'ha trobat el vehicle original: " + matriculaOriginal));

        String novaMatricula = dadesActualitzades.getMatricula().trim().toUpperCase();
        dadesActualitzades.setMatricula(novaMatricula);

        if (dadesActualitzades.getActiu() == null) {
            dadesActualitzades.setActiu(antic.getActiu());
        }

        // Si la matrícula NO ha canviat, actualitzem directament
        if (matriculaOriginal.equalsIgnoreCase(novaMatricula)) {
            dadesActualitzades.setReserves(antic.getReserves());
            return vehiclesRepository.save(dadesActualitzades);
        }

        // Si la matrícula SÍ ha canviat, comprovem que la nova no estigui ocupada
        if (vehiclesRepository.existsById(novaMatricula)) {
            throw new IllegalArgumentException("La matrícula " + novaMatricula + " ja està registrada en un altre vehicle.");
        }

        // 1. Creem el nou registre amb la nova matrícula
        VehiclesEntity nouVehicle = vehiclesRepository.saveAndFlush(dadesActualitzades);

        // 2. Reassignem les reserves associades al vehicle antic cap al nou
        List<ReservesEntity> reservesAssociades = new ArrayList<>(antic.getReserves());
        for (ReservesEntity reserva : reservesAssociades) {
            reserva.setVehicle(nouVehicle);
        }
        antic.getReserves().clear();

        // 3. Reassignem possibles ressenyes vinculades a l'antiga matrícula (si n'hi ha)
        try {
            entityManager.createQuery("UPDATE RessenyaEntity r SET r.vehicle = :nou WHERE r.vehicle = :antic")
                    .setParameter("nou", nouVehicle)
                    .setParameter("antic", antic)
                    .executeUpdate();
        } catch (Exception ignored) {
            // Si RessenyaEntity no té relació directa amb VehiclesEntity, ho ignorem
        }

        entityManager.flush();

        // 4. Eliminem el registre amb la matrícula antiga
        vehiclesRepository.delete(antic);
        vehiclesRepository.flush();

        return nouVehicle;
    }

    /**
     * Modifica l'estat d'activació (Actiu / Inactiu) del vehicle.
     */
    @Transactional
    public void canviarEstatActiu(String matricula, Boolean nouEstat) {
        Optional<VehiclesEntity> vehicleOpt = vehiclesRepository.findById(matricula);
        if (vehicleOpt.isPresent()) {
            VehiclesEntity vehicle = vehicleOpt.get();
            vehicle.setActiu(nouEstat);
            vehiclesRepository.save(vehicle);
        }
    }

    @Transactional
    public void eliminarVehicle(String matricula) {
        vehiclesRepository.deleteById(matricula);
    }
    
    @Transactional(readOnly = true)
    public List<VehiclesEntity> llistarActius() {
        return vehiclesRepository.findByActiuTrue();
    }

    @Transactional(readOnly = true)
    public List<VehiclesEntity> llistarActiusPerTipus(String tipus) {
        if (tipus == null || tipus.isBlank()) {
            return llistarActius();
        }
        return vehiclesRepository.findByActiuTrueAndTipusVehicle(tipus);
    }

    /**
     * Retorna els vehicles disponibles tenint en compte les dates de la reserva i el tipus.
     * Si les dates són nul·les, es mostren tots els vehicles actius per defecte.
     */
    @Transactional(readOnly = true)
    public List<VehiclesEntity> llistarDisponibles(LocalDateTime dataInici, LocalDateTime dataFi, String tipus) {
        if (dataInici == null || dataFi == null) {
            return llistarActiusPerTipus(tipus);
        }
        return vehiclesRepository.llistarDisponiblesPerDatesYTipus(dataInici, dataFi, tipus);
    }
}