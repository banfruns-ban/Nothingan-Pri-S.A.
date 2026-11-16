package cat.copernic.projecte1Grup3.Vehicles.Service;

import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Repository.VehiclesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class VehiclesService {

    private static final Set<String> ESTATS_RESERVA_INACTIVA = Set.of("CANCELADA", "ANULLADA", "FINALITZADA");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
        Optional<VehiclesEntity> existentOpt = vehiclesRepository.findById(vehicle.getMatricula());
        if (existentOpt.isPresent()) {
            VehiclesEntity existent = existentOpt.get();
            if (vehicle.getActiu() == null) {
                vehicle.setActiu(existent.getActiu());
            }
            vehicle.setDataDesactivacio(existent.getDataDesactivacio());
            vehicle.setDataFiDesactivacio(existent.getDataFiDesactivacio());
            vehicle.setDataActivacio(existent.getDataActivacio());
            vehicle.setMotiuActivacio(existent.getMotiuActivacio());
            vehicle.setImportActivacio(existent.getImportActivacio());
        } else if (vehicle.getActiu() == null) {
            vehicle.setActiu(true);
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

    /**
     * Desactiva un vehicle en la data actual amb data de finalització opcional.
     * Bloqueja l'acció si té reserves actives o en curs.
     */
    @Transactional
    public void desactivarVehicle(String matricula, LocalDate dataFiDesactivacio) {
        VehiclesEntity vehicle = vehiclesRepository.findById(matricula)
                .orElseThrow(() -> new IllegalArgumentException("No s'ha trobat el vehicle amb matrícula " + matricula));

        if (teReservesActives(vehicle)) {
            throw new IllegalStateException("No es pot desactivar el vehicle " + matricula + " perquè té reserves actives o en curs.");
        }

        LocalDate avui = LocalDate.now();
        if (dataFiDesactivacio != null && dataFiDesactivacio.isBefore(avui)) {
            throw new IllegalArgumentException("La data de finalització de la desactivació no pot ser anterior a la data actual (" + avui.format(DATE_FMT) + ").");
        }

        vehicle.setActiu(false);
        vehicle.setEstat("EN MANTENIMENT");
        vehicle.setDataDesactivacio(avui);
        vehicle.setDataFiDesactivacio(dataFiDesactivacio);

        StringBuilder registre = new StringBuilder();
        registre.append("[").append(avui.format(DATE_FMT)).append("] DESACTIVAT");
        if (dataFiDesactivacio != null) {
            registre.append(" (Previst fins: ").append(dataFiDesactivacio.format(DATE_FMT)).append(")");
        }
        afegirComentariPrivat(vehicle, registre.toString());

        vehiclesRepository.save(vehicle);
    }

    /**
     * Activa un vehicle donat de baixa en la data actual, registrant opcionalment un motiu i un import.
     */
    @Transactional
    public void activarVehicle(String matricula, String motiu, BigDecimal importCost) {
        VehiclesEntity vehicle = vehiclesRepository.findById(matricula)
                .orElseThrow(() -> new IllegalArgumentException("No s'ha trobat el vehicle amb matrícula " + matricula));

        if (importCost != null && importCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("L'import no pot ser negatiu.");
        }

        LocalDate avui = LocalDate.now();
        String motiuNet = (motiu != null && !motiu.isBlank()) ? motiu.trim() : null;

        vehicle.setActiu(true);
        vehicle.setEstat("DISPONIBLE");
        vehicle.setDataActivacio(avui);
        vehicle.setDataFiDesactivacio(null);
        vehicle.setMotiuActivacio(motiuNet);
        vehicle.setImportActivacio(importCost);

        StringBuilder registre = new StringBuilder();
        registre.append("[").append(avui.format(DATE_FMT)).append("] ACTIVAT");
        if (motiuNet != null) {
            registre.append(" - Motiu: ").append(motiuNet);
        }
        if (importCost != null) {
            registre.append(" - Import: ").append(String.format("%.2f", importCost)).append(" €");
        }
        afegirComentariPrivat(vehicle, registre.toString());

        vehiclesRepository.save(vehicle);
    }

    private void afegirComentariPrivat(VehiclesEntity vehicle, String novaLinia) {
        String actual = vehicle.getComentarisPrivats();
        if (actual == null || actual.isBlank()) {
            vehicle.setComentarisPrivats(novaLinia);
        } else {
            vehicle.setComentarisPrivats(actual + "\n" + novaLinia);
        }
    }

    @Transactional
    public void eliminarVehicle(String matricula) {
        VehiclesEntity vehicle = vehiclesRepository.findById(matricula)
                .orElseThrow(() -> new IllegalArgumentException("No s'ha trobat el vehicle amb matrícula " + matricula));

        if (teReservesActives(vehicle)) {
            throw new IllegalStateException("No es pot eliminar el vehicle " + matricula + " perquè té reserves actives o en curs.");
        }

        if (vehicle.getReserves() != null && !vehicle.getReserves().isEmpty()) {
            throw new IllegalStateException("No es pot eliminar el vehicle " + matricula + " perquè té historial de reserves. Pots desactivar-lo.");
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