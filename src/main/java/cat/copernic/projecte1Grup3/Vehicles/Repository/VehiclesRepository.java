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

    List<VehiclesEntity> findByActiu(Boolean actiu);

    /**
     * RF45: Filtrat combinat per matrícula, marca, model, any de matriculació,
     * categoria (tipus), estat (actiu/inactiu) i disponibilitat entre data mínima i màxima.
     */
    @Query("SELECT v FROM VehiclesEntity v WHERE " +
           "(:matricula IS NULL OR :matricula = '' OR LOWER(v.matricula) LIKE LOWER(CONCAT('%', :matricula, '%'))) " +
           "AND (:marca IS NULL OR :marca = '' OR LOWER(v.marca) LIKE LOWER(CONCAT('%', :marca, '%'))) " +
           "AND (:model IS NULL OR :model = '' OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%'))) " +
           "AND (:anyMatriculacio IS NULL OR v.anyMatriculacio = :anyMatriculacio) " +
           "AND (:tipus IS NULL OR :tipus = '' OR v.tipusVehicle = :tipus) " +
           "AND (:actiu IS NULL OR v.actiu = :actiu) " +
           "AND (" +
           "   (:dataInici IS NULL AND :dataFi IS NULL) OR (" +
           "       v.actiu = true AND v.estat = 'DISPONIBLE' AND v.matricula NOT IN (" +
           "           SELECT r.vehicle.matricula FROM ReservesEntity r " +
           "           WHERE r.estat NOT IN ('CANCELADA', 'ANULLADA', 'FINALITZADA') " +
           "           AND (:dataFi IS NULL OR CAST(r.dataInici AS timestamp) < :dataFi) " +
           "           AND (:dataInici IS NULL OR CAST(r.dataFi AS timestamp) > :dataInici)" +
           "       )" +
           "   )" +
           ")")
    List<VehiclesEntity> filtrarVehicles(
            @Param("matricula") String matricula,
            @Param("marca") String marca,
            @Param("model") String model,
            @Param("anyMatriculacio") Integer anyMatriculacio,
            @Param("tipus") String tipus,
            @Param("actiu") Boolean actiu,
            @Param("dataInici") LocalDateTime dataInici,
            @Param("dataFi") LocalDateTime dataFi);

    List<VehiclesEntity> findByActiuTrue();
    List<VehiclesEntity> findByActiuTrueAndTipusVehicle(String tipusVehicle);

    /**
     * Cerca els vehicles actius disponibles excloent aquells que tenen reserves solapades (Catàleg públic).
     */
    @Query("SELECT v FROM VehiclesEntity v WHERE v.actiu = true " +
           "AND (:tipus IS NULL OR :tipus = '' OR v.tipusVehicle = :tipus) " +
           "AND (" +
           "   :dataInici IS NULL OR :dataFi IS NULL OR v.matricula NOT IN (" +
           "       SELECT r.vehicle.matricula FROM ReservesEntity r " +
           "       WHERE r.estat NOT IN ('CANCELADA', 'ANULLADA', 'FINALITZADA') " +
           "       AND (CAST(r.dataInici AS timestamp) < :dataFi AND CAST(r.dataFi AS timestamp) > :dataInici)" +
           "   )" +
           ")")
    List<VehiclesEntity> llistarDisponiblesPerDatesYTipus(
            @Param("dataInici") LocalDateTime dataInici,
            @Param("dataFi") LocalDateTime dataFi,
            @Param("tipus") String tipus);
}