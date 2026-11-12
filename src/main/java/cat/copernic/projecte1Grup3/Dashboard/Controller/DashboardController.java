package cat.copernic.projecte1Grup3.Dashboard.Controller;

// --- Importaciones para manejo de estructuras de datos en JSON y API REST ---
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody; // Permite devolver objetos/JSON en lugar de plantillas Thymeleaf

import cat.copernic.projecte1Grup3.Reserves.Service.reservesService;
import cat.copernic.projecte1Grup3.Usuaris.Client.Service.clientService;
import cat.copernic.projecte1Grup3.Vehicles.Service.VehiclesService;

/**
 * Controlador para la gestión del Dashboard principal. Incluye renderizado
 * tradicional HTML y un endpoint REST para actualización en tiempo real.
 */
@Controller
public class DashboardController {

    // Referencias a los servicios de lógica de negocio
    private final reservesService reservesLogic;
    private final clientService clientLogic;
    private final VehiclesService vehiclesLogic;

    // Inyección de dependencias por constructor
    public DashboardController(reservesService reservesLogic,
            clientService clientLogic,
            VehiclesService vehiclesLogic) {
        this.reservesLogic = reservesLogic;
        this.clientLogic = clientLogic;
        this.vehiclesLogic = vehiclesLogic;
    }

    // ============================================================
    // CARGA INICIAL DE LA PÁGINA (GET /dashboard)
    // ============================================================
    /**
     * Muestra la vista principal del Dashboard con la carga estática inicial.
     */
    @GetMapping({"/dashboard"})
    public String mostrarDashboard(Model model) {
        // Cálculo de contadores globales
        long pendentsLliurament = reservesLogic.comptarPendentsLliurament();  // PENDENT o CONFIRMADA
        long pendentsRetorn = reservesLogic.comptarPendentsRetorn();          // EN CURS
        long vehiclesActius = reservesLogic.vehiclesActius().size();          // Total vehículos activos
        long clientsTotals = clientLogic.llistar(null, null, null, null, null).size(); // Total clientes

        // Inyección de datos al modelo Thymeleaf
        model.addAttribute("pendentsLliurament", pendentsLliurament);
        model.addAttribute("pendentsRetorn", pendentsRetorn);
        model.addAttribute("vehiclesActius", vehiclesActius);
        model.addAttribute("clientsTotals", clientsTotals);
        model.addAttribute("propersLliuraments", reservesLogic.llistarPropersLliuraments());

        return "dashboard/dashboard"; // Renderiza templates/dashboard/dashboard.html
    }

    // ============================================================
    // ENDPOINT API PARA TIEMPO REAL (GET /dashboard/api/stats)
    // ============================================================
    /**
     * Endpoint API invocado por JavaScript (Polling) en segundo plano. Ubicado
     * dentro de /dashboard/api/stats para estar protegido bajo las reglas de
     * acceso de /dashboard/** en SecurityConfig.
     *
     * @return Map con los KPIs actualizados y el listado de entregas activas.
     */
    @GetMapping("/dashboard/api/stats")
    @ResponseBody
    public Map<String, Object> obtenirEstadistiquesJSON() {
        Map<String, Object> response = new HashMap<>();

        // 1. Recopilación de KPIs en vivo desde la base de datos
        response.put("pendentsLliurament", reservesLogic.comptarPendentsLliurament());
        response.put("pendentsRetorn", reservesLogic.comptarPendentsRetorn());
        response.put("vehiclesActius", reservesLogic.vehiclesActius().size());
        response.put("clientsTotals", clientLogic.llistar(null, null, null, null, null).size());

        // 2. Mapeo simplificado de las reservas a estructuras JSON livianas
        var propers = reservesLogic.llistarPropersLliuraments().stream().map(r -> {
            Map<String, Object> item = new HashMap<>();
            item.put("codiReserva", r.getCodiReserva());
            item.put("clientNom", r.getClient() != null ? r.getClient().getNom() : "Sense Client");
            item.put("vehicleNom", r.getVehicle() != null ? (r.getVehicle().getMarca() + " " + r.getVehicle().getModel()) : "Sense Vehicle");
            item.put("vehicleMatricula", r.getVehicle() != null ? r.getVehicle().getMatricula() : "---");

            // Formateo manual de fecha para enviar un string limpio al cliente JS
            String dataStr = "---";
            if (r.getDataInici() != null) {
                dataStr = String.format("%02d/%02d/%d",
                        r.getDataInici().getDayOfMonth(),
                        r.getDataInici().getMonthValue(),
                        r.getDataInici().getYear());
                if (r.getHoraInici() != null) {
                    dataStr += ", " + r.getHoraInici() + "h";
                }
            }
            item.put("dataIniciFormatada", dataStr);
            item.put("estat", r.getEstat());
            return item;
        }).toList();

        response.put("propersLliuraments", propers);

        return response; // Se envía como objeto JSON al cliente
    }
}
