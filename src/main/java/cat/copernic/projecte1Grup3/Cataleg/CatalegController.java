package cat.copernic.projecte1Grup3.Cataleg;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Service.VehiclesService;

@Controller
public class CatalegController {

    private final VehiclesService vehiclesService;

    public CatalegController(VehiclesService vehiclesService) {
        this.vehiclesService = vehiclesService;
    }

    @GetMapping("/cataleg")
    public String mostrarCataleg(
            @RequestParam(name = "dataInici", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInici,
            @RequestParam(name = "dataFi", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFi,
            @RequestParam(name = "tipus", required = false) String tipus,
            Model model) {

        // Validació de seguretat per al rang de dates
        if (dataInici != null && dataFi != null && dataFi.isBefore(dataInici)) {
            model.addAttribute("error", "La data de fi no pot ser anterior a la data d'inici.");
        }

        // Crida al servei passant dates i tipus per obtenir els vehicles lliures
        List<VehiclesEntity> vehiclesActius = vehiclesService.llistarDisponibles(dataInici, dataFi, tipus);

        model.addAttribute("vehicles", vehiclesActius);
        model.addAttribute("tipusSeleccionat", tipus);
        model.addAttribute("dataInici", dataInici);
        model.addAttribute("dataFi", dataFi);

        return "vehicles/cataleg";
    }
}