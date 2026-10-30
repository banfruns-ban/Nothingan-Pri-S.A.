package cat.copernic.projecte1Grup3.Informes.Service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cat.copernic.projecte1Grup3.Informes.Entity.EstatHistoric;
import cat.copernic.projecte1Grup3.Informes.Entity.HistoricReserva;
import cat.copernic.projecte1Grup3.Informes.Repository.HistoricReservaRepository;
import cat.copernic.projecte1Grup3.Reserves.Entity.reservesEntity;

/**
 * Servei de l'històric de reserves (Mongo). Es crida des de reservesService
 * en crear, eliminar, anul·lar o finalitzar una reserva.
 */
@Service
public class HistoricService {

    public static final String ELIMINADA = "ELIMINADA";

    private static final Logger log = LoggerFactory.getLogger(HistoricService.class);

    private final HistoricReservaRepository repo;

    public HistoricService(HistoricReservaRepository repo) {
        this.repo = repo;
    }

    public void desar(reservesEntity r, EstatHistoric estat) {
        HistoricReserva h = repo.findById(r.getCodiReserva()).orElseGet(HistoricReserva::new);

        h.setIdReserva(r.getCodiReserva());
        if (h.getDataReserva() == null) {
            h.setDataReserva(LocalDateTime.now());
        }
        h.setDataInici(r.getDataInici());
        h.setDataFi(r.getDataFi());
        h.setClientDni(r.getClient().getDni());
        h.setClientNom(r.getClient().getNom());
        h.setVehicleMatricula(r.getVehicle().getMatricula());
        h.setVehicleMarca(r.getVehicle().getMarca());
        h.setVehicleModel(r.getVehicle().getModel());
        h.setImportTotal(r.getCostTotal());
        h.setEstat(estat); // EstatHistoric, no String
        h.setDataUltimEvent(LocalDateTime.now());

        repo.save(h);
        log.info("Històric: reserva {} desada amb estat {}", r.getCodiReserva(), estat);
    }
}