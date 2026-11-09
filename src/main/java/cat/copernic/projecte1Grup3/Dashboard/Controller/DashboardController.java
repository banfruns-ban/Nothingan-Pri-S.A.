package cat.copernic.projecte1Grup3.Dashboard.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import cat.copernic.projecte1Grup3.Reserves.Service.reservesService;
import cat.copernic.projecte1Grup3.Usuaris.Client.Service.clientService;
import cat.copernic.projecte1Grup3.Vehicles.Service.VehiclesService;

@Controller
public class DashboardController {

    //referencias
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

    @GetMapping({"/dashboard"})
    public String mostrarDashboard(Model model) {
        long pendentsLliurament = reservesLogic.comptarPendentsLliurament();  // PENDENT o CONFIRMADA
        long pendentsRetorn = reservesLogic.comptarPendentsRetorn();          // EN CURS
        long vehiclesActius = reservesLogic.vehiclesActius().size();          // Total llista vehicles
        long clientsTotals = clientLogic.llistar(null, null, null, null, null).size(); 
        // Llista clients sense filtres

        model.addAttribute("pendentsLliurament", pendentsLliurament);
        model.addAttribute("pendentsRetorn", pendentsRetorn);
        model.addAttribute("vehiclesActius", vehiclesActius);
        model.addAttribute("clientsTotals", clientsTotals);

        model.addAttribute("propersLliuraments", reservesLogic.llistarPropersLliuraments());
        // Llista proximes 10 entregues (limit 10) que no sean ni CANCELADA ni FINALITZADA
        // Retorna el que están EN CURS i els PENDENTS O CONFIRMATS mes propers
        return "dashboard/dashboard";
        // Apunta a templates/dashboard/dashboard.html
    }
}