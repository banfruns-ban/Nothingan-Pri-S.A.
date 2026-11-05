/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Client.Dades;

import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.clientRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author User
 */
@Configuration
public class DadesProva {

    @Bean
    CommandLineRunner carregarClients(clientRepository repo) {
        return args -> {
            crea(repo, "12345678Z", "Joan Garcia Soler", "joan@test.com", "612345678",
                 LocalDate.of(1990, 5, 14), "Espanyola", "Carrer Major 12, Terrassa", "B",
                 LocalDate.of(2030, 3, 1), LocalDate.of(2032, 7, 20), "VALIDAT", true);
        };
        
        
        
        
    }
    
    private void crea(clientRepository repo, String dni, String nom, String email, String tel,
                      LocalDate naix, String nac, String adreca, String llicencia,
                      LocalDate capDni, LocalDate capCarnet, String estat, boolean premium) {
        if (repo.existsById(dni)) return;

        clientEntity c = new clientEntity();
        c.setDni(dni);
        c.setNom(nom);
        c.setEmail(email);
        c.setPassword("password123");   // ver nota abajo
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
}
        

