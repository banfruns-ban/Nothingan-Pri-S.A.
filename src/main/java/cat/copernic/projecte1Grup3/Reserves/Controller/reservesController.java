package cat.copernic.projecte1Grup3.Reserves.Controller;

import cat.copernic.projecte1Grup3.Reserves.Entity.reservesEntity;
import cat.copernic.projecte1Grup3.Reserves.Service.reservesService;
import cat.copernic.projecte1Grup3.Usuaris.Client.Service.clientService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class reservesController {

    private static final Logger log
            = LoggerFactory.getLogger(reservesController.class);

    private final reservesService reservesLogic;
    private final clientService clientLogic;

    public reservesController(
            reservesService reservesLogic,
            clientService clientLogic) {
        this.reservesLogic = reservesLogic;
        this.clientLogic = clientLogic;
    }

    @GetMapping("/reserves")
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

        // Apunta a reservas/LlistaReserves.html (L mayúscula)
        return "reserves/LlistaReserves";
    }

    @GetMapping("/reserves/nova")
    public String mostrarFormulari(
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String matricula,
            Model model,
            RedirectAttributes ra) {

        if (dni != null && !dni.isBlank()) {
            var client = clientLogic.obtenir(dni.trim().toUpperCase());

            if (client.isEmpty()) {
                ra.addFlashAttribute("error", "Client no trobat");
                return "redirect:/reserves";
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

        // Apunta a reservas/altaReserva.html (a minúscula)
        return "reserves/altaReserva";
    }

    @PostMapping("/reserves/alta")
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
            reservesEntity reserva = reservesLogic.crear(
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

            return "redirect:/reserves";

        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());

        } catch (Exception e) {
            log.error("Error en l'alta de la reserva", e);
            ra.addFlashAttribute(
                    "error",
                    "No s'ha pogut crear la reserva"
            );
        }

        return "redirect:/reserves/nova?dni=" + dni;
    }

    @PostMapping("/reserves/{codi}/eliminar")
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
        return "redirect:/reserves";
    }

    // GET: Obrir Screen 9 (Lliurament)
    @GetMapping("/reserves/{codi}/lliurar")
    public String mostrarFormulariLliurament(@PathVariable Long codi, Model model, RedirectAttributes ra) {
        Optional<reservesEntity> optReserva = reservesLogic.obtenir(codi);
        if (optReserva.isEmpty()) {
            ra.addFlashAttribute("error", "Reserva #" + codi + " no trobada.");
            return "redirect:/dashboard";
        }
        reservesEntity r = optReserva.get();
        if (reservesService.EN_CURS.equals(r.getEstat()) || reservesService.FINALITZADA.equals(r.getEstat()) || reservesService.CANCELADA.equals(r.getEstat())) {
            ra.addFlashAttribute("error", "No es pot lliurar una reserva en estat " + r.getEstat());
            return "redirect:/dashboard";
        }
        model.addAttribute("reserva", r);
        // Apunta a reservas/LliurarReserva.html (L mayúscula)
        return "reservas/LliurarReserva";
    }

    // POST: Confirmar Lliurament (Screen 9)
    @PostMapping("/reserves/{codi}/lliurar")
    public String processarLliurament(@PathVariable Long codi,
                                      @RequestParam(required = false, defaultValue = "14850") Integer kmInicials,
                                      @RequestParam(required = false, defaultValue = "100%") String nivellCombustible,
                                      @RequestParam(required = false) String observacions,
                                      RedirectAttributes ra) {
        try {
            reservesLogic.lliurar(codi, LocalDate.now(), LocalTime.now(), kmInicials, nivellCombustible, observacions);
            ra.addFlashAttribute("succes", "Reserva #" + codi + " lliurada correctament. Estat canviat a EN_CURS.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error lliurant la reserva {}", codi, e);
            ra.addFlashAttribute("error", "No s'ha pogut lliurar la reserva #" + codi);
        }
        return "redirect:/dashboard";
    }

    // GET: Obrir pantalla de Retorn
    @GetMapping("/reserves/{codi}/retornar")
    public String mostrarFormulariRetorn(@PathVariable Long codi, Model model, RedirectAttributes ra) {
        Optional<reservesEntity> optReserva = reservesLogic.obtenir(codi);
        if (optReserva.isEmpty()) {
            ra.addFlashAttribute("error", "Reserva #" + codi + " no trobada.");
            return "redirect:/dashboard";
        }
        reservesEntity r = optReserva.get();
        if (!reservesService.EN_CURS.equals(r.getEstat())) {
            ra.addFlashAttribute("error", "Només es poden retornar vehicles de reserves en estat EN_CURS.");
            return "redirect:/dashboard";
        }
        model.addAttribute("reserva", r);
        model.addAttribute("dataAvui", LocalDate.now());
        model.addAttribute("horaAvui", LocalTime.now());
        // Apunta a reservas/retornarReserva.html (r minúscula)
        return "reservas/retornarReserva";
    }
    
    @GetMapping("/reserves/{codi}")
public String detall(@PathVariable Long codi, Model model, RedirectAttributes ra) {
    Optional<reservesEntity> reserva = reservesLogic.obtenir(codi);

    if (reserva.isEmpty()) {
        ra.addFlashAttribute("error", "Reserva no trobada");
        return "redirect:/reserves";
    }

    model.addAttribute("reserva", reserva.get());
    return "reserves/detallReserva";
}

    // POST: Confirmar Retorn i Liquidació de Fiança
    @PostMapping("/reserves/{codi}/retornar")
    public String processarRetorn(
            @PathVariable Long codi,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataRetorn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaRetorn,
            @RequestParam(defaultValue = "false") boolean teDesperfectes,
            @RequestParam(defaultValue = "false") boolean desperfectesImputables,
            @RequestParam(required = false, defaultValue = "0") BigDecimal gravetat,
            @RequestParam(required = false, defaultValue = "0") BigDecimal deduccioCarburant,
            @RequestParam(required = false) String observacions,
            RedirectAttributes ra) {
        try {
            reservesLogic.retornar(codi, dataRetorn, horaRetorn, teDesperfectes, desperfectesImputables, gravetat, deduccioCarburant, observacions);
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