package cat.copernic.projecte1Grup3.Reserves.Controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.projecte1Grup3.Reserves.Service.ReservesClientService;
import cat.copernic.projecte1Grup3.Ressenya.Service.RessenyaService;

@Controller
public class ReservesClientController {

    private final ReservesClientService reservesClientService;
    private final RessenyaService ressenyaService;

    public ReservesClientController(ReservesClientService reservesClientService,
            RessenyaService ressenyaService) {
        this.reservesClientService = reservesClientService;
        this.ressenyaService = ressenyaService;
    }

    // Llistat de reserves del client amb filtres per codi i dates (G3P1-245)
    @GetMapping("/reserves-client")
    public String mostrarReserves(
            @RequestParam(required = false) String codi,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInici,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFi,
            Authentication authentication,
            Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        model.addAttribute("reserves",
                reservesClientService.filtrarPerClient(authentication.getName(), codi, dataInici, dataFi));
        model.addAttribute("codi", codi);
        model.addAttribute("dataInici", dataInici);
        model.addAttribute("dataFi", dataFi);
        return "reserves/LesMevesReserves";
    }

    // Vista de detall d'una reserva del client (G3P1-244)
    @GetMapping("/reserves-client/{codi}")
    public String detallReserva(@PathVariable("codi") Long codi,
            Authentication authentication,
            Model model,
            RedirectAttributes ra) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        return reservesClientService.obtenirReservaDelClient(codi, authentication.getName())
                .map(r -> {
                    model.addAttribute("reserva", r);
                    return "reserves/detallReservaClient";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "No s'ha trobat aquesta reserva o no tens permís per veure-la.");
                    return "redirect:/reserves-client";
                });
    }

    // Fitxa del vehicle + ressenyes + formulari de valorar
    @GetMapping("/reserves-client/valorar/{codi}")
    public String valorarReserva(@PathVariable("codi") Long codi,
            Authentication authentication,
            Model model,
            RedirectAttributes ra) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        return reservesClientService.obtenirReservaDelClient(codi, authentication.getName())
                .map(r -> {
                    model.addAttribute("reserva", r);
                    model.addAttribute("vehicle", r.getVehicle());
                    model.addAttribute("resenyes",
                            ressenyaService.llistarPerVehicle(r.getVehicle().getMatricula()));
                    model.addAttribute("jaValorat", ressenyaService.jaHaValorat(
                            r.getVehicle().getMatricula(), r.getClient().getDni()));
                    model.addAttribute("dniClient", r.getClient().getDni());
                    return "reserves/valorarReserva";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "No s'ha trobat aquesta reserva.");
                    return "redirect:/reserves-client";
                });
    }
}