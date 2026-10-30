package cat.copernic.projecte1Grup3.Informes.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import cat.copernic.projecte1Grup3.Informes.Entity.EstatHistoric;
import cat.copernic.projecte1Grup3.Informes.Entity.HistoricReserva;
import cat.copernic.projecte1Grup3.Informes.Entity.InformeReserves;
import cat.copernic.projecte1Grup3.Informes.Entity.InformeReservesFila;
import cat.copernic.projecte1Grup3.Informes.Repository.HistoricReservaRepository;

@Service
public class InformeReservesService {

    private static final Logger log = LoggerFactory.getLogger(InformeReservesService.class);

    /** Per client i, dins de cada client, la reserva més recent primer. */
    private static final Sort ORDRE = Sort.by(
            Sort.Order.asc("clientNom"),
            Sort.Order.asc("clientDni"),
            Sort.Order.desc("dataReserva"));

    private static final Pattern FORMAT_DNI = Pattern.compile("^[A-Za-z0-9]{5,15}$");

    /** Estats que no compten als totals. */
    private static final Set<EstatHistoric> NO_COMPTABLES =
            Set.of(EstatHistoric.CANCELADA, EstatHistoric.ELIMINADA);

    private final HistoricReservaRepository repo;

    public InformeReservesService(HistoricReservaRepository repo) {
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
    public InformeReserves generarInforme(List<String> dnis) throws InformeException {
        List<String> filtre = validarDnis(dnis);

        List<HistoricReserva> historic;
        try {
            historic = filtre.isEmpty()
                    ? repo.findAll(ORDRE)
                    : repo.findByClientDniIn(filtre, ORDRE);
        } catch (DataAccessException e) {
            log.error("Error consultant l'històric de reserves", e);
            throw new InformeException(
                    "No s'ha pogut consultar l'històric de reserves. Torneu-ho a provar més tard.");
        }

        List<InformeReservesFila> files = historic.stream().map(this::aFila).toList();

        List<HistoricReserva> vigents = historic.stream()
                .filter(h -> !NO_COMPTABLES.contains(h.getEstat()))
                .toList();

        BigDecimal preuTotal = vigents.stream()
                .map(h -> h.getImportTotal() == null ? BigDecimal.ZERO : h.getImportTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalAnullades = (int) historic.stream()
                .filter(h -> h.getEstat() == EstatHistoric.CANCELADA)
                .count();

        return new InformeReserves(files, vigents.size(), preuTotal, totalAnullades);
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
                h.getImportTotal(),
                h.getEstat());
    }
}