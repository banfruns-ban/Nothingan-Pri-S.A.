package cat.copernic.projecte1Grup3.Vehicles.Repository;

import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositori JPA per a la gestió de la persistència de l'entitat VehiclesEntity.
 */
@Repository
public interface VehiclesRepository extends JpaRepository<VehiclesEntity, String> {

    /**
     * Retorna la llista de vehicles filtrats per l'estat d'activació.
     */
    List<VehiclesEntity> findByActiu(Boolean actiu);

    /**
     * Cerca de vehicles per text (matrícula, marca o model), tipus (categoria) i estat actiu.
     */
    @Query("SELECT v FROM VehiclesEntity v WHERE " +
           "(:cerca IS NULL OR :cerca = '' OR " +
           "LOWER(v.matricula) LIKE LOWER(CONCAT('%', :cerca, '%')) OR " +
           "LOWER(v.marca) LIKE LOWER(CONCAT('%', :cerca, '%')) OR " +
           "LOWER(v.model) LIKE LOWER(CONCAT('%', :cerca, '%'))) " +
           "AND (:tipus IS NULL OR :tipus = '' OR v.tipusVehicle = :tipus) " +
           "AND (:actiu IS NULL OR v.actiu = :actiu)")
    List<VehiclesEntity> filtrarVehicles(
            @Param("cerca") String cerca, 
            @Param("tipus") String tipus, 
            @Param("actiu") Boolean actiu);

    List<VehiclesEntity> findByActiuTrue();
    List<VehiclesEntity> findByActiuTrueAndTipusVehicle(String tipusVehicle);

    /**
     * Cerca els vehicles actius disponibles excloent aquells que tenen reserves solapades.
     * Si no s'especifiquen dates, la subconsulta s'omple i no s'exclou cap vehicle per reserva.
     */
    @Query("SELECT v FROM VehiclesEntity v WHERE v.actiu = true " +
           "AND (:tipus IS NULL OR :tipus = '' OR v.tipusVehicle = :tipus) " +
           "AND (" +
           "   :dataInici IS NULL OR :dataFi IS NULL OR v.matricula NOT IN (" +
           "       SELECT r.vehicle.matricula FROM reservesEntity r " +
           "       WHERE r.estat NOT IN ('CANCELADA', 'ANULLADA', 'FINALITZADA') " +
           "       AND (CAST(r.dataInici AS timestamp) < :dataFi AND CAST(r.dataFi AS timestamp) > :dataInici)" +
           "   )" +
           ")")
    List<VehiclesEntity> llistarDisponiblesPerDatesYTipus(
            @Param("dataInici") LocalDateTime dataInici,
            @Param("dataFi") LocalDateTime dataFi,
            @Param("tipus") String tipus);
}