package cat.copernic.projecte1Grup3.Reserves.Controller;

import cat.copernic.projecte1Grup3.Reserves.Entity.reservesEntity;
import cat.copernic.projecte1Grup3.Reserves.Service.reservesService;
import cat.copernic.projecte1Grup3.Usuaris.Client.Service.clientService;

import java.time.LocalDate;
import java.time.LocalTime;

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

        return "reservas/llistaReserves";
    }

    @GetMapping("/reserves/nova")
    public String mostrarFormulari(
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String matricula, // <-- Afegit per rebre la matrícula del vehicle
            Model model,
            RedirectAttributes ra) {

        if (dni != null && !dni.isBlank()) {
            var client = clientLogic.obtenir(dni.trim().toUpperCase());

            if (client.isEmpty()) {
                ra.addFlashAttribute("error", "Client no trobat");
                return "redirect:/reserves";          // torna a la llista
            }

            model.addAttribute("client", client.get());

        } else {
            // Sense client prefixat: desplegable amb els clients validats
            model.addAttribute("clients",
                    clientLogic.llistar(null, null, "VALIDAT", null, null));
        }

        // Si ve una matrícula preseleccionada des de la fitxa del vehicle, la passem a la vista
        if (matricula != null && !matricula.isBlank()) {
            model.addAttribute("matriculaPreseleccionada", matricula.trim().toUpperCase());
        }

        model.addAttribute("vehicles", reservesLogic.vehiclesActius());

        return "reservas/altaReserva";               // el formulari, a la seva carpeta
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

    // =====================================================
    // RF49: LLIURAR VEHICLE (DASHBOARD I LLISTATS)
    // =====================================================
    @GetMapping("/reserves/{codi}/lliurar")
    public String lliurarVehicle(@PathVariable Long codi, RedirectAttributes ra) {
        try {
            reservesLogic.lliurar(codi, LocalDate.now(), LocalTime.now(), "Lliurament efetuat des del Dashboard");
            ra.addFlashAttribute("succes", "Reserva #" + codi + " lliurada correctament.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error lliurant la reserva {}", codi, e);
            ra.addFlashAttribute("error", "No s'ha pogut lliurar la reserva #" + codi);
        }
        return "redirect:/dashboard";
    }
}