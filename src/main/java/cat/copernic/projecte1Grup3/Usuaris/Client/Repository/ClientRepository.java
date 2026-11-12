package cat.copernic.projecte1Grup3.Usuaris.Client.Repository;

import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

// Repositorio JPA para MySQL: ofrece métodos CRUD estándar y ejecución de consultas dinámicas
public interface ClientRepository extends JpaRepository<ClientEntity, String>, 
        JpaSpecificationExecutor<ClientEntity> {

    // Método derivado automático de Spring Data: busca coincidencias por Nombre o DNI ignorando mayúsculas/minúsculas
    List<ClientEntity> findByNomContainingIgnoreCaseOrDniContainingIgnoreCase(String nom, String dni);
    Optional<ClientEntity> findByEmailIgnoreCase(String email);
    // Devuelve true si OTRO cliente (distinto DNI) ya usa ese email
    boolean existsByEmailIgnoreCaseAndDniNot(String email, String dni);
}