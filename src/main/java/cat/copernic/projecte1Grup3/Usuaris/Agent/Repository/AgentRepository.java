package cat.copernic.projecte1Grup3.Usuaris.Agent.Repository;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.AgentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositori JPA per a l'entitat AgentEntity.
 */
@Repository
public interface AgentRepository extends JpaRepository<AgentEntity, String> {

    // Canviat 'agentEntity' a 'AgentEntity' i afegit CAST per a l'Enum Rol
    @Query("SELECT a FROM AgentEntity a WHERE "
            + "LOWER(a.dni) LIKE LOWER(CONCAT('%', :cerca, '%')) OR "
            + "LOWER(a.nom) LIKE LOWER(CONCAT('%', :cerca, '%')) OR "
            + "LOWER(CAST(a.rol AS string)) LIKE LOWER(CONCAT('%', :cerca, '%')) OR "
            + "LOWER(a.email) LIKE LOWER(CONCAT('%', :cerca, '%'))")
    List<AgentEntity> cercarPerDniNomEmail(@Param("cerca") String cerca);
    Optional<AgentEntity> findByEmailIgnoreCase(String email);
}
