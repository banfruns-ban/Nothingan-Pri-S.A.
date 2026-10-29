package cat.copernic.projecte1Grup3.Reserves.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import cat.copernic.projecte1Grup3.Reserves.DTO.resumClientDTO;
import cat.copernic.projecte1Grup3.Reserves.Repository.historicReservesRepository;

public class informeController {
    private historicReservesRepository historicReservesRepository;

    @GetMapping("/reserves-client")
    public ResponseEntity<List<resumClientDTO>> getInformeClients(@RequestParam List<String> dnis) {
        List<resumClientDTO> informe = historicReservesRepository.obtenirInformeReservesPerClients(dnis);
        return ResponseEntity.ok(informe);
    }
}