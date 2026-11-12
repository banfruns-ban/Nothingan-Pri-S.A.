package cat.copernic.projecte1Grup3.Informes.Entity;

import java.math.BigDecimal;
import java.util.List;

public record InformeReserves(
        List<InformeReservesFila> files,
        int totalReserves,
        BigDecimal preuTotal,
        int totalAnullades) {
}