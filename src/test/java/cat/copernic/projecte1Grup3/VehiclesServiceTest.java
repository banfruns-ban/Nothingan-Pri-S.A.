package cat.copernic.projecte1Grup3;

import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Repository.VehiclesRepository;
import cat.copernic.projecte1Grup3.Vehicles.Service.VehiclesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiclesServiceTest {

    @Mock
    private VehiclesRepository vehiclesRepository;

    @InjectMocks
    private VehiclesService vehiclesService;

    private VehiclesEntity vehicleExemple;

    @BeforeEach
    void setUp() {
        vehicleExemple = new VehiclesEntity();
        vehicleExemple.setMatricula("1234ABC");
        vehicleExemple.setMarca("Tesla");
        vehicleExemple.setModel("Model 3");
        vehicleExemple.setTipusVehicle("Elèctric");
        vehicleExemple.setActiu(true);
    }

    @Test
    @DisplayName("Comprovar que guardarVehicle assigna actiu=true si és un nou vehicle i actiu ve nul")
    void testGuardarVehicleNouAssignaActiuTrue() {
        vehicleExemple.setActiu(null);
        when(vehiclesRepository.findById("1234ABC")).thenReturn(Optional.empty());
        when(vehiclesRepository.save(any(VehiclesEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehiclesEntity resultat = vehiclesService.guardarVehicle(vehicleExemple);

        assertNotNull(resultat);
        assertTrue(resultat.getActiu());
        verify(vehiclesRepository, times(1)).save(vehicleExemple);
    }

    @Test
    @DisplayName("Comprovar que llistarDisponibles crida a llistarActiusPerTipus quan les dates són nul·les")
    void testLlistarDisponiblesSenseDates() {
        when(vehiclesRepository.findByActiuTrue()).thenReturn(List.of(vehicleExemple));

        List<VehiclesEntity> resultat = vehiclesService.llistarDisponibles(null, null, "");

        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        assertEquals("1234ABC", resultat.get(0).getMatricula());
        verify(vehiclesRepository, times(1)).findByActiuTrue();
        verify(vehiclesRepository, never()).llistarDisponiblesPerDatesYTipus(any(), any(), any());
    }

    @Test
    @DisplayName("Comprovar que llistarDisponibles crida al repositori amb dates quan s'especifiquen")
    void testLlistarDisponiblesAmbDates() {
        LocalDateTime inici = LocalDateTime.of(2026, 6, 1, 10, 0);
        LocalDateTime fi = LocalDateTime.of(2026, 6, 5, 10, 0);

        when(vehiclesRepository.llistarDisponiblesPerDatesYTipus(inici, fi, "Turisme"))
                .thenReturn(List.of(vehicleExemple));

        List<VehiclesEntity> resultat = vehiclesService.llistarDisponibles(inici, fi, "Turisme");

        assertNotNull(resultat);
        assertEquals(1, resultat.size());
        verify(vehiclesRepository, times(1)).llistarDisponiblesPerDatesYTipus(inici, fi, "Turisme");
    }

    @Test
    @DisplayName("Comprovar que desactivarVehicle marca el vehicle com a inactiu")
    void testDesactivarVehicle() {
        when(vehiclesRepository.findById("1234ABC")).thenReturn(Optional.of(vehicleExemple));

        vehiclesService.desactivarVehicle("1234ABC", null);

        assertFalse(vehicleExemple.getActiu());
        assertEquals("EN MANTENIMENT", vehicleExemple.getEstat());
        verify(vehiclesRepository, times(1)).save(vehicleExemple);
    }
}