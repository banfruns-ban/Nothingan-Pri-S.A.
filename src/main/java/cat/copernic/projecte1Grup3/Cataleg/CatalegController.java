package cat.copernic.projecte1Grup3.Cataleg;

import java.util.List;

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
            @RequestParam(name = "tipus", required = false) String tipus,
            Model model) {

        List<VehiclesEntity> vehiclesActius = vehiclesService.llistarActiusPerTipus(tipus);

        model.addAttribute("vehicles", vehiclesActius);
        model.addAttribute("tipusSeleccionat", tipus);

        return "vehicles/cataleg";
    }
}