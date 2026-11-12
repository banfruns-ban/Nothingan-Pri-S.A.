package cat.copernic.projecte1Grup3.Login.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    /**
     * Mapea la vista principal de inicio de sesión.
     * Spring Security redirige aquí cuando un usuario no autenticado intenta acceder a una ruta protegida.
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        // Los parámetros ?error y ?logout son añadidos automáticamente por Spring Security 
        // y se pueden validar en la plantilla con ${param.error} o pasando atributos al modelo:
        if (error != null) {
            model.addAttribute("errorMessage", "Dades d'accés incorrectes.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Sessió tancada correctament.");
        }

        return "login"; // Carga src/main/resources/templates/login.html
    }

    /**
     * Vista de recuperación de contraseña (enlace "Has oblidat la contrasenya?").
     */
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }
}