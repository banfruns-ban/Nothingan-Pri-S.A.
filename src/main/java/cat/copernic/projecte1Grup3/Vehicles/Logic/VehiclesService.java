package cat.copernic.projecte1Grup3.Vehicles.Service;

import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Repository.VehiclesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servei encarregat de la lògica de negoci per als Requisits Funcionals RF40 a RF44.
 */
@Service
public class VehiclesService {

    private final VehiclesRepository vehiclesRepository;

    @Autowired
    public VehiclesService(VehiclesRepository vehiclesRepository) {
        this.vehiclesRepository = vehiclesRepository;
    }

    /**
     * RF43: Llistar tots els vehicles de la base de dades.
     * @return Llista completa de vehicles
     */
    @Transactional(readOnly = true)
    public List<VehiclesEntity> llistarTots() {
        return vehiclesRepository.findAll();
    }

    /**
     * RF44: Consulta detalls d'un vehicle mitjançant la seva matrícula.
     * @param matricula Identificador únic del vehicle
     * @return Optional amb l'entitat trobada
     */
    @Transactional(readOnly = true)
    public Optional<VehiclesEntity> buscarPerMatricula(String matricula) {
        return vehiclesRepository.findById(matricula);
    }

    /**
     * RF40 i RF42: Guardar o modificar un vehicle.
     * En cas d'alta (RF40), s'estableix per defecte com a actiu.
     * @param vehicle Entitat a persistir
     * @return Entitat guardada
     */
    @Transactional
    public VehiclesEntity guardarVehicle(VehiclesEntity vehicle) {
        if (vehicle.getActiu() == null) {
            vehicle.setActiu(true); // Actiu per defecte en donar d'alta
        }
        return vehiclesRepository.save(vehicle);
    }

    /**
     * RF41: Baixa de vehicle.
     * No es permet la baixa si el vehicle té reserves associades.
     * @param matricula Matrícula del vehicle a eliminar
     * @throws IllegalStateException si el vehicle té reserves associades
     */
    @Transactional
    public void eliminarVehicle(String matricula) {
        // TODO: Quan estigui integrat el repositori de Reserves, verificar:
        // boolean teReserves = reservesRepository.existsByVehicleMatricula(matricula);
        // if (teReserves) {
        //     throw new IllegalStateException("No es pot eliminar el vehicle perquè té reserves associades.");
        // }
        
        vehiclesRepository.deleteById(matricula);
    }
}