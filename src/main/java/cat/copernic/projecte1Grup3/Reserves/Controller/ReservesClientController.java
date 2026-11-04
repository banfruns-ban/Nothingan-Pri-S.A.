package cat.copernic.projecte1Grup3.Reserves.Controller;

import cat.copernic.projecte1Grup3.Reserves.Entity.reservesEntity;
import cat.copernic.projecte1Grup3.Reserves.Service.reservesService;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Service.VehiclesService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/ReservesClient")
public class ReservesClientController {

    private final reservesService reservesService;
    private final VehiclesService vehiclesService;

    @Autowired
    public ReservesClientController(reservesService reservesService, VehiclesService vehiclesService) {
        this.reservesService = reservesService;
        this.vehiclesService = vehiclesService;
    }

    // Ruta: /ReservesClient (Listado de mis reservas)
    @GetMapping
    public String llistarReservesClient(HttpSession session, Model model, RedirectAttributes ra) {
        // Control de autenticación: si no hay cliente en sesión, redirige al login
        clientEntity clientLogueat = (clientEntity) session.getAttribute("clientLogueat");
        if (clientLogueat == null) {
            ra.addFlashAttribute("error", "Ha de iniciar sessió per veure les meves reserves.");
            return "redirect:/login";
        }

        // Cargar solo las reservas del cliente autenticado
        // model.addAttribute("reserves", reservesService.buscarPerClient(clientLogueat.getDni()));

        return "reservas/lesMevesReserves";
    }

    // Ruta: /ReservesClient/nova (Formulario de reserva)
    @GetMapping("/nova")
    public String formulariNovaReserva(
            @RequestParam(name = "matricula", required = false) String matricula,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {

        // 1. Control de autenticación
        clientEntity clientLogueat = (clientEntity) session.getAttribute("clientLogueat");
        if (clientLogueat == null) {
            ra.addFlashAttribute("error", "Cal iniciar sessió o registrar-se per reservar un vehicle.");
            return "redirect:/login";
        }

        // 2. Control de estado de validación (RF82): Si está PENDENT no puede reservar aún
        if ("PENDENT".equals(clientLogueat.getEstatValidacio())) {
            ra.addFlashAttribute("error", "El teu compte està pendent de validació per un administrador. No pots realitzar reserves encara.");
            return "redirect:/cataleg";
        }

        // Carga de vehicle seleccionat
        if (matricula != null && !matricula.isBlank()) {
            Optional<VehiclesEntity> vehicleOpt = vehiclesService.buscarPerMatricula(matricula);
            if (vehicleOpt.isPresent()) {
                model.addAttribute("vehicleSeleccionat", vehicleOpt.get());
            } else {
                ra.addFlashAttribute("error", "El vehicle seleccionat no existeix.");
                return "redirect:/cataleg";
            }
        }

        if (!model.containsAttribute("reserva")) {
            model.addAttribute("reserva", new reservesEntity());
        }

        return "reservas/crearReservaClient";
    }

    // Ruta: POST /ReservesClient/guardar
    @PostMapping("/guardar")
    public String guardarReservaClient(
            @ModelAttribute("reserva") reservesEntity reserva,
            HttpSession session,
            RedirectAttributes ra) {

        clientEntity clientLogueat = (clientEntity) session.getAttribute("clientLogueat");
        if (clientLogueat == null) {
            ra.addFlashAttribute("error", "Sessió caducada. Torna a iniciar sessió.");
            return "redirect:/login";
        }

        try {
            // Asignar el cliente logueado a la reserva antes de guardar
            // reserva.setClient(clientLogueat);
            // reservesService.guardarReservaClient(reserva);

            ra.addFlashAttribute("missatge", "Reserva realitzada amb èxit!");
            return "redirect:/ReservesClient";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No s'ha pogut realitzar la reserva: " + e.getMessage());
            return "redirect:/ReservesClient/nova?matricula=" + (reserva.getVehicle() != null ? reserva.getVehicle().getMatricula() : "");
        }
    }

    // Ruta: POST /ReservesClient/anular/{codiReserva}
    @PostMapping("/anular/{codiReserva}")
    public String anularReservaClient(
            @PathVariable("codiReserva") Long codiReserva,
            HttpSession session,
            RedirectAttributes ra) {

        clientEntity clientLogueat = (clientEntity) session.getAttribute("clientLogueat");
        if (clientLogueat == null) {
            return "redirect:/login";
        }

        try {
            ra.addFlashAttribute("missatge", "La reserva #" + codiReserva + " s'ha anul·lat correctament.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No s'ha pogut anul·lar la reserva: " + e.getMessage());
        }
        return "redirect:/ReservesClient";
    }
}