package cat.copernic.projecte1Grup3.Vehicles.Dades;

import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Repository.VehiclesRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DadesVehicles {

    @Bean
    CommandLineRunner carregarVehicles(VehiclesRepository repo) {
        return args -> {
            crea(repo, "1234ABC", "Seat", "Ibiza", 2021, "Utilitari compacte, ideal per a ciutat",
                 "Turisme", 300, "12.50", "300.00", 1, 14, "DISPONIBLE", true, "Revisió feta al març");

            crea(repo, "5678DEF", "Volkswagen", "Golf", 2022, "Berlina còmoda per a viatges llargs",
                 "Turisme", 400, "18.00", "500.00", 1, 21, "DISPONIBLE", true, null);

            crea(repo, "9012GHI", "Ford", "Transit", 2020, "Furgoneta de càrrega gran",
                 "Furgoneta", 250, "25.00", "700.00", 2, 30, "MANTENIMENT", false, "Pendent canvi de frens");

            crea(repo, "3456JKL", "Tesla", "Model 3", 2023, "Elèctric amb gran autonomia",
                 "Elèctric", 500, "35.00", "1000.00", 1, 10, "DISPONIBLE", true, null);
        };
    }

    private void crea(VehiclesRepository repo, String matricula, String marca, String model, Integer any,
                      String descripcio, String tipus, Integer limitKm,
                      String preuHora, String fianca, Integer minDies, Integer maxDies,
                      String estat, Boolean actiu, String comentaris) {
        if (repo.existsById(matricula)) return;

        VehiclesEntity v = new VehiclesEntity(matricula, marca, model, any, descripcio, tipus,
                limitKm, new BigDecimal(preuHora), new BigDecimal(fianca),
                minDies, maxDies, estat, actiu, comentaris);
        repo.save(v);
    }
}