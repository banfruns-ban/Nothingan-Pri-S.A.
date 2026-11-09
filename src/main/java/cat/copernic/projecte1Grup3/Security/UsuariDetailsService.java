package cat.copernic.projecte1Grup3.Security;

import java.util.Optional;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.AgentEntity;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Repository.AgentRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.clientRepository;
import cat.copernic.projecte1Grup3.Usuaris.Entity.usuariEntity;

@Service
public class UsuariDetailsService implements UserDetailsService {

    private final AgentRepository agentRepository;
    private final clientRepository clienRepository;

    public UsuariDetailsService(AgentRepository agentRepository,
                                clientRepository clientRepository) {
        this.agentRepository = agentRepository;
        this.clienRepository = clientRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<AgentEntity> agent = agentRepository.findByEmailIgnoreCase(email);
        if (agent.isPresent()) {
            return construir(agent.get(), agent.get().getRol().name());
        }

        clientEntity client = clienRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuari no trobat: " + email));
        return construir(client, "CLIENT");
    }

    private UserDetails construir(usuariEntity usuari, String rol) {
        if (usuari.getPassword() == null || usuari.getPassword().isBlank()) {
            throw new UsernameNotFoundException(
                    "L'usuari no té contrasenya configurada: " + usuari.getEmail());
        }
        return User.builder()
                .username(usuari.getEmail())
                .password(usuari.getPassword())
                .roles(rol)
                .build();
    }
}