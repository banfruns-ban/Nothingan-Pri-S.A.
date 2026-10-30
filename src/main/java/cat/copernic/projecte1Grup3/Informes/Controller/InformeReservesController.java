package cat.copernic.projecte1Grup3.Informes.Controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import cat.copernic.projecte1Grup3.Informes.Service.InformeReservesService;
import cat.copernic.projecte1Grup3.Usuaris.Client.Service.clientService;

@Controller
public class InformeReservesController {
 
    private final InformeReservesService informeService;
    private final clientService clientLogic;
 
    public InformeReservesController(InformeReservesService informeService,
                                     clientService clientLogic) {
        this.informeService = informeService;
        this.clientLogic = clientLogic;
    }
 
    /**
     * Mostra l'informe de reserves per client. Sense paràmetres, tots els clients;
     * amb un o més {@code dnis}, només els seleccionats.
     */
    @GetMapping("/informes/reserves")
    public String informeReserves(@RequestParam(required = false) List<String> dnis,
                                  Model model) {
        // Mateixa crida que ja feu servir a reservesController per al desplegable de clients
        model.addAttribute("clients", clientLogic.llistar(null, null, "VALIDAT", null, null));
        model.addAttribute("dnisSeleccionats", dnis == null ? List.of() : dnis);
        try {
            model.addAttribute("informe", informeService.generarInforme(dnis));
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "informes/informeReserves";
    }
}