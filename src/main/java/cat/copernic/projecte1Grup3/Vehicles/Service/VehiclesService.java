package cat.copernic.projecte1Grup3.Vehicles.Service;

import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Repository.VehiclesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VehiclesService {

    private final VehiclesRepository vehiclesRepository;

    @Autowired
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
}