package cat.copernic.projecte1Grup3.Usuaris.Agent.Dades;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.AgentEntity;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.Rol;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Repository.AgentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DadesAgents {

    @Bean
    CommandLineRunner carregarAgents(AgentRepository repo, PasswordEncoder passwordEncoder) {
        return args -> {
            crea(repo, passwordEncoder, "11111111H", "Laura Vidal Roca", "laura.agent@test.com", "08:00", "16:00", Rol.ADMIN);
            crea(repo, passwordEncoder, "22222222J", "Marc Ferrer Costa", "marc.agent@test.com", "09:00", "17:00", Rol.AGENT);
            crea(repo, passwordEncoder, "33333333P", "Núria Serra Pons", "nuria.agent@test.com", "14:00", "22:00", Rol.AGENT);
        };
    }

    private void crea(AgentRepository repo, PasswordEncoder passwordEncoder,
                      String dni, String nom, String email,
                      String horaEntrada, String horaSortida, Rol rol) {
        AgentEntity existing = repo.findById(dni).orElse(null);
        if (existing != null) {
            String password = existing.getPassword();
            if (password == null || password.isBlank()) {
                existing.setPassword(passwordEncoder.encode("password123"));
                repo.save(existing);
            } else if (!esContrasenyaBcrypt(password)) {
                existing.setPassword(passwordEncoder.encode(password));
                repo.save(existing);
            }
            return;
        }

        AgentEntity a = new AgentEntity();
        a.setDni(dni);
        a.setNom(nom);
        a.setEmail(email);
        a.setPassword(passwordEncoder.encode("password123"));
        a.setHora_entrada(horaEntrada);
        a.setHora_sortida(horaSortida);
        a.setRol(rol);
        repo.save(a);
    }

    private boolean esContrasenyaBcrypt(String password) {
        return password.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    }
}