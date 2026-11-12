package cat.copernic.projecte1Grup3.Security;

import java.util.Optional;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.AgentEntity;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Repository.AgentRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Entity.UsuariEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepository;

@Service
public class UsuariDetailsService implements UserDetailsService {

    private final AgentRepository agentRepository;
    private final ClientRepository clienRepository;

    public UsuariDetailsService(AgentRepository agentRepository,
                                ClientRepository clientRepository) {
        this.agentRepository = agentRepository;
        this.clienRepository = clientRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<AgentEntity> agent = agentRepository.findByEmailIgnoreCase(email);
        if (agent.isPresent()) {
            return construir(agent.get(), agent.get().getRol().name());
        }

        ClientEntity client = clienRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuari no trobat: " + email));
        return construir(client, "CLIENT");
    }

    private UserDetails construir(UsuariEntity usuari, String rol) {
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