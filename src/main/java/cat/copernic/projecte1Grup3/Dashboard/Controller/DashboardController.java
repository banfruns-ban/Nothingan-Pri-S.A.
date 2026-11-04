package cat.copernic.projecte1Grup3.Dashboard.Controller;

import cat.copernic.projecte1Grup3.Reserves.Service.reservesService;
import cat.copernic.projecte1Grup3.Usuaris.Client.Service.clientService;
import cat.copernic.projecte1Grup3.Vehicles.Service.VehiclesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final reservesService reservesLogic;
    private final clientService clientLogic;
    private final VehiclesService vehiclesLogic;

    public DashboardController(reservesService reservesLogic,
                               clientService clientLogic,
                               VehiclesService vehiclesLogic) {
        this.reservesLogic = reservesLogic;
        this.clientLogic = clientLogic;
        this.vehiclesLogic = vehiclesLogic;
    }

    @GetMapping({"/", "/dashboard"})
    public String mostrarDashboard(Model model) {
        long pendentsLliurament = reservesLogic.comptarPendentsLliurament();
        long pendentsRetorn = reservesLogic.comptarPendentsRetorn();
        long vehiclesActius = reservesLogic.vehiclesActius().size();
        long clientsTotals = clientLogic.llistar(null, null, null, null, null).size();

        model.addAttribute("pendentsLliurament", pendentsLliurament);
        model.addAttribute("pendentsRetorn", pendentsRetorn);
        model.addAttribute("vehiclesActius", vehiclesActius);
        model.addAttribute("clientsTotals", clientsTotals);

        model.addAttribute("propersLliuraments", reservesLogic.llistarPropersLliuraments());

        // Apunta a templates/dashboard/dashboard.html
        return "dashboard/dashboard";
    }
}