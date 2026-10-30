package cat.copernic.projecte1Grup3.Reserves.Controller;

import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cat.copernic.projecte1Grup3.Reserves.Entity.InformeReserves;
import cat.copernic.projecte1Grup3.Reserves.Logic.InformeReservesLogic;

@RestController
@RequestMapping("/agent/informes")
@PreAuthorize("hasAnyRole('AGENT','ADMIN')")
public class InformeReservesController {
 
    private final InformeReservesLogic logic;
 
    public InformeReservesController(InformeReservesLogic logic) {
        this.logic = logic;
    }
 
    /** GET /agent/informes/reserves            -> tots els clients
     *  GET /agent/informes/reserves?dnis=A,B   -> només aquests clients */
    @GetMapping("/reserves")
    public InformeReserves informe(@RequestParam(required = false) List<String> dnis) {
        return logic.generarInforme(dnis);
    }
 
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> peticioInvalida(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("missatge", e.getMessage()));
    }
}