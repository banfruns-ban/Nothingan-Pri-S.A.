package cat.copernic.projecte1Grup3.Usuaris.Agent.Controller;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.agentEntity;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Service.agentService;
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

/**
 * Controlador Spring MVC per a la gestió d'agents.
 */
@Controller
@RequestMapping("/agents")
public class agentController {

    private final agentService agentService;

    public agentController(agentService agentService) {
        this.agentService = agentService;
    }

    /**
     * Mostra el formulari d'alta d'un nou agent.
     */
    @GetMapping("/alta")
    public String mostrarFormulariAlta(Model model) {
        model.addAttribute("agent", new agentEntity());
        return "agents/alta_agent";
    }

    /**
     * Processa el formulari d'alta i edició d'agents.
     */
    @PostMapping("/alta")
    public String processarAlta(@Valid @ModelAttribute("agent") agentEntity agent,
                                BindingResult result,
                                @RequestParam(value = "fitxerFoto", required = false) MultipartFile fitxerFoto,
                                Model model) {

        Optional<agentEntity> agentExistent = agentService.obtenirPerDni(agent.getDni());
        boolean esEdicio = agentExistent.isPresent();

        if (result.hasErrors()) {
            return "agents/alta_agent";
        }

        // Gestió de la imatge de perfil
        if (fitxerFoto != null && !fitxerFoto.isEmpty()) {
            try {
                String nomFitxer = agent.getDni() + "_" + fitxerFoto.getOriginalFilename();
                
                // Desa les fotos directament dins de uploads/agentes
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
        } else if (esEdicio) {
            // Si és una edició i no s'ha adjuntat cap foto nova, mantenim la foto que ja tenia
            agent.setFoto(agentExistent.get().getFoto());
        }

        try {
            agentService.guardarAgent(agent);
            return "redirect:/agents/llista?exit";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "Ja existeix un usuari amb aquest DNI o correu electrònic.");
            return "agents/alta_agent";
        }
    }

    /**
     * RF13 & RF14: Mostra la llista d'agents.
     */
    @GetMapping({"", "/", "/llista"})
    public String llistarAgents(@RequestParam(name = "cerca", required = false) String cerca, Model model) {
        List<agentEntity> llista = agentService.llistarOFiltrarAgents(cerca);
        model.addAttribute("agents", llista);
        return "agents/llistat_agents";
    }

    /**
     * RF14 & RF15: Consulta els detalls d'un agent.
     */
    @GetMapping("/detall/{dni}")
    public String consultarDetall(@PathVariable("dni") String dni, Model model) {
        Optional<agentEntity> agent = agentService.obtenirPerDni(dni);
        if (agent.isPresent()) {
            model.addAttribute("agent", agent.get());
            return "agents/detall_agent";
        }
        return "redirect:/agents/llista";
    }

    /**
     * RF12: Carrega les dades d'un agent al formulari per a la seva modificació.
     */
    @GetMapping("/editar/{dni}")
    public String mostrarFormulariEditar(@PathVariable("dni") String dni, Model model) {
        Optional<agentEntity> agent = agentService.obtenirPerDni(dni);
        if (agent.isPresent()) {
            model.addAttribute("agent", agent.get());
            return "agents/alta_agent";
        }
        return "redirect:/agents/llista";
    }

    /**
     * RF11: Donar de baixa un agent.
     */
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
        return "redirect:/agents/llista";
    }
}