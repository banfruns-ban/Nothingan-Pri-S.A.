package cat.copernic.projecte1Grup3.Reserves.Repository;

import cat.copernic.projecte1Grup3.Reserves.Entity.reservesEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface reservesRepository extends JpaRepository<reservesEntity, Long> {

    // Reserves d'un vehicle que no tenen l'estat indicat (s'usa per ignorar les CANCELADA)
    List<reservesEntity> findByVehicleMatriculaAndEstatNot(String matricula, String estat);

    // ============================================================
    // RF91: CONSULTA D'INFORME PER CLIENTS
    // ============================================================
    // Cerca totes les reserves que pertanyen a qualsevol dels DNIs d'una llista de clients.
    @Query("SELECT r FROM reservesEntity r WHERE r.client.dni IN :dnis")
    List<reservesEntity> findByClientDniIn(@Param("dnis") List<String> dnis);
}
