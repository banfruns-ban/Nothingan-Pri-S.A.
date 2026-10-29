package cat.copernic.projecte1Grup3.Reserves.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import cat.copernic.projecte1Grup3.Reserves.DTO.resumClientDTO;
import cat.copernic.projecte1Grup3.Reserves.Entity.historicReserva;

@Repository
public interface historicReservesRepository extends MongoRepository<historicReserva, String> {

    @Aggregation(pipeline = {
        "{ '$match': { 'clientDni': { '$in': ?0 } } }",
        "{ '$group': { '_id': '$clientDni', 'totalReserves': { '$sum': 1 }, 'preuTotalReserves': { '$sum': '$preuTotal' }, 'detallReserves': { '$push': { 'idReserva': '$idReserva', 'preuTotal': '$preuTotal', 'dataInici': '$dataInici', 'dataFi': '$dataFi' } } } }",
        "{ '$project': { '_id': 0, 'dniClient': '$_id', 'totalReserves': 1, 'preuTotalReserves': 1, 'detallReserves': 1 } }"
    })
    List<resumClientDTO> obtenirInformeReservesPerClients(List<String> dnisClients);
}