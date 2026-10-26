package cat.copernic.projecte1Grup3.Vehicles.Repository;

import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositori JPA per a la gestió de la persistència de l'entitat VehiclesEntity.
 */
@Repository
public interface VehiclesRepository extends JpaRepository<VehiclesEntity, String> {

    /**
     * Retorna la llista de vehicles filtrats per l'estat d'activació.
     * @param actiu Estat d'activació (true/false)
     * @return Llista de vehicles
     */
    List<VehiclesEntity> findByActiu(Boolean actiu);
}