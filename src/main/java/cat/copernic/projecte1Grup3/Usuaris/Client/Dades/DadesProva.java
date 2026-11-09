package cat.copernic.projecte1Grup3.Usuaris.Client.Dades;

import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.clientRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DadesProva {

    @Bean
    CommandLineRunner carregarClients(clientRepository repo, PasswordEncoder passwordEncoder) {
        return args -> {
            crea(repo, passwordEncoder, "12345678Z", "Joan Garcia Soler", "joan@test.com", "612345678",
                 LocalDate.of(1990, 5, 14), "Espanyola", "Carrer Major 12, Terrassa", "B",
                 LocalDate.of(2030, 3, 1), LocalDate.of(2032, 7, 20), "VALIDAT", true);
        };
    }

    private void crea(clientRepository repo, PasswordEncoder passwordEncoder,
                      String dni, String nom, String email, String tel,
                      LocalDate naix, String nac, String adreca, String llicencia,
                      LocalDate capDni, LocalDate capCarnet, String estat, boolean premium) {
        clientEntity existing = repo.findById(dni).orElse(null);
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

        clientEntity c = new clientEntity();
        c.setDni(dni);
        c.setNom(nom);
        c.setEmail(email);
        c.setPassword(passwordEncoder.encode("password123"));
        c.setTelefon(tel);
        c.setDataNaixement(naix);
        c.setNacionalitat(nac);
        c.setAdreca(adreca);
        c.setTipusLlicencia(llicencia);
        c.setDataCaducitatDNI(capDni);
        c.setDataCaducitatCarnet(capCarnet);
        c.setEstatValidacio(estat);
        c.setPremium(premium);
        repo.save(c);
    }

    private boolean esContrasenyaBcrypt(String password) {
        return password.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    }
}

