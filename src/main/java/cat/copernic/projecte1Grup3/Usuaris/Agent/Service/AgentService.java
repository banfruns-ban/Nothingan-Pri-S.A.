package cat.copernic.projecte1Grup3.Usuaris.Agent.Service;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.AgentEntity;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Repository.AgentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    private final PasswordEncoder passwordEncoder;
    private final AgentRepository agentRepository;

    public AgentService(PasswordEncoder passwordEncoder, AgentRepository agentRepository) {
        this.passwordEncoder = passwordEncoder;
        this.agentRepository = agentRepository;
    }

    public void guardarAgent(AgentEntity agent) {
        String dni = agent.getDni() == null ? "" : agent.getDni().trim().toUpperCase();
        if (!esDniValid(dni)) {
            throw new IllegalArgumentException(
                    "El DNI/NIE no és vàlid (ha de tenir 8 xifres i la lletra correcta, o ser un NIE vàlid)");
        }
        agent.setDni(dni);

        if (agent.getDataNaixement() == null) {
            throw new IllegalArgumentException("La data de naixement és obligatòria");
        }
        if (agent.getDataNaixement().isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("L'agent ha de tenir com a mínim 18 anys");
        }

        Optional<AgentEntity> existent = agentRepository.findById(dni);
        if (existent.isPresent()) {
            String passwordGuardada = existent.get().getPassword();
            String passwordRebuda = agent.getPassword();
            if (passwordRebuda == null || passwordRebuda.isBlank()
                    || passwordRebuda.equals(passwordGuardada)) {
                agent.setPassword(passwordGuardada);
            } else {
                agent.setPassword(passwordEncoder.encode(passwordRebuda));
            }
        } else if (agent.getPassword() != null && !agent.getPassword().isBlank()) {
            agent.setPassword(passwordEncoder.encode(agent.getPassword()));
        }

        agentRepository.save(agent);
        log.info("Agent donat d'alta: {}", agent);
    }

    private boolean esDniValid(String dni) {
        if (!dni.matches("^([0-9]{8}|[XYZ][0-9]{7})[A-Z]$")) {
            return false;
        }
        String numeroStr = dni.substring(0, 8)
                .replace("X", "0")
                .replace("Y", "1")
                .replace("Z", "2");
        int numero = Integer.parseInt(numeroStr);
        String lletresValides = "TRWAGMYFPDXBNJZSQVHLCKE";
        return dni.charAt(8) == lletresValides.charAt(numero % 23);
    }

    public List<AgentEntity> llistarAgents() {
        return agentRepository.findAll();
    }

    public Optional<AgentEntity> obtenirPerDni(String dni) {
        return agentRepository.findById(dni);
    }

    public void eliminarAgent(String dni) {
        agentRepository.deleteById(dni);
        log.warn("Agent eliminat: {}", dni);
    }

    public List<AgentEntity> llistarOFiltrarAgents(String cerca) {
        if (cerca == null || cerca.trim().isEmpty()) {
            return agentRepository.findAll();
        }
        return agentRepository.cercarPerDniNomEmail(cerca.trim());
    }
}
