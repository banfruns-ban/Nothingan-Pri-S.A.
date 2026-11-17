package cat.copernic.projecte1Grup3.Reserves.Service;

import cat.copernic.projecte1Grup3.Informes.Entity.EstatHistoric;
import cat.copernic.projecte1Grup3.Informes.Service.HistoricService;
import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;
import cat.copernic.projecte1Grup3.Reserves.Repository.ReservesRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepository;
import cat.copernic.projecte1Grup3.Vehicles.Repository.VehiclesRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservesServiceTest {

    @Mock
    private ReservesRepository reservesRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private VehiclesRepository vehiclesRepository;

    @Mock
    private HistoricService historicService;

    private ReservesService service;

    @BeforeEach
    void setUp() {
        service = new ReservesService(
                reservesRepository,
                clientRepository,
                vehiclesRepository,
                historicService);
    }

    @Test
    void anularReservaRequereixMotiu() {
        assertThrows(IllegalArgumentException.class,
                () -> service.anularReserva(7L, "  "));

        verifyNoInteractions(reservesRepository, historicService);
    }

    @Test
    void anularReservaDesaMotiuIActualitzaHistoric() {
        ReservesEntity reserva = new ReservesEntity();
        reserva.setCodiReserva(7L);
        reserva.setEstat(ReservesService.PENDENT);
        when(reservesRepository.findById(7L)).thenReturn(Optional.of(reserva));
        when(reservesRepository.save(reserva)).thenReturn(reserva);

        service.anularReserva(7L, "  Error en la reserva  ");

        assertEquals(ReservesService.CANCELADA, reserva.getEstat());
        assertEquals("Error en la reserva", reserva.getAnulacioMotiu());
        verify(reservesRepository).save(reserva);
        verify(historicService).desar(reserva, EstatHistoric.CANCELADA);
    }
}
