/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Reserves.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.projecte1Grup3.Reserves.Service.ReservesClientService;
import cat.copernic.projecte1Grup3.Ressenya.Service.RessenyaService;

/**
 * Controlador per a la consulta de reserves i valoracions des de l'àrea de
 * client: Gestiona la visualització de l'historial de reserves pròpies i
 * l'accés a la vista de valoració dels vehicles llogats. Verifica en tot moment
 * l'autenticació de l'usuari i que la reserva consultada pertanyi al client
 * connectat.
 *
 * @author Grup3
 */
@Controller
public class ReservesClientController {

    private final ReservesClientService reservesClientService;
    private final RessenyaService ressenyaService;

    /**
     * Injecció de dependències dels serveis de reserves de client i ressenyes:
     * Inicialitza el controlador rebent ReservesClientService i RessenyaService
     * per consultar les reserves i valoracions.
     */
    public ReservesClientController(ReservesClientService reservesClientService,
            RessenyaService ressenyaService) {
        this.reservesClientService = reservesClientService;
        this.ressenyaService = ressenyaService;
    }

    /**
     * Mostra el llistat de reserves associades al client autenticat: Comprova
     * si la sessió és vàlida i recupera totes les reserves vinculades al correu
     * de l'usuari connectat. Carrega la llista al model i renderitza la
     * plantilla HTML "reserves/LesMevesReserves".
     */
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

    /**
     * Mostra la fitxa de valoració d'una reserva concreta del client: Valida
     * que la reserva pertanyi al client autenticat i carrega al model el
     * vehicle, les ressenyes existents i si ja ha valorat. Si la reserva no
     * existeix o no correspon a l'usuari, redirigeix al llistat principal amb
     * un missatge d'error.
     */
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
