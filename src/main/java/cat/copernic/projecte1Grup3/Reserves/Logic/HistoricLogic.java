package cat.copernic.projecte1Grup3.Reserves.Logic;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cat.copernic.projecte1Grup3.Reserves.Entity.EstatHistoric;
import cat.copernic.projecte1Grup3.Reserves.Entity.HistoricReserva;
import cat.copernic.projecte1Grup3.Reserves.Entity.reservesEntity;
import cat.copernic.projecte1Grup3.Reserves.Repository.HistoricReservaRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;

@Service
public class HistoricLogic {
 
    private static final Logger log = LoggerFactory.getLogger(HistoricLogic.class);
 
    private final HistoricReservaRepository repo;
 
    public HistoricLogic(HistoricReservaRepository repo) {
        this.repo = repo;
    }
 
    /**
     * Registra la creació d'una reserva a l'històric.
     * IMPORTANT: s'ha de cridar DESPRÉS de reservesRepository.save(), perquè el
     * codiReserva (IDENTITY) només existeix un cop la reserva s'ha desat.
     *
     * @param reserva reserva ja persistida a MySQL
     */
    public void registrarCreacio(reservesEntity reserva) {
        clientEntity client = reserva.getClient();
        VehiclesEntity vehicle = reserva.getVehicle();
 
        HistoricReserva h = new HistoricReserva();
        h.setIdReserva(reserva.getCodiReserva());
        // reservesEntity no té data de creació: és el moment en què es registra l'alta
        h.setDataReserva(LocalDateTime.now());
        h.setDataInici(reserva.getDataInici());
        h.setDataFi(reserva.getDataFi());
        h.setClientDni(client.getDni());
        h.setClientNom(client.getNom());
        h.setVehicleMatricula(vehicle.getMatricula());
        h.setVehicleMarca(vehicle.getMarca());
        h.setVehicleModel(vehicle.getModel());
        h.setImportTotal(reserva.getCostTotal());   // només lloguer, sense fiança
        h.setEstat(EstatHistoric.CREADA);
        h.setDataUltimEvent(LocalDateTime.now());
 
        repo.save(h);
        log.info("Històric: reserva {} creada", reserva.getCodiReserva());
    }
 
    /**
     * Registra l'anul·lació d'una reserva.
     *
     * @param codiReserva codi de la reserva anul·lada
     */
    public void registrarAnulacio(Long codiReserva) {
        canviarEstat(codiReserva, EstatHistoric.ANUL_LADA);
    }
 
    /**
     * Registra la finalització d'una reserva (retorn del vehicle).
     *
     * @param codiReserva codi de la reserva finalitzada
     */
    public void registrarFinalitzacio(Long codiReserva) {
        canviarEstat(codiReserva, EstatHistoric.FINALITZADA);
    }
 
    private void canviarEstat(Long codiReserva, EstatHistoric nouEstat) {
        repo.findById(codiReserva).ifPresentOrElse(h -> {
            h.setEstat(nouEstat);
            h.setDataUltimEvent(LocalDateTime.now());
            repo.save(h);
            log.info("Històric: reserva {} passa a {}", codiReserva, nouEstat);
        }, () -> log.warn("Històric: no existeix la reserva {} (estat {})", codiReserva, nouEstat));
    }
}