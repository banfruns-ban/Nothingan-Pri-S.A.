package cat.copernic.projecte1Grup3.Usuaris.Agent.Controller;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.AgentEntity;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Service.AgentService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/dashboard/agents")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping("/alta")
    public String mostrarFormulariAlta(Model model) {
        model.addAttribute("agent", new AgentEntity());
        model.addAttribute("esEdicio", false);
        return "agents/alta_agent";
    }

    @PostMapping("/alta")
    public String processarAlta(
            @Valid @ModelAttribute("agent") AgentEntity agent,
            BindingResult result,
            @RequestParam(value = "esEdicio", defaultValue = "false") boolean esEdicio,
            @RequestParam(value = "fitxerFoto", required = false) MultipartFile fitxerFoto,
            Model model) {

        model.addAttribute("esEdicio", esEdicio);

        String dniNet = agent.getDni() == null ? "" : agent.getDni().trim().toUpperCase();
        agent.setDni(dniNet);

        Optional<AgentEntity> agentExistent = agentService.obtenirPerDni(dniNet);

        if (result.hasErrors()) {
            return "agents/alta_agent";
        }

        if (!esEdicio && agentExistent.isPresent()) {
            model.addAttribute("error", "Ja existeix un agent registrat amb aquest DNI.");
            return "agents/alta_agent";
        }

        if (fitxerFoto != null && !fitxerFoto.isEmpty()) {
            try {
                String nomFitxer = agent.getDni() + "_" + fitxerFoto.getOriginalFilename();
                Path directoriUploads = Paths.get("uploads/agents");
                if (!Files.exists(directoriUploads)) {
                    Files.createDirectories(directoriUploads);
                }
                Path rutaFitxer = directoriUploads.resolve(nomFitxer);
                Files.copy(fitxerFoto.getInputStream(), rutaFitxer, StandardCopyOption.REPLACE_EXISTING);
                agent.setFoto(nomFitxer);
            } catch (IOException e) {
                model.addAttribute("error", "Error en pujar la foto del perfil.");
                return "agents/alta_agent";
            }
        } else if (esEdicio && agentExistent.isPresent()) {
            agent.setFoto(agentExistent.get().getFoto());
        }

        try {
            agentService.guardarAgent(agent);
            return "redirect:/dashboard/agents/llista?exit";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "agents/alta_agent";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "Ja existeix un usuari amb aquest DNI o correu electrònic.");
            return "agents/alta_agent";
        }
    }

    @GetMapping({"", "/", "/llista"})
    public String llistarAgents(@RequestParam(name = "cerca", required = false) String cerca, Model model) {
        List<AgentEntity> llista = agentService.llistarOFiltrarAgents(cerca);
        model.addAttribute("agents", llista);
        return "agents/llistat_agents";
    }

    @GetMapping("/detall/{dni}")
    public String consultarDetall(@PathVariable("dni") String dni, Model model) {
        Optional<AgentEntity> agent = agentService.obtenirPerDni(dni);
        if (agent.isPresent()) {
            model.addAttribute("agent", agent.get());
            return "agents/detall_agent";
        }
        return "redirect:/dashboard/agents/llista";
    }

    @GetMapping("/editar/{dni}")
    public String mostrarFormulariEditar(@PathVariable("dni") String dni, Model model) {
        Optional<AgentEntity> agent = agentService.obtenirPerDni(dni);
        if (agent.isPresent()) {
            model.addAttribute("agent", agent.get());
            model.addAttribute("esEdicio", true);
            return "agents/alta_agent";
        }
        return "redirect:/dashboard/agents/llista";
    }

    @PostMapping("/eliminar/{dni}")
    public String eliminarAgent(@PathVariable("dni") String dni, RedirectAttributes redirect) {
        try {
            agentService.eliminarAgent(dni);
            redirect.addFlashAttribute("succes", "Agent donat de baixa amb èxit.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error en intentar donar de baixa.");
        }
        return "redirect:/dashboard/agents/llista";
    }
}
