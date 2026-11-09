package cat.copernic.projecte1Grup3.Reserves.Service;

import cat.copernic.projecte1Grup3.Reserves.Entity.reservesEntity;
import cat.copernic.projecte1Grup3.Reserves.Repository.reservesRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.clientRepository;
import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Repository.VehiclesRepository;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    public List<VehiclesEntity> vehiclesActius() {
        return vehiclesRepository.findAll().stream()
                .filter(v -> Boolean.TRUE.equals(v.getActiu()))
                .toList();
    }

    public List<reservesEntity> llistar(String q, String dni, String email,
            LocalDate dataReserva, String matricula,
            String codi, String estat) {
        List<reservesEntity> reserves = reservesRepository.findAll();

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

        if (dni != null && !dni.isBlank()) {
            String d = dni.trim().toLowerCase();
            reserves = reserves.stream()
                    .filter(r -> r.getClient() != null && conte(r.getClient().getDni(), d))
                    .toList();
        }

        if (email != null && !email.isBlank()) {
            String e = email.trim().toLowerCase();
            reserves = reserves.stream()
                    .filter(r -> r.getClient() != null && conte(r.getClient().getEmail(), e))
                    .toList();
        }

        if (dataReserva != null) {
            reserves = reserves.stream()
                    .filter(r -> r.getDataInici() != null && r.getDataFi() != null
                    && !dataReserva.isBefore(r.getDataInici())
                    && !dataReserva.isAfter(r.getDataFi()))
                    .toList();
        }

        if (matricula != null && !matricula.isBlank()) {
            String m = matricula.trim().toLowerCase();
            reserves = reserves.stream()
                    .filter(r -> r.getVehicle() != null && conte(r.getVehicle().getMatricula(), m))
                    .toList();
        }

        if (codi != null && !codi.isBlank()) {
            String c = codi.trim();
            reserves = reserves.stream()
                    .filter(r -> String.valueOf(r.getCodiReserva()).equals(c))
                    .toList();
        }

        if (estat != null && !estat.isBlank()) {
            String e = estat.trim();
            reserves = reserves.stream()
                    .filter(r -> e.equalsIgnoreCase(r.getEstat()))
                    .toList();
        }

        return reserves.stream()
                .sorted(Comparator.comparing(reservesEntity::getCodiReserva).reversed())
                .toList();
    }

    private boolean conte(String valor, String text) {
        return valor != null && valor.toLowerCase().contains(text);
    }

    public Optional<reservesEntity> obtenir(Long codi) {
        return reservesRepository.findById(codi);
    }

    @Transactional
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

        clientEntity client = clientRepository.findById(dni.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));
        if (!"VALIDAT".equals(client.getEstatValidacio())) {
            throw new IllegalArgumentException(
                    "El client ha d'estar validat per poder fer una reserva");
        }

        VehiclesEntity vehicle = vehiclesRepository.findById(matricula)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle no trobat"));
        if (!Boolean.TRUE.equals(vehicle.getActiu())) {
            throw new IllegalArgumentException("Aquest vehicle no està disponible per a reserves");
        }

        LocalDateTime inici = LocalDateTime.of(dataInici, horaInici);
        LocalDateTime fi = LocalDateTime.of(dataFi, horaFi);
        if (inici.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("L'inici de la reserva no pot ser en el passat");
        }
        if (!fi.isAfter(inici)) {
            throw new IllegalArgumentException("La data i hora de fi han de ser posteriors a les d'inici");
        }

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

    // Emmagatzematge físic a: uploads/vehicles/(MATRICULA_MARCA_MODEL)/(lliurar|retornar)/
    public String guardarFotos(reservesEntity reserva, MultipartFile[] fotos, String accio) {
        if (fotos == null || fotos.length == 0) {
            return "";
        }
        StringBuilder rutes = new StringBuilder();

        // Construcció del nom de la carpeta del vehicle
        String carpetaVehicle = "vehicle_desconegut";
        if (reserva != null && reserva.getVehicle() != null) {
            String matricula = (reserva.getVehicle().getMatricula() != null) 
                    ? reserva.getVehicle().getMatricula() : "S-MATRICULA";
            String marcaModel = (reserva.getVehicle().getMarca() + "_" + reserva.getVehicle().getModel())
                    .replaceAll("[^a-zA-Z0-9_-]", "_");
            carpetaVehicle = matricula + "_" + marcaModel;
        }

        // Ruta completa: uploads/vehicles/1234ABC_Seat_Ibiza/lliurar/
        Path uploadPath = Paths.get("uploads", "vehicles", carpetaVehicle, accio).toAbsolutePath();
        File dir = uploadPath.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String[] costats = {"frontal", "posterior", "lateral_esquerre", "lateral_dret"};

        for (int i = 0; i < fotos.length; i++) {
            MultipartFile foto = fotos[i];
            if (foto != null && !foto.isEmpty()) {
                try {
                    String originalFilename = foto.getOriginalFilename();
                    String ext = (originalFilename != null && originalFilename.contains("."))
                            ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";

                    String costat = (i < costats.length) ? costats[i] : "foto_" + (i + 1);
                    String filename = accio + "_" + costat + "_" + System.currentTimeMillis() + ext;

                    Path filePath = uploadPath.resolve(filename);
                    Files.copy(foto.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    rutes.append("/uploads/vehicles/")
                         .append(carpetaVehicle).append("/")
                         .append(accio).append("/")
                         .append(filename).append(" ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return rutes.toString().trim();
    }

    @Transactional
    public reservesEntity lliurar(Long codi, LocalDate dataLliurament, LocalTime horaLliurament,
            Integer kmInicials, String nivellCombustible, String novesObservacions,
            MultipartFile[] fotos) {
        reservesEntity r = reservesRepository.findById(codi)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no trobada: #" + codi));

        if (EN_CURS.equals(r.getEstat()) || FINALITZADA.equals(r.getEstat()) || CANCELADA.equals(r.getEstat())) {
            throw new IllegalArgumentException("No es pot lliurar una reserva en estat " + r.getEstat());
        }

        r.setDataLliurament(dataLliurament != null ? dataLliurament : LocalDate.now());
        r.setHoraLliurament(horaLliurament != null ? horaLliurament : LocalTime.now());
        r.setEstat(EN_CURS);

        String rutesFotos = guardarFotos(r, fotos, "lliurar");

        StringBuilder obs = new StringBuilder();
        if (r.getObservacions() != null && !r.getObservacions().isBlank()) {
            obs.append(r.getObservacions()).append("\n");
        }
        obs.append("[Lliurament ").append(r.getDataLliurament()).append("]: ");
        if (kmInicials != null) {
            obs.append("Km inicials: ").append(kmInicials).append(" km. ");
        }
        if (nivellCombustible != null && !nivellCombustible.isBlank()) {
            obs.append("Combustible: ").append(nivellCombustible).append(". ");
        }
        if (!rutesFotos.isBlank()) {
            obs.append("Fotos lliurament: ").append(rutesFotos).append(". ");
        }
        if (novesObservacions != null && !novesObservacions.isBlank()) {
            obs.append("Notes: ").append(novesObservacions.trim());
        }

        r.setObservacions(obs.toString());
        return reservesRepository.save(r);
    }

    @Transactional
    public reservesEntity retornar(Long codi, LocalDate dataRetorn, LocalTime horaRetorn,
            boolean teDesperfectes, boolean desperfectesImputables,
            BigDecimal importDanys, BigDecimal deduccioCarburant,
            String novesObservacions, MultipartFile[] fotos) {
        reservesEntity r = reservesRepository.findById(codi)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no trobada: #" + codi));

        if (!EN_CURS.equals(r.getEstat())) {
            throw new IllegalArgumentException("Només es poden retornar vehicles de reserves EN_CURS.");
        }

        LocalDate dataTornada = (dataRetorn != null) ? dataRetorn : LocalDate.now();
        LocalTime horaTornada = (horaRetorn != null) ? horaRetorn : LocalTime.now();

        r.setDataRetorn(dataTornada);

        LocalDateTime previst = LocalDateTime.of(r.getDataFi(), r.getHoraFi());
        LocalDateTime real = LocalDateTime.of(dataTornada, horaTornada);

        BigDecimal penalitzacioRetard = BigDecimal.ZERO;
        long horesRetard = 0;
        if (real.isAfter(previst)) {
            long minutsRetard = Duration.between(previst, real).toMinutes();
            horesRetard = (minutsRetard + 59) / 60;
            BigDecimal preuHora = (r.getVehicle() != null && r.getVehicle().getPreuHora() != null)
                    ? r.getVehicle().getPreuHora() : BigDecimal.ZERO;
            penalitzacioRetard = preuHora.multiply(BigDecimal.valueOf(horesRetard));
        }

        BigDecimal fiancaOriginal = (r.getFianca() != null) ? r.getFianca() : BigDecimal.ZERO;
        BigDecimal carrecDanys = (teDesperfectes && desperfectesImputables && importDanys != null) ? importDanys : BigDecimal.ZERO;
        BigDecimal carrecCombustible = (deduccioCarburant != null) ? deduccioCarburant : BigDecimal.ZERO;

        BigDecimal totalDeduccions = carrecDanys.add(carrecCombustible).add(penalitzacioRetard);
        BigDecimal fiancaARetornar = fiancaOriginal.subtract(totalDeduccions);
        if (fiancaARetornar.compareTo(BigDecimal.ZERO) < 0) {
            fiancaARetornar = BigDecimal.ZERO;
        }

        String rutesFotos = guardarFotos(r, fotos, "retornar");

        StringBuilder obs = new StringBuilder();
        if (r.getObservacions() != null && !r.getObservacions().isBlank()) {
            obs.append(r.getObservacions()).append("\n");
        }
        obs.append("[Retorn ").append(dataTornada).append(" ").append(horaTornada).append("]: ");

        if (horesRetard > 0) {
            obs.append("Retard: ").append(horesRetard).append("h (-").append(penalitzacioRetard).append("€). ");
        }
        if (teDesperfectes) {
            if (desperfectesImputables) {
                obs.append("Danys imputables al client (-").append(carrecDanys).append("€). ");
            } else {
                obs.append("Danys no imputables (avaria/tercers, 0€ càrrec). ");
            }
        }
        if (carrecCombustible.compareTo(BigDecimal.ZERO) > 0) {
            obs.append("Carburant no retornat complet (-").append(carrecCombustible).append("€). ");
        }

        if (!rutesFotos.isBlank()) {
            obs.append("Fotos retorn: ").append(rutesFotos).append(". ");
        }
        obs.append("Resum fiança: ").append(fiancaOriginal).append("€ | Deduccions: ").append(totalDeduccions).append("€ | Retorn final: ").append(fiancaARetornar).append("€. ");

        if (novesObservacions != null && !novesObservacions.isBlank()) {
            obs.append("Descripció: ").append(novesObservacions.trim());
        }

        r.setObservacions(obs.toString());
        r.setEstat(FINALITZADA);

        return reservesRepository.save(r);
    }

    public long comptarPendentsLliurament() {
        return reservesRepository.findAll().stream()
                .filter(r -> PENDENT.equals(r.getEstat()) || CONFIRMADA.equals(r.getEstat()))
                .count();
    }

    public long comptarPendentsRetorn() {
        return reservesRepository.findAll().stream()
                .filter(r -> EN_CURS.equals(r.getEstat()))
                .count();
    }

    public List<reservesEntity> llistarPropersLliuraments() {
        LocalDate avui = LocalDate.now();
        return reservesRepository.findAll().stream()
                .filter(r -> !CANCELADA.equals(r.getEstat()) && !FINALITZADA.equals(r.getEstat()))
                .filter(r -> EN_CURS.equals(r.getEstat()) || (r.getDataInici() != null && !r.getDataInici().isBefore(avui)))
                .sorted(Comparator.comparing(reservesEntity::getDataInici)
                        .thenComparing(reservesEntity::getHoraInici))
                .limit(10)
                .toList();
    }
}