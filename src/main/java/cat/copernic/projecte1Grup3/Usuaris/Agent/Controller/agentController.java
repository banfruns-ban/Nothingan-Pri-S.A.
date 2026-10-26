package cat.copernic.projecte1Grup3.Usuaris.Agent.Controller;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.agentEntity;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Service.agentService;
// Import del servicio de vehículos si fuera necesario
// import cat.copernic.projecte1Grup3.Vehicles.Service.VehiclesService; 

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/agents") // Busca allí
public class agentController {

    private final agentService agentService;
    // private final VehiclesService vehiclesService; // Solo se añade si realmente se va a usar en este controlador

    // Inyección por constructor (recomendada por Spring en lugar de @Autowired en atributos)
    public agentController(agentService agentService/*, VehiclesService vehiclesService*/) {
        this.agentService = agentService;
        // this.vehiclesService = vehiclesService;
    }

    @GetMapping("/alta") // Es para pedir formularios listar cosas
    public String mostrarFormulariAlta(Model model) {
        model.addAttribute("agent", new agentEntity()); // Controller --> Vista
        return "agents/alta_agent";
    }

    @PostMapping("/alta") // Editar, eliminar todo
    public String processarAlta(@ModelAttribute("agent") agentEntity agent) {
        agentService.guardarAgent(agent);
        return "redirect:/agents/alta?exit";
    }
}

//crear listado ()