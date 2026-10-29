package cat.copernic.projecte1Grup3.Reserves.DTO;

import java.time.LocalDateTime;

public record detallReservaDTO(
    String idReserva,
    Double preuTotal,
    LocalDateTime dataInici,
    LocalDateTime dataFi
) {}