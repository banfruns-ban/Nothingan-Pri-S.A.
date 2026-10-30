package cat.copernic.projecte1Grup3.Informes.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InformeReservesFila(
        LocalDateTime data,
        String vehicle,      // "1234ABC - Seat Ibiza"
        String client,       // "Maria Pons (12345678Z)"
        BigDecimal importReserva,
        EstatHistoric estat) {
}