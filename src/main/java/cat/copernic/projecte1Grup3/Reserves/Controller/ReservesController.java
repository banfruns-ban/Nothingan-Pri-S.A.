/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Reserves.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;
import cat.copernic.projecte1Grup3.Reserves.Service.ReservesService;
import cat.copernic.projecte1Grup3.Usuaris.Client.Service.ClientService;

/**
 * Controlador principal de gestió de reserves al panell d'administració:
 * Gestiona el cicle de vida complet de les reserves des del dashboard
 * (consulta, alta, anul·lació, eliminació, lliurament i retorn). Coordina les
 * interaccions entre les vistes Thymeleaf i els serveis de reserves i clients.
 *
 * @author Grup3
 */
@Controller
public class ReservesController {

    private static final Logger log
            = LoggerFactory.getLogger(ReservesController.class);

    private final ReservesService reservesLogic;
    private final ClientService clientLogic;

    /**
     * Injecció de dependències dels serveis de reserves i clients: Inicialitza
     * el controlador rebent ReservesService i ClientService per gestionar totes
     * les operacions de reserva.
     */
    public ReservesController(
            ReservesService reservesLogic,
            ClientService clientLogic) {
        this.reservesLogic = reservesLogic;
        this.clientLogic = clientLogic;
    }

    /**
     * Llista i filtra les reserves registrades al sistema: Aplica els filtres
     * opcionals rebuts per paràmetre (cerca lliure, DNI, correu, data,
     * matrícula, codi o estat) mitjançant el servei. Carrega la llista
     * resultant i conserva els criteris de cerca al model abans de mostrar la
     * vista HTML.
     */
    @GetMapping("/dashboard/reserves")
    public String llistar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String email,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReserva,
            @RequestParam(required = false) String matricula,
            @RequestParam(required = false) String codi,
            @RequestParam(required = false) String estat,
            Model model) {

        model.addAttribute("reserves",
                reservesLogic.llistar(
                        q,
                        dni,
                        email,
                        dataReserva,
                        matricula,
                        codi,
                        estat
                ));

        model.addAttribute("q", q);
        model.addAttribute("dni", dni);
        model.addAttribute("email", email);
        model.addAttribute("dataReserva", dataReserva);
        model.addAttribute("matricula", matricula);
        model.addAttribute("codi", codi);
        model.addAttribute("estat", estat);

        return "reserves/llistaReserves";
    }

    /**
     * Mostra el formulari de creació d'una nova reserva des del dashboard:
     * Precarrega les dades del client (si s'indica el DNI) o la llista de
     * clients validats, juntament amb els vehicles actius. Si s'especifica un
     * DNI inexistent, redirigeix al llistat amb un missatge d'error.
     */
    @GetMapping("/dashboard/reserves/nova")
    public String mostrarFormulari(
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String matricula,
            Model model,
            RedirectAttributes ra) {

        if (dni != null && !dni.isBlank()) {
            var client = clientLogic.obtenir(dni.trim().toUpperCase());

            if (client.isEmpty()) {
                ra.addFlashAttribute("error", "Client no trobat");
                return "redirect:/dashboard/reserves";
            }

            model.addAttribute("client", client.get());

        } else {
            model.addAttribute("clients",
                    clientLogic.llistar(null, null, "VALIDAT", null, null));
        }

        if (matricula != null && !matricula.isBlank()) {
            model.addAttribute("matriculaPreseleccionada", matricula.trim().toUpperCase());
        }

        model.addAttribute("vehicles", reservesLogic.vehiclesActius());

        return "reserves/altaReserva";
    }

    /**
     * Processa l'alta d'una nova reserva des del panell de gestió: Envia les
     * dades del client, vehicle, dates, hores i observacions al servei per
     * validar-les i crear la reserva. Si es produeix algun error, conserva els
     * valors introduïts com a atributs flash i retorna al formulari d'alta.
     */
    @PostMapping("/dashboard/reserves/alta")
    public String altaReserva(
            @RequestParam String dni,
            @RequestParam String matricula,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInici,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInici,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFi,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFi,
            @RequestParam(required = false) String observacions,
            RedirectAttributes ra) {

        try {
            ReservesEntity reserva = reservesLogic.crear(
                    dni,
                    matricula,
                    dataInici,
                    horaInici,
                    dataFi,
                    horaFi,
                    observacions
            );

            ra.addFlashAttribute(
                    "missatge",
                    "Reserva creada correctament (codi "
                    + reserva.getCodiReserva() + ")"
            );

            return "redirect:/dashboard/reserves";

        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());

        } catch (Exception e) {
            log.error("Error en l'alta de la reserva", e);
            ra.addFlashAttribute(
                    "error",
                    "No s'ha pogut crear la reserva"
            );
        }

        ra.addFlashAttribute("dataInici", dataInici);
        ra.addFlashAttribute("horaInici", horaInici);
        ra.addFlashAttribute("dataFi", dataFi);
        ra.addFlashAttribute("horaFi", horaFi);
        ra.addFlashAttribute("observacions", observacions);
        ra.addAttribute("dni", dni);
        ra.addAttribute("matricula", matricula);
        return "redirect:/dashboard/reserves/nova";
    }

    /**
     * Elimina definitivament una reserva del sistema: Invoca el servei per
     * esborrar la reserva identificada pel seu codi i sincronitzar l'estat a
     * l'històric. Redirigeix al llistat de reserves informant del resultat o de
     * l'error capturat.
     */
    @PostMapping("/dashboard/reserves/{codi}/eliminar")
    public String eliminar(@PathVariable Long codi, RedirectAttributes ra) {
        try {
            reservesLogic.eliminar(codi);
            ra.addFlashAttribute("missatge", "Reserva #" + codi + " eliminada correctament");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error eliminant la reserva {}", codi, e);
            ra.addFlashAttribute("error", "No s'ha pogut eliminar la reserva");
        }
        return "redirect:/dashboard/reserves";
    }

    /**
     * Mostra el formulari d'anul·lació per a una reserva específica: Comprova
     * que la reserva existeixi i que no estigui en curs, finalitzada o ja
     * cancel·lada. Si compleix les condicions, carrega la reserva al model i
     * mostra la vista d'anul·lació.
     */
    @GetMapping("/dashboard/reserves/{codi}/anular")
    public String mostrarFormulariAnulacio(
            @PathVariable Long codi,
            Model model,
            RedirectAttributes ra) {
        Optional<ReservesEntity> optReserva = reservesLogic.obtenir(codi);
        if (optReserva.isEmpty()) {
            ra.addFlashAttribute("error", "Reserva #" + codi + " no trobada.");
            return "redirect:/dashboard/reserves";
        }

        ReservesEntity reserva = optReserva.get();
        if (ReservesService.EN_CURS.equals(reserva.getEstat())
                || ReservesService.FINALITZADA.equals(reserva.getEstat())
                || ReservesService.CANCELADA.equals(reserva.getEstat())) {
            ra.addFlashAttribute("error", "Aquesta reserva ja no es pot anul·lar.");
            return "redirect:/dashboard/reserves/" + codi;
        }

        model.addAttribute("reserva", reserva);
        return "reserves/anularReserva";
    }

    /**
     * Processa l'anul·lació d'una reserva registrant-ne el motiu: Crida al
     * servei per canviar l'estat de la reserva a cancel·lada i desar la
     * justificació proporcionada. Si falla la validació o el procés, retorna al
     * formulari d'anul·lació mantenint el text del motiu introduït.
     */
    @PostMapping("/dashboard/reserves/{codi}/anular")
    public String anular(
            @PathVariable Long codi,
            @RequestParam("anulacio_motiu") String motiu,
            RedirectAttributes ra) {
        try {
            reservesLogic.anularReserva(codi, motiu);
            ra.addFlashAttribute("missatge", "Reserva #" + codi + " anul·lada correctament.");
            return "redirect:/dashboard/reserves";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("anulacioMotiu", motiu);
            return "redirect:/dashboard/reserves/" + codi + "/anular";
        } catch (Exception e) {
            log.error("Error anul·lant la reserva {}", codi, e);
            ra.addFlashAttribute("error", "No s'ha pogut anul·lar la reserva #" + codi);
            ra.addFlashAttribute("anulacioMotiu", motiu);
            return "redirect:/dashboard/reserves/" + codi + "/anular";
        }
    }

    /**
     * Mostra la pantalla de lliurament d'un vehicle al client: Verifica que la
     * reserva existeixi i es trobi en un estat vàlid per ser lliurada (no en
     * curs, finalitzada ni cancel·lada). Carrega la reserva juntament amb la
     * data i hora actuals per defecte a la vista de lliurament.
     */
    // GET: Obrir pantalla de lliurament
    @GetMapping("/dashboard/reserves/{codi}/lliurar")
    public String mostrarFormulariLliurament(@PathVariable Long codi, Model model, RedirectAttributes ra) {
        Optional<ReservesEntity> optReserva = reservesLogic.obtenir(codi);
        if (optReserva.isEmpty()) {
            ra.addFlashAttribute("error", "Reserva #" + codi + " no trobada.");
            return "redirect:/dashboard";
        }
        ReservesEntity r = optReserva.get();
        if (ReservesService.EN_CURS.equals(r.getEstat()) || ReservesService.FINALITZADA.equals(r.getEstat()) || ReservesService.CANCELADA.equals(r.getEstat())) {
            ra.addFlashAttribute("error", "No es pot lliurar una reserva en estat " + r.getEstat());
            return "redirect:/dashboard";
        }
        model.addAttribute("reserva", r);
        model.addAttribute("dataAvui", LocalDate.now());
        model.addAttribute("horaAvui", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        return "reserves/lliurarReserva";
    }

    /**
     * Confirma i registra el lliurament del vehicle passant la reserva a estat
     * EN_CURS: Recull la data, hora, quilometratge inicial, nivell de
     * combustible, observacions i fotografies de l'estat del vehicle. Delega
     * l'operació al servei i redirigeix al dashboard amb un missatge de
     * confirmació o error.
     */
    // POST: Confirmar lliurament amb data, hora i recepció d'imatges (G3P1-240)
    @PostMapping("/dashboard/reserves/{codi}/lliurar")
    public String processarLliurament(@PathVariable Long codi,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataLliurament,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaLliurament,
            @RequestParam(required = false, defaultValue = "14850") Integer kmInicials,
            @RequestParam(required = false, defaultValue = "100%") String nivellCombustible,
            @RequestParam(required = false) String observacions,
            @RequestParam(value = "fotos", required = false) MultipartFile[] fotos,
            RedirectAttributes ra) {
        try {
            reservesLogic.lliurar(
                    codi,
                    dataLliurament != null ? dataLliurament : LocalDate.now(),
                    horaLliurament != null ? horaLliurament : LocalTime.now(),
                    kmInicials,
                    nivellCombustible,
                    observacions,
                    fotos
            );
            ra.addFlashAttribute("succes", "Reserva #" + codi + " lliurada correctament. Estat canviat a EN_CURS.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error lliurant la reserva {}", codi, e);
            ra.addFlashAttribute("error", "No s'ha pogut lliurar la reserva #" + codi);
        }
        return "redirect:/dashboard";
    }

    /**
     * Mostra la pantalla de recepció i retorn d'un vehicle llogat: Comprova que
     * la reserva existeixi i estigui en estat EN_CURS abans de permetre el
     * registre del retorn. Prepara el model amb les dades de la reserva i la
     * marca temporal actual per renderitzar el formulari.
     */
    // GET: Obrir pantalla de retorn
    @GetMapping("/dashboard/reserves/{codi}/retornar")
    public String mostrarFormulariRetorn(@PathVariable Long codi, Model model, RedirectAttributes ra) {
        Optional<ReservesEntity> optReserva = reservesLogic.obtenir(codi);
        if (optReserva.isEmpty()) {
            ra.addFlashAttribute("error", "Reserva #" + codi + " no trobada.");
            return "redirect:/dashboard";
        }
        ReservesEntity r = optReserva.get();
        if (!ReservesService.EN_CURS.equals(r.getEstat())) {
            ra.addFlashAttribute("error", "Només es poden retornar vehicles de reserves en estat EN_CURS.");
            return "redirect:/dashboard";
        }
        model.addAttribute("reserva", r);
        model.addAttribute("dataAvui", LocalDate.now());
        model.addAttribute("horaAvui", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        return "reserves/retornarReserva";
    }

    /**
     * Mostra la fitxa amb tota la informació detallada d'una reserva: Busca la
     * reserva pel seu codi identificatiu i, si existeix, la passa al model per
     * visualitzar-ne el detall complet. Si no es troba, redirigeix al llistat
     * general amb un avís d'error.
     */
    @GetMapping("/dashboard/reserves/{codi}")
    public String detall(@PathVariable Long codi, Model model, RedirectAttributes ra) {
        Optional<ReservesEntity> reserva = reservesLogic.obtenir(codi);

        if (reserva.isEmpty()) {
            ra.addFlashAttribute("error", "Reserva no trobada");
            return "redirect:/dashboard/reserves";
        }

        model.addAttribute("reserva", reserva.get());
        return "reserves/detallReserva";
    }

    /**
     * Processa el retorn del vehicle, la liquidació de la fiança i el tancament
     * del contracte: Agrupa les dades de quilometratge, combustible,
     * observacions, possibles desperfectes, deduccions i fotografies aportades.
     * Delega al servei el càlcul final i el canvi d'estat de la reserva a
     * FINALITZADA.
     */
    // POST: Confirmar retorn i liquidació de fiança amb data, hora i fotos addicionals il·limitades (G3P1-238 / G3P1-239)
    @PostMapping("/dashboard/reserves/{codi}/retornar")
    public String processarRetorn(
            @PathVariable Long codi,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataRetorn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaRetorn,
            @RequestParam(required = false) Integer kmRetorn,
            @RequestParam(required = false) String nivellCombustible,
            @RequestParam(defaultValue = "false") boolean teDesperfectes,
            @RequestParam(defaultValue = "false") boolean desperfectesImputables,
            @RequestParam(required = false, defaultValue = "0") BigDecimal gravetat,
            @RequestParam(required = false, defaultValue = "0") BigDecimal deduccioCarburant,
            @RequestParam(required = false) String observacions,
            @RequestParam(value = "fotos", required = false) MultipartFile[] fotos,
            @RequestParam(value = "fotosAddicionals", required = false) MultipartFile[] fotosAddicionals,
            @RequestParam(value = "titolsFotosAddicionals", required = false) String[] titolsFotosAddicionals,
            @RequestParam(value = "descripcionsFotosAddicionals", required = false) String[] descripcionsFotosAddicionals,
            RedirectAttributes ra) {
        try {
            boolean hiHaDanys = teDesperfectes || (gravetat != null && gravetat.compareTo(BigDecimal.ZERO) > 0);
            StringBuilder obsCompletes = new StringBuilder();
            if (kmRetorn != null) {
                obsCompletes.append("Km retorn: ").append(kmRetorn).append(" km. ");
            }
            if (nivellCombustible != null && !nivellCombustible.isBlank()) {
                obsCompletes.append("Combustible retorn: ").append(nivellCombustible.trim()).append(". ");
            }
            if (observacions != null && !observacions.isBlank()) {
                obsCompletes.append(observacions.trim());
            }

            reservesLogic.retornar(
                    codi,
                    dataRetorn,
                    horaRetorn,
                    hiHaDanys,
                    desperfectesImputables,
                    gravetat,
                    deduccioCarburant,
                    obsCompletes.toString(),
                    fotos,
                    fotosAddicionals,
                    titolsFotosAddicionals,
                    descripcionsFotosAddicionals
            );
            ra.addFlashAttribute("succes", "Vehicle de la reserva #" + codi + " retornat correctament i contracte tancat (FINALITZADA).");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error processant el retorn de la reserva {}", codi, e);
            ra.addFlashAttribute("error", "Error en intentar processar el retorn de la reserva #" + codi);
        }
        return "redirect:/dashboard";
    }
}
