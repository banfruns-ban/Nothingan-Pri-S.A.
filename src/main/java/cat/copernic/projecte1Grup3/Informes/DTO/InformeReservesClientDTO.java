package cat.copernic.projecte1Grup3.Informes.DTO;

import cat.copernic.projecte1Grup3.Reserves.Entity.reservesEntity;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO (Data Transfer Object) per a l'informe de reserves per client (RF91).
 * Agrupa la llista de reserves trobades i calcula els valors totals (recompte i suma econòmica).
 */
public class InformeReservesClientDTO {

    // Llista de les reserves filtrades
    private List<reservesEntity> reserves;
    
    // Recompte total de reserves trobades
    private long totalReserves;
    
    // Suma del cost total de totes les reserves filtrades
    private BigDecimal preuTotal;

    // Constructor buit
    public InformeReservesClientDTO() {
    }

    // Constructor amb tots els camps
    public InformeReservesClientDTO(List<reservesEntity> reserves, long totalReserves, BigDecimal preuTotal) {
        this.reserves = reserves;
        this.totalReserves = totalReserves;
        this.preuTotal = preuTotal;
    }

    // ============================================================
    // GETTERS I SETTERS
    // ============================================================

    public List<reservesEntity> getReserves() {
        return reserves;
    }

    public void setReserves(List<reservesEntity> reserves) {
        this.reserves = reserves;
    }

    public long getTotalReserves() {
        return totalReserves;
    }

    public void setTotalReserves(long totalReserves) {
        this.totalReserves = totalReserves;
    }

    public BigDecimal getPreuTotal() {
        return preuTotal;
    }

    public void setPreuTotal(BigDecimal preuTotal) {
        this.preuTotal = preuTotal;
    }
}