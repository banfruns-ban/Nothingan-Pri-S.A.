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
                    "Compacte", 500, "35.00", "1000.00", 1, 10, "DISPONIBLE", true, null);

            crea(repo, "4821MNB", "Toyota", "Corolla", 2023, "Híbrid eficient i silenciós per a ciutat i carretera",
                    "Turisme", 400, "19.50", "450.00", 1, 21, "DISPONIBLE", true, "Pneumàtics nous");

            crea(repo, "7394KLP", "Peugeot", "208", 2022, "Compacte àgil amb baix consum",
                    "Compacte", 300, "13.00", "300.00", 1, 15, "DISPONIBLE", true, null);

            crea(repo, "1958RST", "Renault", "Clio", 2021, "Utilitari pràctic i fàcil d'aparcar",
                    "Compacte", 300, "12.00", "300.00", 1, 14, "DISPONIBLE", true, null);

            crea(repo, "6204BVG", "Hyundai", "Tucson", 2024, "Vehicle espaiós i confortable per a família",
                    "Turisme", 450, "24.00", "600.00", 1, 25, "DISPONIBLE", true, "Assegurança a tot risc");

            crea(repo, "8519CDF", "Kia", "Niro", 2023, "Híbrid endollable amb etiqueta zero emissions",
                    "Turisme", 400, "22.50", "550.00", 1, 20, "DISPONIBLE", true, null);

            crea(repo, "3047HJK", "Mercedes-Benz", "Sprinter", 2021, "Furgoneta industrial de gran capacitat",
                    "Furgoneta", 300, "29.00", "850.00", 2, 30, "DISPONIBLE", true, "Revisió ITV vigent");

            crea(repo, "9182LMN", "Audi", "A3", 2022, "Compacte prèmium amb acabats esportius",
                    "Compacte", 400, "26.00", "700.00", 1, 15, "DISPONIBLE", true, null);

            crea(repo, "2673PRV", "BMW", "Sèrie 1", 2023, "Conducció dinàmica i equipament complet",
                    "Compacte", 400, "27.50", "750.00", 1, 15, "DISPONIBLE", true, null);

            crea(repo, "5490STV", "Citroën", "Berlingo", 2020, "Furgoneta lleugera versàtil per a repartiment",
                    "Furgoneta", 300, "20.00", "500.00", 1, 30, "DISPONIBLE", true, null);

            crea(repo, "7812WXZ", "Dacia", "Sandero", 2022, "Opció econòmica i fiable per a desplaçaments diaris",
                    "Compacte", 300, "10.50", "250.00", 1, 21, "DISPONIBLE", true, null);

            crea(repo, "1465BCD", "Cupra", "Formentor", 2024, "Model esportiu amb altes prestacions",
                    "Turisme", 450, "32.00", "900.00", 1, 14, "DISPONIBLE", true, "Només conductors majors de 25 anys");

            crea(repo, "8930FGH", "Opel", "Corsa", 2021, "Compacte urbà de baix manteniment",
                    "Compacte", 300, "11.50", "300.00", 1, 14, "MANTENIMENT", false, "Revisió d'oli i filtres");

            crea(repo, "4127JKL", "Nissan", "Qashqai", 2022, "Turisme ampli amb gran maleter",
                    "Turisme", 400, "21.00", "550.00", 1, 21, "DISPONIBLE", true, null);

            crea(repo, "6753MNP", "Fiat", "Ducato", 2019, "Furgoneta de transport per a mudances",
                    "Furgoneta", 250, "24.00", "700.00", 2, 30, "DISPONIBLE", true, null);

            crea(repo, "2389RTV", "Skoda", "Octavia", 2023, "Berlina familiar amb molt d'espai interior",
                    "Turisme", 450, "18.50", "450.00", 1, 25, "DISPONIBLE", true, null);

            crea(repo, "9541VWX", "Toyota", "Yaris", 2024, "Compacte híbrid ideal per a trànsit urbà",
                    "Compacte", 350, "14.00", "350.00", 1, 14, "DISPONIBLE", true, null);
        };
    }

    private void crea(VehiclesRepository repo, String matricula, String marca, String model, Integer any,
            String descripcio, String tipus, Integer limitKm,
            String preuHora, String fianca, Integer minDies, Integer maxDies,
            String estat, Boolean actiu, String comentaris) {
        if (repo.existsById(matricula)) {
            return;
        }

        VehiclesEntity v = new VehiclesEntity(matricula, marca, model, any, descripcio, tipus,
                limitKm, new BigDecimal(preuHora), new BigDecimal(fianca),
                minDies, maxDies, estat, actiu, comentaris);
        repo.save(v);
    }
}
