package cat.copernic.projecte1Grup3.Vehicles.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.projecte1Grup3.Ressenya.Service.ressenyaService;
import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Service.VehiclesService;
import jakarta.validation.Valid;

@Controller // Indica que la classe es un controlador **
@RequestMapping("/dashboard/vehicles") // Base de las url de todas las rutas del doc
public class VehiclesController {
    private final VehiclesService vehiclesService;
    private final ressenyaService ressenyaLogic;
    @Autowired
    public VehiclesController(VehiclesService vehiclesService, ressenyaService ressenyaLogic) {
        this.vehiclesService = vehiclesService;
        this.ressenyaLogic = ressenyaLogic; 
    }

    @GetMapping//Protocolo http que ejecuta la orden GET (Listar,ver,detalle,cargar vista,formularios, etc...)
    public String llistarVehicles(
            @RequestParam(name = "cerca", required = false) String cerca,
            @RequestParam(name = "tipus", required = false) String tipus,
            @RequestParam(name = "estat", required = false) Boolean estat,
            Model model) {
        
        List<VehiclesEntity> vehicles = vehiclesService.filtrarVehicles(cerca, tipus, estat);
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("cerca", cerca);
        model.addAttribute("tipus", tipus);
        model.addAttribute("estat", estat);
       
        
        return "vehicles/llistat";
    }

    @GetMapping("/detall/{matricula}")
    public String mostrarDetall(@PathVariable("matricula") String matricula, Model model, RedirectAttributes redirect) {
        Optional<VehiclesEntity> vehicle = vehiclesService.buscarPerMatricula(matricula);
        if (vehicle.isPresent()) {
            model.addAttribute("vehicle", vehicle.get());
            model.addAttribute("resenyes", ressenyaLogic.llistarPerVehicle(matricula));
            return "vehicles/detallVehicle";
        }
        redirect.addFlashAttribute("error", "El vehicle amb matrícula " + matricula + " no existeix.");
        return "redirect:/dashboard/vehicles";
    }

    @GetMapping("/nou")
    public String formulariNouVehicle(Model model) {//Conecta la capa controller directamente con la capa vista
        model.addAttribute("vehicle", new VehiclesEntity());
        return "vehicles/altaVehicle";
    }

    @GetMapping("/editar/{matricula}")
    public String formulariEditarVehicle(@PathVariable("matricula") String matricula, Model model, RedirectAttributes redirect) {
        Optional<VehiclesEntity> vehicle = vehiclesService.buscarPerMatricula(matricula);
        if (vehicle.isPresent()) {
            model.addAttribute("vehicle", vehicle.get());
            return "vehicles/editarVehicle";
        }
        redirect.addFlashAttribute("error", "No s'ha trobat el vehicle per editar.");
        return "redirect:/dashboard/vehicles";
    }

    @PostMapping("/guardar")//Protocolo http que ejecuta la orden POST (Modificar,Guardar,Eliminar,etc..)
    public String guardarVehicle(
            @Valid @ModelAttribute("vehicle") VehiclesEntity vehicle,
            BindingResult result,
            @RequestParam(name = "file", required = false) MultipartFile file,
            Model model,
            RedirectAttributes redirect) {
        //Asegura que no da problemas en atributos que pueden ser nulos
        Optional<VehiclesEntity> existentOpt = vehiclesService.buscarPerMatricula(vehicle.getMatricula());
        boolean existeixBDD = vehicle.getMatricula() != null && existentOpt.isPresent();

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

        // Gestió de la foto
        if (file != null && !file.isEmpty()) {
            try {
                String fileName = vehicle.getMatricula() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get("uploads/vehicles");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                vehicle.setFoto(fileName);
            } catch (IOException e) {
                redirect.addFlashAttribute("error", "Error en carregar la imatge del vehicle.");
            }
        } else if (existeixBDD) {
            // Si estem editant i no s'ha pujat cap foto nova, mantenim la foto existent
            vehicle.setFoto(existentOpt.get().getFoto());
        }

        vehiclesService.guardarVehicle(vehicle);
        redirect.addFlashAttribute("succes", "Vehicle desat correctament.");
        return "redirect:/dashboard/vehicles";
    }

    @PostMapping("/canviar-estat/{matricula}")
    public String canviarEstatVehicle(
            @PathVariable("matricula") String matricula, 
            @RequestParam("actiu") Boolean actiu, 
            RedirectAttributes redirect) {
        try {
            vehiclesService.canviarEstatActiu(matricula, actiu);
            redirect.addFlashAttribute("succes", "L'estat del vehicle s'ha actualitzat correctament.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error en canviar l'estat del vehicle.");
        }
        return "redirect:/dashboard/vehicles";
    }

    @PostMapping("/eliminar/{matricula}")
    public String eliminarVehicle(@PathVariable("matricula") String matricula, RedirectAttributes redirect) {
        try {
            vehiclesService.eliminarVehicle(matricula);
            redirect.addFlashAttribute("succes", "Vehicle eliminat amb èxit.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error en intentar eliminar el vehicle.");
        }
        return "redirect:/dashboard/vehicles";
    }
}