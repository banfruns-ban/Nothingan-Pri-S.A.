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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Controlador Spring MVC per a la gestió d'agents.
 * S'encarrega de gestionar les peticions HTTP relacionades amb el registre,
 * llistat, edició i detall dels agents (RF10, RF12, RF13, RF14, RF15).
 * 
 * @author Grup 3
 */
@Controller
@RequestMapping("/agents")
public class agentController {

    private final agentService agentService;

    // Injecció de dependència per constructor
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
     * Processa el formulari d'alta i edició gestionant validacions, la pujada de la foto
     * i el control de duplicats a la base de dades.
     */
    @PostMapping("/alta")
    public String processarAlta(@Valid @ModelAttribute("agent") agentEntity agent,
                                BindingResult result,
                                @RequestParam("fitxerFoto") MultipartFile fitxerFoto,
                                Model model) {

        // 1. Validació de camps amb Bean Validation (@Valid)
        if (result.hasErrors()) {
            return "agents/alta_agent";
        }

        // 2. Gestió de la foto de perfil si s'ha adjuntat un fitxer
        if (!fitxerFoto.isEmpty()) {
            try {
                String nomFitxer = agent.getDni() + "_" + fitxerFoto.getOriginalFilename();
                Path ruta = Paths.get("uploads/" + nomFitxer);
                Files.createDirectories(ruta.getParent());
                Files.write(ruta, fitxerFoto.getBytes());
                agent.setFoto(nomFitxer);
            } catch (IOException e) {
                model.addAttribute("error", "Error en pujar la foto del perfil.");
                return "agents/alta_agent";
            }
        }

        // 3. Desa l'agent a la BD controlant si el DNI o email ja existeixen
        try {
            agentService.guardarAgent(agent);
            return "redirect:/agents/llista?exit";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "Ja existeix un usuari amb aquest DNI o correu electrònic.");
            return "agents/alta_agent";
        }
    }

    /**
     * RF13: Mostra la llista completa d'agents registrats al sistema.
     * Mapeja /agents, /agents/ i /agents/llista per evitar errors 404.
     */
    @GetMapping({"", "/", "/llista"})
    public String llistarAgents(Model model) {
        // Obtenim la llista de la BD i la passem al model
        List<agentEntity> llista = agentService.llistarAgents();
        model.addAttribute("agents", llista);
        
        // Retorna exactament "agents/llistat_agents" coincidint amb el fitxer llistat_agents.html
        return "agents/llistat_agents";
    }

    /**
     * RF14 & RF15: Consulta els detalls d'un agent concret a partir del seu DNI.
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
}