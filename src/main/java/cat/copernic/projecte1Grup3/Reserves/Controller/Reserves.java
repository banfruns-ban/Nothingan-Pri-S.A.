package cat.copernic.projecte1Grup3.Reserves.Controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;
import cat.copernic.projecte1Grup3.Reserves.Service.ReservesClientService;
import cat.copernic.projecte1Grup3.Reserves.Service.ReservesService;
import cat.copernic.projecte1Grup3.Ressenya.Service.RessenyaService;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Service.ClientService;
import cat.copernic.projecte1Grup3.Vehicles.Entity.VehiclesEntity;
import cat.copernic.projecte1Grup3.Vehicles.Service.VehiclesService;

@Controller
@RequestMapping("/reserves-client")
public class Reserves {

    private final ReservesService reservesService;
    private final VehiclesService vehiclesService;
    private final ClientService clientService;
    private final ReservesClientService reservesClientService;
    private final RessenyaService ressenyaService;

    @Autowired
    public Reserves(ReservesService reservesService, VehiclesService vehiclesService,
                    ClientService clientService, ReservesClientService reservesClientService,
                    RessenyaService ressenyaService) {
        this.reservesService = reservesService;
        this.vehiclesService = vehiclesService;
        this.clientService = clientService;
        this.reservesClientService = reservesClientService;
        this.ressenyaService = ressenyaService;
    }

    // GET /reserves-client/nova
    @GetMapping("/nova")
    public String formulariNovaReserva(
            @RequestParam(name = "matricula", required = false) String matricula,
            Authentication authentication,
            Model model,
            RedirectAttributes ra) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        boolean clientPendent = clientService.llistar(null, null, null, authentication.getName(), null)
                .stream()
                .filter(client -> authentication.getName().equalsIgnoreCase(client.getEmail()))
                .anyMatch(client -> "PENDENT".equals(client.getEstatValidacio()));
        if (clientPendent) {
            ra.addFlashAttribute("error", "El teu compte està pendent de validació per un administrador. No pots realitzar reserves encara.");
            return "redirect:/cataleg";
        }

        if (matricula != null && !matricula.isBlank()) {
            Optional<VehiclesEntity> vehicleOpt = vehiclesService.buscarPerMatricula(matricula);
            if (vehicleOpt.isPresent()) {
                model.addAttribute("vehicleSeleccionat", vehicleOpt.get());
                model.addAttribute("resenyes",
                        ressenyaService.llistarPerVehicle(vehicleOpt.get().getMatricula()));
            } else {
                ra.addFlashAttribute("error", "El vehicle seleccionat no existeix.");
                return "redirect:/cataleg";
            }
        }

        if (!model.containsAttribute("reserva")) {
            model.addAttribute("reserva", new ReservesEntity());
        }

        // Aviso del 25% para clientes premium (AJUSTA getPremium() al campo real de ClientEntity)
        model.addAttribute("esPremium",
                reservesClientService.obtenirClient(authentication.getName())
                        .map(c -> Boolean.TRUE.equals(c.isPremium()))
                        .orElse(false));

        return "reserves/crearReservaClient";
    }

    // POST /reserves-client/guardar
    @PostMapping("/guardar")
    public String guardarReservaClient(
            @ModelAttribute("reserva") ReservesEntity reserva,
            Authentication authentication,
            RedirectAttributes ra) {

        if (authentication == null || !authentication.isAuthenticated()) {
            ra.addFlashAttribute("error", "Sessió caducada. Torna a iniciar sessió.");
            return "redirect:/login";
        }

        String matricula = reserva.getVehicle() != null ? reserva.getVehicle().getMatricula() : "";

        try {
            ClientEntity client = reservesClientService.obtenirClient(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));

            reservesService.crear(client.getDni(), matricula,
                    reserva.getDataInici(), reserva.getHoraInici(),
                    reserva.getDataFi(), reserva.getHoraFi(),
                    reserva.getObservacions());

            ra.addFlashAttribute("missatge", "Reserva realitzada amb èxit!");
            return "redirect:/reserves-client";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No s'ha pogut realitzar la reserva: " + e.getMessage());
            ra.addFlashAttribute("reserva", reserva);
            return "redirect:/reserves-client/nova?matricula=" + matricula;
        }
    }
}