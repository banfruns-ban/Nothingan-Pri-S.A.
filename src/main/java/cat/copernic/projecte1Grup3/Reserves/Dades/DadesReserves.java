/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Reserves.Dades;

import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Repository.VehiclesRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import cat.copernic.projecte1Grup3.Reserves.Repository.ReservesRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepository;

@Configuration
public class DadesReserves {

    @Bean
    @Order(10) // s'executa després dels carregadors de clients i vehicles
    CommandLineRunner carregarReserves(ReservesRepository reservesRepo,
                                       ClientRepository clientRepo,
                                       VehiclesRepository vehicleRepo) {
        return args -> {
            // Només es carrega si encara no hi ha reserves (evita duplicats en reiniciar)
            if (reservesRepo.count() > 1) return;

            crea(reservesRepo, clientRepo, vehicleRepo,
                 "12345678Z", "1234ABC",
                 LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12),
                 LocalTime.of(10, 0), LocalTime.of(10, 0),
                 "37.50", "300.00", "CONFIRMADA", "Client habitual");

            crea(reservesRepo, clientRepo, vehicleRepo,
                 "87654321X", "5678DEF",
                 LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 20),
                 LocalTime.of(9, 30), LocalTime.of(18, 0),
                 "90.00", "500.00", "PENDENT", null);

            crea(reservesRepo, clientRepo, vehicleRepo,
                 "X1234567L", "3456JKL",
                 LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3),
                 LocalTime.of(8, 0), LocalTime.of(20, 0),
                 "70.00", "1000.00", "FINALITZADA", "Retornat sense incidències");
        };
    }

    private void crea(ReservesRepository reservesRepo, ClientRepository clientRepo,
                      VehiclesRepository vehicleRepo, String dni, String matricula,
                      LocalDate dataInici, LocalDate dataFi,
                      LocalTime horaInici, LocalTime horaFi,
                      String costTotal, String fianca, String estat, String observacions) {

        ClientEntity client = clientRepo.findById(dni).orElse(null);
        VehiclesEntity vehicle = vehicleRepo.findById(matricula).orElse(null);
        if (client == null || vehicle == null) return; // falta client o vehicle: s'omet

        ReservesEntity r = new ReservesEntity();
        r.setClient(client);
        r.setVehicle(vehicle);
        r.setDataInici(dataInici);
        r.setDataFi(dataFi);
        r.setHoraInici(horaInici);
        r.setHoraFi(horaFi);
        r.setCostTotal(new BigDecimal(costTotal));
        r.setFianca(new BigDecimal(fianca));
        r.setEstat(estat);
        r.setObservacions(observacions);
        if ("FINALITZADA".equals(estat)) {
            r.setDataLliurament(dataInici);
            r.setHoraLliurament(horaInici);
            r.setDataRetorn(dataFi);
        }
        reservesRepo.save(r);
    }
}