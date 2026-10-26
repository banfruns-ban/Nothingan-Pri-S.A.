package cat.copernic.projecte1Grup3.Usuaris.Agent.Service;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.agentEntity;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Repository.agentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servei de negoci per als Agents.
 */
@Service
public class agentService {

    @Autowired // Le endica a Spring que utilice la instancia
    private agentRepository agentRepository;

    /**
     * Desar o actualitzar un agent a la base de dades.
     */
    public void guardarAgent(agentEntity agent) {
        agentRepository.save(agent);
    }
}

// crear validaciones del alta de los agentes