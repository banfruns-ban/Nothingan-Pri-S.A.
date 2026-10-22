package cat.copernic.projecte1Grup3.Vehicles.Controller;

import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Service.VehiclesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controladors per a les visites i accions del CRUD de Vehicles (RF40 - RF44).
 */
@Controller
@RequestMapping("/vehicles")
public class VehiclesController {

    private final VehiclesService vehiclesService;

    @Autowired
    public VehiclesController(VehiclesService vehiclesService) {
        this.vehiclesService = vehiclesService;
    }

    /**
     * RF43: Llistar tots els vehicles.
     * Retorna: templates/vehicles/llistat.html
     */
    @GetMapping
    public String llistarVehicles(Model model) {
        model.addAttribute("vehicles", vehiclesService.llistarTots());
        return "vehicles/llistat";
    }

    /**
     * RF44: Visualitzar detalls d'un vehicle.
     * Retorna: templates/vehicles/detall.html
     */
    @GetMapping("/detall/{matricula}")
    public String mostrarDetall(@PathVariable String matricula, Model model, RedirectAttributes redirect) {
        Optional<VehiclesEntity> vehicle = vehiclesService.buscarPerMatricula(matricula);
        if (vehicle.isPresent()) {
            model.addAttribute("vehicle", vehicle.get());
            return "vehicles/detall";
        }
        redirect.addFlashAttribute("error", "El vehicle no existeix.");
        return "redirect:/vehicles";
    }

    /**
     * RF40: Formulari exclusiu per a l'Alta d'un vehicle.
     * Retorna: templates/vehicles/altaVehicle.html
     */
    @GetMapping("/nou")
    public String formulariNouVehicle(Model model) {
        model.addAttribute("vehicle", new VehiclesEntity());
        return "vehicles/altaVehicle";
    }

    /**
     * RF42: Formulari exclusiu per a la Modificació d'un vehicle.
     * Retorna: templates/vehicles/editarVehicle.html
     */
    @GetMapping("/editar/{matricula}")
    public String formulariEditarVehicle(@PathVariable String matricula, Model model, RedirectAttributes redirect) {
        Optional<VehiclesEntity> vehicle = vehiclesService.buscarPerMatricula(matricula);
        if (vehicle.isPresent()) {
            model.addAttribute("vehicle", vehicle.get());
            return "vehicles/editarVehicle";
        }
        redirect.addFlashAttribute("error", "No s'ha trobat el vehicle per editar.");
        return "redirect:/vehicles";
    }

    /**
     * Processa el desament (creació o modificació) d'un vehicle.
     */
    @PostMapping("/guardar")
    public String guardarVehicle(@ModelAttribute("vehicle") VehiclesEntity vehicle, RedirectAttributes redirect) {
        vehiclesService.guardarVehicle(vehicle);
        redirect.addFlashAttribute("succes", "Vehicle desat correctament.");
        return "redirect:/vehicles";
    }

    /**
     * RF41: Baixa / Desactivació d'un vehicle.
     */
    @PostMapping("/eliminar/{matricula}")
    public String eliminarVehicle(@PathVariable String matricula, RedirectAttributes redirect) {
        try {
            vehiclesService.eliminarVehicle(matricula);
            redirect.addFlashAttribute("succes", "Vehicle eliminat amb èxit.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error en intentar eliminar el vehicle.");
        }
        return "redirect:/vehicles";
    }
}