package cat.copernic.projecte1Grup3.Usuaris.Agent.Dades;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.AgentEntity;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.Rol;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Repository.AgentRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DadesAgents {

    @Bean
    CommandLineRunner carregarAgents(AgentRepository repo, PasswordEncoder passwordEncoder) {
        return args -> {
            // 4 Administradors (contrasenya: 12345678)
            crea(repo, passwordEncoder, "45936880S", "Hugo López Soldado", "h@test.com", "12345678", "08:00", "14:30", Rol.ADMIN);
            crea(repo, passwordEncoder, "X8392014G", "Brais Anfruns Rodriguez", "b@test.com", "12345678", "08:00", "14:30", Rol.ADMIN);
            crea(repo, passwordEncoder, "53918274H", "Izan Lazaro Egea", "i@test.com", "12345678", "08:00", "14:30", Rol.ADMIN);
            crea(repo, passwordEncoder, "Y4729183Y", "Yuri Andrei Martinello", "y@test.com", "12345678", "08:00", "14:30", Rol.ADMIN);

            // 16 Agents
            crea(repo, passwordEncoder, "41283947M", "Laura Vidal Roca", "laura.agent@test.com", "FsSNLQnI6p", "08:00", "16:00", Rol.AGENT);
            crea(repo, passwordEncoder, "48920134P", "Marc Ferrer Costa", "marc.agent@test.com", "DQxCQs7Nt0", "09:00", "17:00", Rol.AGENT);
            crea(repo, passwordEncoder, "X3948172S", "Núria Serra Pons", "nuria.agent@test.com", "f3PJLJmZE9", "14:00", "22:00", Rol.AGENT);
            crea(repo, passwordEncoder, "39284710C", "Pau Martí Soler", "pau.agent@test.com", "WixINFj3Mi", "08:00", "14:30", Rol.AGENT);
            crea(repo, passwordEncoder, "52839104P", "Carla Puig Rovira", "carla.agent@test.com", "4Js8KuE0K7", "08:00", "16:00", Rol.AGENT);
            crea(repo, passwordEncoder, "Y6182934L", "Oriol Camps Torres", "oriol.agent@test.com", "H8ZS4KsUs5", "09:00", "17:00", Rol.AGENT);
            crea(repo, passwordEncoder, "46392018Y", "Mireia Bosch Giménez", "mireia.agent@test.com", "vr4TbosEjZ", "14:00", "22:00", Rol.AGENT);
            crea(repo, passwordEncoder, "38492017E", "Pol Navarro Ruiz", "pol.agent@test.com", "HVD2AcfNuB", "08:00", "14:30", Rol.AGENT);
            crea(repo, passwordEncoder, "Z1928374J", "Alba Mora Castillo", "alba.agent@test.com", "87y3f56QCM", "08:00", "16:00", Rol.AGENT);
            crea(repo, passwordEncoder, "51928374D", "Arnau Riera Mas", "arnau.agent@test.com", "2nItmrVlw4", "09:00", "17:00", Rol.AGENT);
            crea(repo, passwordEncoder, "43829104M", "Laia Fonts Esteve", "laia.agent@test.com", "eeTSfO903a", "14:00", "22:00", Rol.AGENT);
            crea(repo, passwordEncoder, "X7283910V", "Biel Duran Ortiz", "biel.agent@test.com", "3AF6GjIu5I", "08:00", "14:30", Rol.AGENT);
            crea(repo, passwordEncoder, "49283716Y", "Júlia Valls Moreno", "julia.agent@test.com", "63xfdu0Aj8", "08:00", "16:00", Rol.AGENT);
            crea(repo, passwordEncoder, "37192845M", "Jan Sala Domínguez", "jan.agent@test.com", "1cf5LC9aX3", "09:00", "17:00", Rol.AGENT);
            crea(repo, passwordEncoder, "Y2839471C", "Clàudia Prat Iglesias", "claudia.agent@test.com", "SPt9sOc3tl", "14:00", "22:00", Rol.AGENT);
            crea(repo, passwordEncoder, "54829103R", "Nil Casanovas Grau", "nil.agent@test.com", "6NRrwwkMTI", "08:00", "14:30", Rol.AGENT);
        };
    }

    private void crea(AgentRepository repo, PasswordEncoder passwordEncoder,
            String dni, String nom, String email, String password,
            String horaEntrada, String horaSortida, Rol rol) {
        if (repo.existsById(dni)) {
            return;
        }

        String pwdFinal = (password == null || password.isBlank()) ? "password123" : password;

        AgentEntity a = new AgentEntity();
        a.setDni(dni);
        a.setNom(nom);
        a.setEmail(email);
        a.setPassword(passwordEncoder.encode(pwdFinal));
        a.setDataNaixement(LocalDate.of(1995, 6, 15));
        a.setHora_entrada(horaEntrada);
        a.setHora_sortida(horaSortida);
        a.setRol(rol);
        repo.save(a);
    }
}
