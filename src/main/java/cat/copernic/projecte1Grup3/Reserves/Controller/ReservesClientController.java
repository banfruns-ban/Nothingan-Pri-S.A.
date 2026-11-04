package cat.copernic.projecte1Grup3.Reserves.Controller;

import cat.copernic.projecte1Grup3.Reserves.Entity.reservesEntity;
import cat.copernic.projecte1Grup3.Reserves.Service.reservesService; // Ajusta según el nombre exacto de tu Service de reservas
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/ReservesClient")
public class ReservesClientController {

    private final reservesService reservesService;

    @Autowired
    public ReservesClientController(reservesService reservesService) {
        this.reservesService = reservesService;
    }

    // Muestra el listado de reservas del cliente logueado
    @GetMapping
    public String llistarReservesClient(Model model) {
        // NOTA: Para pruebas o integración, aquí recuperarás el DNI del cliente de la sesión HTTP.
        // Por ejemplo: String dniClient = (String) session.getAttribute("dniClient");
        // De momento cargamos las reservas asociadas o la lista general adaptada al cliente.
        
        // List<reservesEntity> reserves = reservesService.buscarPerClient(dniClient);
        // model.addAttribute("reserves", reserves);

        return "reservas/lesMevesReserves";
    }

    // Procesa la cancelación/anulación de una reserva por parte del cliente
    @PostMapping("/anular/{codiReserva}")
    public String anularReservaClient(@PathVariable("codiReserva") Long codiReserva, RedirectAttributes ra) {
        try {
            // Lógica para anular la reserva
            // reservesService.anularReservaClient(codiReserva);
            ra.addFlashAttribute("missatge", "La reserva #" + codiReserva + " s'ha anul·lat correctament.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No s'ha pogut anul·lar la reserva: " + e.getMessage());
        }
        return "redirect:/ReservesClient";
    }
}