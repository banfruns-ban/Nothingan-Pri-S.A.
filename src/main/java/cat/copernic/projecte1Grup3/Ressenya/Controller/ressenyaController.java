/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Ressenya.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.projecte1Grup3.Ressenya.Service.ressenyaService;

@Controller
public class ressenyaController {

    private static final Logger log = LoggerFactory.getLogger(ressenyaController.class);

    private final ressenyaService resenyesLogic;

    public ressenyaController(ressenyaService resenyesLogic) {
        this.resenyesLogic = resenyesLogic;
    }

    @PostMapping("/dashboard/vehicles/{matricula}/resenyes")
    public String alta(@PathVariable String matricula,
                       @RequestParam String dni,
                       @RequestParam int puntuacio,
                       @RequestParam(required = false) String comentari,
                       RedirectAttributes ra) {
        try {
            resenyesLogic.crear(matricula, dni, puntuacio, comentari);
            ra.addFlashAttribute("missatge", "Ressenya afegida correctament");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error creant la ressenya", e);
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
            ra.addFlashAttribute("error", "No s'ha pogut crear la ressenya");
        }
        return "redirect:/dashboard/vehicles/detall/" + matricula;
    }

    @PostMapping("/dashboard/vehicles/{matricula}/resenyes/{codi}/eliminar")
    public String eliminar(@PathVariable String matricula,
                           @PathVariable Long codi,
                           RedirectAttributes ra) {
        try {
            resenyesLogic.eliminar(codi);
            ra.addFlashAttribute("missatge", "Ressenya eliminada correctament");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error eliminant la ressenya {}", codi, e);
            ra.addFlashAttribute("error", "No s'ha pogut eliminar la ressenya");
        }
       return "redirect:/dashboard/vehicles/detall/" + matricula;
    }
}