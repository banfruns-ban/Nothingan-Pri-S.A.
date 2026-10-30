package cat.copernic.projecte1Grup3.Reserves.Repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import cat.copernic.projecte1Grup3.Reserves.Entity.HistoricReserva;

public interface HistoricReservaRepository extends MongoRepository<HistoricReserva, Long> {
 
    /** Reserves de l'històric d'un o més clients, amb l'ordre indicat. */
    List<HistoricReserva> findByClientDniIn(List<String> dnis, Sort sort);
}