package cat.copernic.projecte1Grup3.Ressenya.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.projecte1Grup3.Ressenya.Service.ressenyaService;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.clientRepository;

@Controller
public class ressenyaController {

    private static final Logger log = LoggerFactory.getLogger(ressenyaController.class);

    private final ressenyaService resenyesLogic;
    private final clientRepository clientRepository;

    public ressenyaController(ressenyaService resenyesLogic, clientRepository clientRepository) {
        this.resenyesLogic = resenyesLogic;
        this.clientRepository = clientRepository;
    }

    // ---------- AGENT (panel): indica el DNI a mà ----------
    @PostMapping("/dashboard/vehicles/{matricula}/resenyes")
    public String alta(@PathVariable String matricula,
                       @RequestParam String dni,
                       @RequestParam int puntuacio,
                       @RequestParam(required = false) String comentari,
                       RedirectAttributes ra) {
        try {
            resenyesLogic.crear(matricula, dni, puntuacio, comentari);
            ra.addFlashAttribute("succes", "Ressenya afegida correctament");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error creant la ressenya", e);
            ra.addFlashAttribute("error", "No s'ha pogut crear la ressenya");
        }
        return "redirect:/dashboard/vehicles/detall/" + matricula;
    }

    // ---------- CLIENT (portal): el DNI surt de la sessió ----------
    @PostMapping("/reserves-client/vehicle/{matricula}/resenyes")
    public String altaClient(@PathVariable String matricula,
                             @RequestParam int puntuacio,
                             @RequestParam(required = false) String comentari,
                             @RequestParam(required = false) Long codiReserva,
                             Authentication auth,
                             RedirectAttributes ra) {
        try {
            if (auth == null || !auth.isAuthenticated()) {
                throw new IllegalArgumentException("Cal iniciar sessió com a client");
            }
            String id = auth.getName().trim();

            // El login pot ser l'email o el DNI
            clientEntity client = clientRepository.findByEmailIgnoreCase(id)
                    .or(() -> clientRepository.findById(id.toUpperCase()))
                    .orElseThrow(() -> new IllegalArgumentException("Cal iniciar sessió com a client"));

            resenyesLogic.crear(matricula, client.getDni(), puntuacio, comentari);
            ra.addFlashAttribute("succes", "Gràcies per la teva ressenya");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error creant la ressenya del client", e);
            ra.addFlashAttribute("error", "No s'ha pogut crear la ressenya");
        }

        // Torna a la fitxa de valorar si venim d'allà; si no, al formulari de reserva
        return codiReserva != null
                ? "redirect:/reserves-client/valorar/" + codiReserva
                : "redirect:/reserves-client/nova?matricula=" + matricula;
    }

    @PostMapping("/dashboard/vehicles/{matricula}/resenyes/{codi}/eliminar")
    public String eliminar(@PathVariable String matricula,
                           @PathVariable Long codi,
                           RedirectAttributes ra) {
        try {
            resenyesLogic.eliminar(codi);
            ra.addFlashAttribute("succes", "Ressenya eliminada correctament");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error eliminant la ressenya {}", codi, e);
            ra.addFlashAttribute("error", "No s'ha pogut eliminar la ressenya");
        }
        return "redirect:/dashboard/vehicles/detall/" + matricula;
    }
    // ---------- CLIENT (portal): eliminar la pròpia ressenya ----------
@PostMapping("/reserves-client/vehicle/{matricula}/resenyes/{codi}/eliminar")
public String eliminarClient(@PathVariable String matricula,
                             @PathVariable Long codi,
                             @RequestParam(required = false) Long codiReserva,
                             Authentication auth,
                             RedirectAttributes ra) {
    try {
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalArgumentException("Cal iniciar sessió com a client");
        }
        String id = auth.getName().trim();
        clientEntity client = clientRepository.findByEmailIgnoreCase(id)
                .or(() -> clientRepository.findById(id.toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Cal iniciar sessió com a client"));

        resenyesLogic.eliminarDelClient(codi, client.getDni());
        ra.addFlashAttribute("succes", "Ressenya eliminada correctament");
    } catch (IllegalArgumentException e) {
        ra.addFlashAttribute("error", e.getMessage());
    } catch (Exception e) {
        log.error("Error eliminant la ressenya {}", codi, e);
        ra.addFlashAttribute("error", "No s'ha pogut eliminar la ressenya");
    }
    return codiReserva != null
            ? "redirect:/reserves-client/valorar/" + codiReserva
            : "redirect:/reserves-client/nova?matricula=" + matricula;
}
}