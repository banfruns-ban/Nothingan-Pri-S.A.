package cat.copernic.projecte1Grup3.Usuaris.Agent.Dades;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.AgentEntity;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.Rol;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Repository.AgentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DadesAgents {

    @Bean
    CommandLineRunner carregarAgents(AgentRepository repo) {
        return args -> {
            crea(repo, "11111111H", "Laura Vidal Roca", "laura.agent@test.com", "08:00", "16:00", Rol.ADMIN);
            crea(repo, "22222222J", "Marc Ferrer Costa", "marc.agent@test.com", "09:00", "17:00", Rol.AGENT);
            crea(repo, "33333333P", "Núria Serra Pons", "nuria.agent@test.com", "14:00", "22:00", Rol.AGENT);
        };
    }

    private void crea(AgentRepository repo, String dni, String nom, String email,
                      String horaEntrada, String horaSortida, Rol rol) {
        if (repo.existsById(dni)) return;

        AgentEntity a = new AgentEntity();
        a.setDni(dni);
        a.setNom(nom);
        a.setEmail(email);
        a.setPassword("password123");
        a.setHora_entrada(horaEntrada);
        a.setHora_sortida(horaSortida);
        a.setRol(rol);
        repo.save(a);
    }
}