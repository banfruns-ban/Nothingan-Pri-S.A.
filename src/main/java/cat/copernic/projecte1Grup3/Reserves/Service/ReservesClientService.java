package cat.copernic.projecte1Grup3.Reserves.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cat.copernic.projecte1Grup3.Reserves.Entity.reservesEntity;
import cat.copernic.projecte1Grup3.Reserves.Repository.reservesRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.clientRepository;

@Service
public class ReservesClientService {

    private final reservesRepository reservesRepository;
    private final clientRepository clientRepository;

    public ReservesClientService(reservesRepository reservesRepository,
                                 clientRepository clientRepository) {
        this.reservesRepository = reservesRepository;
        this.clientRepository = clientRepository;
    }

    // El login pot ser el DNI o l'email: provem tots dos
    public Optional<clientEntity> obtenirClient(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            return Optional.empty();
        }
        String id = identificador.trim();
        Optional<clientEntity> client = clientRepository.findById(id.toUpperCase());
        if (client.isEmpty()) {
            client = clientRepository.findByEmailIgnoreCase(id);
        }
        return client;
    }

    // Només les reserves del client loguejat
    public List<reservesEntity> llistarPerClient(String identificador) {
        return obtenirClient(identificador)
                .map(c -> reservesRepository.findByClientDniIn(List.of(c.getDni()))
                        .stream()
                        .sorted(Comparator.comparing(reservesEntity::getCodiReserva).reversed())
                        .toList())
                .orElse(List.of());
    }

    // Una reserva concreta, només si pertany al client loguejat
    public Optional<reservesEntity> obtenirReservaDelClient(Long codi, String identificador) {
        return obtenirClient(identificador).flatMap(c ->
                reservesRepository.findById(codi)
                        .filter(r -> r.getClient() != null
                                && c.getDni().equals(r.getClient().getDni())));
    }
}