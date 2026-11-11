package cat.copernic.projecte1Grup3.Reserves.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.projecte1Grup3.Reserves.Service.ReservesClientService;
import cat.copernic.projecte1Grup3.Ressenya.Service.ressenyaService;

@Controller
public class ReservesClientController {

    private final ReservesClientService reservesClientService;
    private final ressenyaService ressenyaService;

    public ReservesClientController(ReservesClientService reservesClientService,
                                    ressenyaService ressenyaService) {
        this.reservesClientService = reservesClientService;
        this.ressenyaService = ressenyaService;
    }

    // Listado de mis reservas
    @GetMapping("/reserves-client")
    public String mostrarReserves(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        model.addAttribute("reserves",
                reservesClientService.llistarPerClient(authentication.getName()));
        return "reserves/LesMevesReserves";
    }

    // Ficha del vehículo + reseñas + formulario de valorar
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
                            ressenyaService.llistarPerVehicle(r.getVehicle().getMatricula()));model.addAttribute("jaValorat", ressenyaService.jaHaValorat(
        r.getVehicle().getMatricula(), r.getClient().getDni()));
                    return "reserves/valorarReserva";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "No s'ha trobat aquesta reserva.");
                    return "redirect:/reserves-client";
                });
    }
}