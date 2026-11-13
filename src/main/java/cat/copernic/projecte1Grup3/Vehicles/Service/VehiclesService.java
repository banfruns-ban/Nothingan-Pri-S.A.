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
     * RF45: Filtra els vehicles combinant matrícula, marca, model, any de matriculació,
     * categoria, estat d'activació i disponibilitat entre data mínima i data màxima.
     */
    @Transactional(readOnly = true)
    public List<VehiclesEntity> filtrarVehicles(
            String matricula,
            String marca,
            String model,
            Integer anyMatriculacio,
            String tipus,
            Boolean actiu,
            LocalDateTime dataInici,
            LocalDateTime dataFi) {

        boolean senseFiltres = (matricula == null || matricula.isBlank()) &&
                               (marca == null || marca.isBlank()) &&
                               (model == null || model.isBlank()) &&
                               anyMatriculacio == null &&
                               (tipus == null || tipus.isBlank()) &&
                               actiu == null &&
                               dataInici == null &&
                               dataFi == null;

        if (senseFiltres) {
            return vehiclesRepository.findAll();
        }

        return vehiclesRepository.filtrarVehicles(
                matricula != null ? matricula.trim() : null,
                marca != null ? marca.trim() : null,
                model != null ? model.trim() : null,
                anyMatriculacio,
                tipus,
                actiu,
                dataInici,
                dataFi);
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

    @Transactional
    public VehiclesEntity actualitzarVehicle(String matriculaOriginal, VehiclesEntity dadesActualitzades) {
        VehiclesEntity antic = vehiclesRepository.findById(matriculaOriginal)
                .orElseThrow(() -> new IllegalArgumentException("No s'ha trobat el vehicle original: " + matriculaOriginal));

        String novaMatricula = dadesActualitzades.getMatricula().trim().toUpperCase();
        dadesActualitzades.setMatricula(novaMatricula);

        if (dadesActualitzades.getActiu() == null) {
            dadesActualitzades.setActiu(antic.getActiu());
        }

        if (matriculaOriginal.equalsIgnoreCase(novaMatricula)) {
            dadesActualitzades.setReserves(antic.getReserves());
            return vehiclesRepository.save(dadesActualitzades);
        }

        if (vehiclesRepository.existsById(novaMatricula)) {
            throw new IllegalArgumentException("La matrícula " + novaMatricula + " ja està registrada en un altre vehicle.");
        }

        VehiclesEntity nouVehicle = vehiclesRepository.saveAndFlush(dadesActualitzades);

        List<ReservesEntity> reservesAssociades = new ArrayList<>(antic.getReserves());
        for (ReservesEntity reserva : reservesAssociades) {
            reserva.setVehicle(nouVehicle);
        }
        antic.getReserves().clear();

        try {
            entityManager.createQuery("UPDATE RessenyaEntity r SET r.vehicle = :nou WHERE r.vehicle = :antic")
                    .setParameter("nou", nouVehicle)
                    .setParameter("antic", antic)
                    .executeUpdate();
        } catch (Exception ignored) {
        }

        entityManager.flush();

        vehiclesRepository.delete(antic);
        vehiclesRepository.flush();

        return nouVehicle;
    }

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

    @Transactional(readOnly = true)
    public List<VehiclesEntity> llistarDisponibles(LocalDateTime dataInici, LocalDateTime dataFi, String tipus) {
        if (dataInici == null || dataFi == null) {
            return llistarActiusPerTipus(tipus);
        }
        return vehiclesRepository.llistarDisponiblesPerDatesYTipus(dataInici, dataFi, tipus);
    }
}