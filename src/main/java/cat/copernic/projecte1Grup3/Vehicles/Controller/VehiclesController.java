package cat.copernic.projecte1Grup3.Vehicles.Controller;

import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Service.VehiclesService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * hola
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
     */
    @GetMapping
    public String llistarVehicles(Model model) {
        model.addAttribute("vehicles", vehiclesService.llistarTots());
        return "vehicles/llistat";
    }

    /**
     * RF44: Visualitzar detalls d'un vehicle.
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
     */
    @GetMapping("/nou")
    public String formulariNouVehicle(Model model) {
        model.addAttribute("vehicle", new VehiclesEntity());
        return "vehicles/altaVehicle";
    }

    /**
     * RF42: Formulari exclusiu per a la Modificació d'un vehicle.
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
     * Processa el desament (creació o modificació) d'un vehicle amb validacions.
     */
    @PostMapping("/guardar")
    public String guardarVehicle(
            @Valid @ModelAttribute("vehicle") VehiclesEntity vehicle,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {

        boolean existeixBDD = vehicle.getMatricula() != null && 
                              vehiclesService.buscarPerMatricula(vehicle.getMatricula()).isPresent();

        if (!existeixBDD && vehicle.getMatricula() != null && !vehicle.getMatricula().isBlank()) {
            if (vehiclesService.buscarPerMatricula(vehicle.getMatricula()).isPresent()) {
                result.rejectValue("matricula", "error.vehicle", "Aquesta matrícula ja està registrada.");
            }
        }

        if (vehicle.getMinDiesLloguer() != null && vehicle.getMaxDiesLloguer() != null) {
            if (vehicle.getMinDiesLloguer() > vehicle.getMaxDiesLloguer()) {
                result.rejectValue("maxDiesLloguer", "error.vehicle", "El màxim de dies ha de ser major o igual al mínim.");
            }
        }

        if (result.hasErrors()) {
            return existeixBDD ? "vehicles/editarVehicle" : "vehicles/altaVehicle";
        }

        vehiclesService.guardarVehicle(vehicle);
        redirect.addFlashAttribute("succes", "Vehicle desat correctament.");
        return "redirect:/vehicles";
    }

    /**
     * Canvia l'estat d'activació (Actiu / Inactiu) des del menú desplegable de la taula.
     */
    @PostMapping("/canviar-estat/{matricula}")
    public String canviarEstatVehicle(
            @PathVariable String matricula, 
            @RequestParam("actiu") Boolean actiu, 
            RedirectAttributes redirect) {
        try {
            vehiclesService.canviarEstatActiu(matricula, actiu);
            redirect.addFlashAttribute("succes", "L'estat del vehicle s'ha actualitzat correctament.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error en canviar l'estat del vehicle.");
        }
        return "redirect:/vehicles";
    }

    /**
     * RF41: Baixa / Eliminar vehicle de la BDD.
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