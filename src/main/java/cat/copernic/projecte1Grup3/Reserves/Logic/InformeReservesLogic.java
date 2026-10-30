package cat.copernic.projecte1Grup3.Reserves.Logic;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import cat.copernic.projecte1Grup3.Reserves.Entity.EstatHistoric;
import cat.copernic.projecte1Grup3.Reserves.Entity.HistoricReserva;
import cat.copernic.projecte1Grup3.Reserves.Entity.InformeReserves;
import cat.copernic.projecte1Grup3.Reserves.Entity.InformeReservesFila;
import cat.copernic.projecte1Grup3.Reserves.Repository.HistoricReservaRepository;

@Service
public class InformeReservesLogic {

    private static final Logger log = LoggerFactory.getLogger(InformeReservesLogic.class);
 
    /** Per client i, dins de cada client, la reserva més recent primer. */
    private static final Sort ORDRE = Sort.by(
            Sort.Order.asc("clientNom"),
            Sort.Order.asc("clientDni"),
            Sort.Order.desc("dataReserva"));
 
    private static final Pattern FORMAT_DNI = Pattern.compile("^[A-Za-z0-9]{5,15}$");
 
    private final HistoricReservaRepository repo;
 
    public InformeReservesLogic(HistoricReservaRepository repo) {
        this.repo = repo;
    }
 
    /**
     * Genera l'informe de reserves de l'històric.
     *
     * @param dnis DNIs dels clients a filtrar; si és null o buit, tots els clients
     * @return l'informe amb les files ordenades i els totals
     * @throws IllegalArgumentException si algun DNI té un format invàlid
     * @throws InformeException si falla l'accés a l'històric
     */
    public InformeReserves generarInforme(List<String> dnis) {
        List<String> filtre = validarDnis(dnis);
 
        List<HistoricReserva> historic = null;
        try {
            historic = filtre.isEmpty()
                    ? repo.findAll(ORDRE)
                    : repo.findByClientDniIn(filtre, ORDRE);
        } catch (DataAccessException e) {
            log.error("Error consultant l'històric de reserves", e);
        }
 
        List<InformeReservesFila> files = historic.stream().map(this::aFila).toList();
 
        List<HistoricReserva> vigents = historic.stream()
                .filter(h -> h.getEstat() != EstatHistoric.ANUL_LADA)
                .toList();
        BigDecimal preuTotal = vigents.stream()
            .map(this::obtenirImportTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
 
        return new InformeReserves(files, vigents.size(), preuTotal,
                historic.size() - vigents.size());
    }
 
    /** Neteja i valida els DNIs rebuts. Retorna llista buida si no hi ha filtre. */
    private List<String> validarDnis(List<String> dnis) {
        if (dnis == null) {
            return List.of();
        }
        List<String> nets = dnis.stream()
                .filter(d -> d != null && !d.isBlank())
                .map(d -> d.trim().toUpperCase())
                .distinct()
                .toList();
        for (String dni : nets) {
            if (!FORMAT_DNI.matcher(dni).matches()) {
                throw new IllegalArgumentException("El DNI '" + dni + "' no té un format vàlid.");
            }
        }
        return nets;
    }
 
    private InformeReservesFila aFila(HistoricReserva h) {
        return new InformeReservesFila(
                h.getDataReserva(),
                h.getVehicleMatricula() + " - " + h.getVehicleMarca() + " " + h.getVehicleModel(),
                h.getClientNom() + " (" + h.getClientDni() + ")",
                obtenirImportTotal(h),
                h.getEstat());
    }

    private BigDecimal obtenirImportTotal(HistoricReserva reserva) {
        try {
            Object importTotal = reserva.getClass().getMethod("getImportTotal").invoke(reserva);
            return importTotal == null ? BigDecimal.ZERO : new BigDecimal(importTotal.toString());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No s'ha pogut obtenir l'import total de la reserva", e);
        }
    }
}
