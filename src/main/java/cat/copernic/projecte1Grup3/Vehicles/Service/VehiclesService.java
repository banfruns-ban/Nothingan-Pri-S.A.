package cat.copernic.projecte1Grup3.Vehicles.Service;

import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Repository.VehiclesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class VehiclesService {

    private static final Set<String> ESTATS_RESERVA_INACTIVA = Set.of("CANCELADA", "ANULLADA", "FINALITZADA");

    private final VehiclesRepository vehiclesRepository;

    public VehiclesService(VehiclesRepository vehiclesRepository) {
        this.vehiclesRepository = vehiclesRepository;
    }

    @Transactional(readOnly = true)
    public List<VehiclesEntity> llistarTots() {
        return vehiclesRepository.findAll();
    }

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

    private boolean teReservesActives(VehiclesEntity vehicle) {
        if (vehicle.getReserves() == null || vehicle.getReserves().isEmpty()) {
            return false;
        }
        return vehicle.getReserves().stream()
                .anyMatch(r -> r.getEstat() == null || !ESTATS_RESERVA_INACTIVA.contains(r.getEstat().toUpperCase()));
    }

    @Transactional
    public void canviarEstatActiu(String matricula, Boolean nouEstat) {
        VehiclesEntity vehicle = vehiclesRepository.findById(matricula)
                .orElseThrow(() -> new IllegalArgumentException("No s'ha trobat el vehicle amb matrícula " + matricula));

        if (Boolean.FALSE.equals(nouEstat) && teReservesActives(vehicle)) {
            throw new IllegalStateException("No es pot donar de baixa el vehicle " + matricula + " perquè té reserves actives o pendents.");
        }

        vehicle.setActiu(nouEstat);
        vehiclesRepository.save(vehicle);
    }

    @Transactional
    public void eliminarVehicle(String matricula) {
        VehiclesEntity vehicle = vehiclesRepository.findById(matricula)
                .orElseThrow(() -> new IllegalArgumentException("No s'ha trobat el vehicle amb matrícula " + matricula));

        if (teReservesActives(vehicle)) {
            throw new IllegalStateException("No es pot donar de baixa ni eliminar el vehicle " + matricula + " perquè té reserves actives o pendents.");
        }

        if (vehicle.getReserves() != null && !vehicle.getReserves().isEmpty()) {
            throw new IllegalStateException("No es pot eliminar el vehicle " + matricula + " perquè té historial de reserves. Pots marcar-lo com a Inactiu.");
        }

        vehiclesRepository.delete(vehicle);
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