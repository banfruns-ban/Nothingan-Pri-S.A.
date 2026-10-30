package cat.copernic.projecte1Grup3.Usuaris.Client.Repository;

import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

// Repositorio JPA para MySQL: ofrece métodos CRUD estándar y ejecución de consultas dinámicas
public interface clientRepository extends JpaRepository<clientEntity, String>,
                                           JpaSpecificationExecutor<clientEntity> {

    // Método derivado automático de Spring Data: busca coincidencias por Nombre o DNI ignorando mayúsculas/minúsculas
    List<clientEntity> findByNomContainingIgnoreCaseOrDniContainingIgnoreCase(String nom, String dni);
}