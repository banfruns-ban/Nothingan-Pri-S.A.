package cat.copernic.projecte1Grup3.Reserves.DTO;

import java.util.List;

public record resumClientDTO(
    String dniClient,
    long totalReserves,
    Double preuTotalReserves,
    List<detallReservaDTO> detallReserves
) {}