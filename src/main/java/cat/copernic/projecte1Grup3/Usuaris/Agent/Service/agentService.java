package cat.copernic.projecte1Grup3.Usuaris.Agent.Service;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.agentEntity;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Repository.agentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class agentService {

    @Autowired
    private agentRepository agentRepository;

    public void guardarAgent(agentEntity agent) {
        agentRepository.save(agent);
    }

    // RF13: Llistar agents
    public List<agentEntity> llistarAgents() {
        return agentRepository.findAll();
    }

    // RF14 / RF15: Buscar per DNI / Consultar detall
    public Optional<agentEntity> obtenirPerDni(String dni) {
        return agentRepository.findById(dni);
    }

    public void eliminarAgent(String dni) {
        agentRepository.deleteById(dni);
    }
    
    /**
     * Llista tots els agents o els filtra si s'ha introduït un text de cerca.
     */
    public List<agentEntity> llistarOFiltrarAgents(String cerca) {
        if (cerca == null || cerca.trim().isEmpty()) {
            return agentRepository.findAll();
        }
        return agentRepository.cercarPerDniNomEmail(cerca.trim());
    }
}