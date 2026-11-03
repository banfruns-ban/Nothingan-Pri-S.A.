package cat.copernic.projecte1Grup3.Reserves.Service;

import cat.copernic.projecte1Grup3.Reserves.Entity.reservesEntity;
import cat.copernic.projecte1Grup3.Reserves.Repository.reservesRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.clientRepository;
import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Repository.VehiclesRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class reservesService {

    public static final String PENDENT = "PENDENT";
    public static final String CONFIRMADA = "CONFIRMADA";
    public static final String EN_CURS = "EN_CURS";
    public static final String FINALITZADA = "FINALITZADA";
    public static final String CANCELADA = "CANCELADA";

    private final reservesRepository reservesRepository;
    private final clientRepository clientRepository;
    private final VehiclesRepository vehiclesRepository;

    public reservesService(reservesRepository reservesRepository,
            clientRepository clientRepository,
            VehiclesRepository vehiclesRepository) {
        this.reservesRepository = reservesRepository;
        this.clientRepository = clientRepository;
        this.vehiclesRepository = vehiclesRepository;
    }

    // Recoge los vehiculos activos para saber cuales vehiculos se pueden reservar
    public List<VehiclesEntity> vehiclesActius() {
        return vehiclesRepository.findAll().stream()
                .filter(v -> Boolean.TRUE.equals(v.getActiu()))
                .toList();
    }

    public List<reservesEntity> llistar(String q, String dni, String email,
            LocalDate dataReserva, String matricula,
            String codi, String estat) {
        List<reservesEntity> reserves = reservesRepository.findAll();

        // 1. Cerca general: codi, nom/DNI del client, matrícula/marca/model del vehicle
        if (q != null && !q.isBlank()) {
            String text = q.trim().toLowerCase();
            reserves = reserves.stream()
                    .filter(r -> conte(String.valueOf(r.getCodiReserva()), text)
                            || (r.getClient() != null
                            && (conte(r.getClient().getNom(), text) || conte(r.getClient().getDni(), text)))
                            || (r.getVehicle() != null
                            && (conte(r.getVehicle().getMatricula(), text)
                            || conte(r.getVehicle().getMarca(), text)
                            || conte(r.getVehicle().getModel(), text))))
                    .toList();
        }

        // 2. Filtre per DNI
        if (dni != null && !dni.isBlank()) {
            String d = dni.trim().toLowerCase();
            reserves = reserves.stream()
                    .filter(r -> r.getClient() != null && conte(r.getClient().getDni(), d))
                    .toList();
        }

        // 3. Email del client
        if (email != null && !email.isBlank()) {
            String e = email.trim().toLowerCase();
            reserves = reserves.stream()
                    .filter(r -> r.getClient() != null && conte(r.getClient().getEmail(), e))
                    .toList();
        }

        // 4. Data de reserva: reserves actives aquell dia (dataInici <= dia <= dataFi)
        if (dataReserva != null) {
            reserves = reserves.stream()
                    .filter(r -> r.getDataInici() != null && r.getDataFi() != null
                            && !dataReserva.isBefore(r.getDataInici())
                            && !dataReserva.isAfter(r.getDataFi()))
                    .toList();
        }

        // 5. Matrícula del vehicle
        if (matricula != null && !matricula.isBlank()) {
            String m = matricula.trim().toLowerCase();
            reserves = reserves.stream()
                    .filter(r -> r.getVehicle() != null && conte(r.getVehicle().getMatricula(), m))
                    .toList();
        }

        // 6. Codi de reserva (exacte)
        if (codi != null && !codi.isBlank()) {
            String c = codi.trim();
            reserves = reserves.stream()
                    .filter(r -> String.valueOf(r.getCodiReserva()).equals(c))
                    .toList();
        }

        // 7. Estat
        if (estat != null && !estat.isBlank()) {
            reserves = reserves.stream()
                    .filter(r -> r.getClient() != null
                            && r.getClient().getDni() != null
                            && r.getClient().getDni().toUpperCase().contains(q))
                    .toList();
        }

        // 8. Les més noves primer
        return reserves.stream()
                .sorted(Comparator.comparing(reservesEntity::getCodiReserva).reversed())
                .toList();
    }

    private boolean conte(String valor, String text) {
        return valor != null && valor.toLowerCase().contains(text);
    }
        
        public Optional<reservesEntity> obtenir(Long codi) {
    return reservesRepository.findById(codi); // usa el nombre real de tu repositorio
}

    @Transactional //<--si algo falla, no se guarda nada.
    public reservesEntity crear(String dni, String matricula,
            LocalDate dataInici, LocalTime horaInici,
            LocalDate dataFi, LocalTime horaFi,
            String observacions) {

        if (dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("Cal indicar el client");
        }
        if (matricula == null || matricula.isBlank()) {
            throw new IllegalArgumentException("Cal triar un vehicle");
        }
        if (dataInici == null || horaInici == null || dataFi == null || horaFi == null) {
            throw new IllegalArgumentException("Cal indicar les dates i hores d'inici i fi");
        }

        // 2. El client existeix i  no està validat
        clientEntity client = clientRepository.findById(dni.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));
        if (!"VALIDAT".equals(client.getEstatValidacio())) {
            throw new IllegalArgumentException(
                    "El client ha d'estar validat per poder fer una reserva");
        }

        // 3. El vehicle existeix i està actiu
        VehiclesEntity vehicle = vehiclesRepository.findById(matricula)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle no trobat"));
        if (!Boolean.TRUE.equals(vehicle.getActiu())) { //<-- si vehiculo NO en lista de activos
            throw new IllegalArgumentException("Aquest vehicle no està disponible per a reserves");
        }

        // 4. Dates coherents
        LocalDateTime inici = LocalDateTime.of(dataInici, horaInici);
        LocalDateTime fi = LocalDateTime.of(dataFi, horaFi);
        if (inici.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("L'inici de la reserva no pot ser en el passat");
        }
        if (!fi.isAfter(inici)) {
            throw new IllegalArgumentException("La data i hora de fi han de ser posteriors a les d'inici");
        }

        // 5. Durada: es compta en hores senceres (les hores parcials s'arrodoneixen amunt)
        long minuts = Duration.between(inici, fi).toMinutes();
        long hores = (minuts + 59) / 60;
        long dies = (hores + 23) / 24;
        if (dies < vehicle.getMinDiesLloguer()) {
            throw new IllegalArgumentException(
                    "La durada mínima de lloguer és de " + vehicle.getMinDiesLloguer() + " dia(es)");
        }
        if (dies > vehicle.getMaxDiesLloguer()) {
            throw new IllegalArgumentException(
                    "La durada màxima de lloguer és de " + vehicle.getMaxDiesLloguer() + " dia(es)");
        }

        // 6. El vehicle no pot estar ja reservat en aquest interval
        List<reservesEntity> existents
                = reservesRepository.findByVehicleMatriculaAndEstatNot(matricula, CANCELADA);
        for (reservesEntity r : existents) {
            LocalDateTime iniciR = LocalDateTime.of(r.getDataInici(), r.getHoraInici());
            LocalDateTime fiR = LocalDateTime.of(r.getDataFi(), r.getHoraFi());
            boolean esSolapa = iniciR.isBefore(fi) && fiR.isAfter(inici);
            if (esSolapa) {
                throw new IllegalArgumentException(
                        "El vehicle ja està reservat en aquest període");
            }
        }

        // 7. Valors que decideix el servidor, no el formulari
        reservesEntity reserva = new reservesEntity();
        reserva.setClient(client);
        reserva.setVehicle(vehicle);
        reserva.setDataInici(dataInici);
        reserva.setHoraInici(horaInici);
        reserva.setDataFi(dataFi);
        reserva.setHoraFi(horaFi);
        reserva.setCostTotal(vehicle.getPreuHora()
                .multiply(BigDecimal.valueOf(hores))
                .setScale(2, RoundingMode.HALF_UP));
        reserva.setFianca(vehicle.getFianca());
        reserva.setEstat(PENDENT);
        reserva.setObservacions(observacions == null || observacions.isBlank()
                ? null : observacions.trim());

        return reservesRepository.save(reserva);
    }
    
     @Transactional
    public void eliminar(Long codi) {
        reservesEntity r = reservesRepository.findById(codi)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no trobada"));
 
        if (EN_CURS.equals(r.getEstat()) || FINALITZADA.equals(r.getEstat())) {
            throw new IllegalArgumentException(
                    "No es pot eliminar una reserva en curs o finalitzada");
        }
        reservesRepository.delete(r);
    }
}

