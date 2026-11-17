package cat.copernic.projecte1Grup3.Usuaris.Client.Controller;

import cat.copernic.projecte1Grup3.Usuaris.Client.Documents.ClientDocuments;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Service.ClientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador de clientes. Agrupa dos zonas:
 *  - Panel de agentes/admin (rutas /dashboard/clients...): gestión completa de clientes.
 *  - Portal del cliente (rutas /registre y /perfil): registro y autogestión del propio perfil.
 * El controlador solo recibe la petición y decide qué pantalla mostrar;
 * la lógica de negocio está en ClientService.
 */
@Controller
public class ClientController {

    private static final Logger log = LoggerFactory.getLogger(ClientController.class);
    private final ClientService clientLogic;

    public ClientController(ClientService clientLogic) {
        this.clientLogic = clientLogic;
    }

    // =====================================================
    // ALTA DE CLIENTE (PANEL AGENTS)
    // =====================================================

    /**
     * GET: muestra el formulario de alta de cliente del panel.
     * Si venimos de un error (los datos ya están en el modelo) los conserva;
     * si no, crea un cliente vacío para rellenar.
     */
    @GetMapping({"/dashboard/clients/alta", "/dashboard/clients/altaUsuari", "/dashboard/clients/nou"})
    public String mostrarFormulari(Model model) {
        if (!model.containsAttribute("client")) {
            model.addAttribute("client", new ClientEntity());
        }
        return "client/altaUsuari";
    }

    /**
     * POST: da de alta un cliente desde el panel (lo hace un agente o un admin).
     * Pasa "true" al servicio para que el cliente quede VALIDAT directamente,
     * sin tener que esperar a una validación posterior.
     * Si hay un error, vuelve al formulario conservando lo escrito.
     */
    @PostMapping({"/dashboard/clients/alta", "/dashboard/clients/altaUsuari"})
    public String altaClient(
            @ModelAttribute("client") ClientEntity client,
            @RequestParam("fotoDniFrontal") MultipartFile fotoDniFrontal,
            @RequestParam("fotoDniPosterior") MultipartFile fotoDniPosterior,
            @RequestParam("fotoCarnet") MultipartFile fotoCarnet,
            Model model,
            RedirectAttributes ra) {
        String error;
        try {
            clientLogic.alta(client, fotoDniFrontal, fotoDniPosterior, fotoCarnet, true);
            ra.addFlashAttribute("missatge", "Client donat d'alta i validat correctament!");
            return "redirect:/dashboard/clients";
        } catch (IllegalArgumentException e) {
            // Error de validación previsto (DNI incorrecto, contraseña corta...): se muestra tal cual
            error = e.getMessage();
        } catch (Exception e) {
            // Error inesperado: se registra en el log y al usuario solo se le da un mensaje genérico
            log.error("Error en l'alta del client", e);
            error = "No s'ha pogut completar el registre";
        }

        // Nunca se devuelve la contraseña ni los datos de la tarjeta al formulario
        client.setPassword(null);
        client.setNumTargeta(null);
        client.setCvv(null);
        model.addAttribute("error", error);
        return "client/altaUsuari";
    }

    // =====================================================
    // LISTADO Y DETALLE
    // =====================================================

    /**
     * GET: lista los clientes del panel, con filtros opcionales
     * (nacionalidad, nombre, teléfono, ciudad, DNI, población, email).
     * Devuelve a la vista los valores del filtro para que los campos no se vacíen.
     */
    @GetMapping("/dashboard/clients")
    public String llistar(@RequestParam(required = false) String nacionalitat,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String telefon,
            @RequestParam(required = false) String ciutat,
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String poblacio,
            @RequestParam(required = false) String email,
            Model model) {
        model.addAttribute("clients",
                clientLogic.filtrarClients(nacionalitat, nom, telefon, ciutat, dni, poblacio, email));
        model.addAttribute("nacionalitat", nacionalitat);
        model.addAttribute("nom", nom);
        model.addAttribute("telefon", telefon);
        model.addAttribute("ciutat", ciutat);
        model.addAttribute("dni", dni);
        model.addAttribute("poblacio", poblacio);
        model.addAttribute("email", email);
        return "client/llistaClients";
    }

    /**
     * GET: muestra la ficha de un cliente concreto (por DNI).
     * Acepta dos rutas para evitar errores 404 si algún enlace usa la antigua.
     * Si el cliente no existe, vuelve al listado con un aviso.
     */
    @GetMapping({"/dashboard/clients/{dni}", "/dashboard/clients/detall/{dni}"})
    public String detall(@PathVariable String dni, Model model, RedirectAttributes ra) {
        return clientLogic.obtenir(dni).map(c -> {
            model.addAttribute("client", c);
            return "client/detallClient";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Client no trobat");
            return "redirect:/dashboard/clients";
        });
    }

    /**
     * GET: devuelve una imagen del cliente guardada en MongoDB
     * (tipus = frontal, posterior o carnet) para mostrarla en la ficha.
     * Responde 404 si el cliente no tiene documentos, la imagen no existe o el tipo no es válido.
     */
    @GetMapping("/dashboard/clients/{dni}/imatge/{tipus}")
    @ResponseBody
    public ResponseEntity<byte[]> imatge(@PathVariable String dni, @PathVariable String tipus) {
        Optional<ClientDocuments> docs = clientLogic.obtenirDocuments(dni);
        if (docs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ClientDocuments d = docs.get();

        byte[] bytes;
        String mime;
        switch (tipus) {
            case "frontal" -> {
                bytes = d.getFotoDniFrontal();
                mime = d.getTipusFotoDniFrontal();
            }
            case "posterior" -> {
                bytes = d.getFotoDniPosterior();
                mime = d.getTipusFotoDniPosterior();
            }
            case "carnet" -> {
                bytes = d.getFotoCarnet();
                mime = d.getTipusFotoCarnet();
            }
            default -> {
                return ResponseEntity.notFound().build();
            }
        }

        if (bytes == null || mime == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mime))
                .body(bytes);
    }

    // =====================================================
    // EDICIÓN Y ELIMINACIÓN
    // =====================================================

    /**
     * GET: muestra el formulario de edición con los datos actuales del cliente.
     * Si el cliente no existe, vuelve al listado con un aviso.
     */
    @GetMapping("/dashboard/clients/{dni}/editar")
    public String formulariEditar(@PathVariable String dni, Model model, RedirectAttributes ra) {
        return clientLogic.obtenir(dni).map(c -> {
            model.addAttribute("client", c);
            return "client/editarClient";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Client no trobat");
            return "redirect:/dashboard/clients";
        });
    }

    /**
     * POST: guarda los cambios de un cliente desde el panel.
     * Permite cambiar el estado de validación y el premium, y subir imágenes nuevas (opcionales).
     * Siempre redirige al listado, con un aviso de éxito o de error.
     */
    @PostMapping("/dashboard/clients/{dni}/editar")
    public String editar(@PathVariable String dni,
            @ModelAttribute("client") ClientEntity dades,
            @RequestParam String estatValidacio,
            @RequestParam(defaultValue = "false") boolean premium,
            @RequestParam(value = "fotoDniFrontal", required = false) MultipartFile fotoDniFrontal,
            @RequestParam(value = "fotoDniPosterior", required = false) MultipartFile fotoDniPosterior,
            @RequestParam(value = "fotoCarnet", required = false) MultipartFile fotoCarnet,
            RedirectAttributes ra) {
        try {
            clientLogic.actualitzar(dni, dades, estatValidacio, premium,
                    fotoDniFrontal, fotoDniPosterior, fotoCarnet);
            ra.addFlashAttribute("missatge", "Client actualitzat correctament");
        } catch (IllegalArgumentException e) {
            // Validación de negocio (correo duplicado, teléfono incorrecto...): mensaje tal cual
            ra.addFlashAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            // Violación de la base de datos no prevista: se guarda la causa real en el log
            log.error("Error d'integritat editant el client {}: {}", dni, e.getMostSpecificCause().getMessage(), e);
            ra.addFlashAttribute("error",
                    "No s'ha pogut guardar el client. Revisa que totes les dades siguin vàlides");
        } catch (IOException e) {
            log.error("Error llegint les imatges del client {}", dni, e);
            ra.addFlashAttribute("error", "No s'han pogut llegir les imatges");
        }
        return "redirect:/dashboard/clients";
    }

    /**
     * POST: elimina un cliente (MySQL y sus documentos de MongoDB).
     * Si el cliente tiene reservas asociadas, la base de datos lo impide
     * y se muestra un aviso en lugar de borrarlo.
     */
    @PostMapping("/dashboard/clients/{dni}/eliminar")
    public String eliminar(@PathVariable String dni, RedirectAttributes ra) {
        try {
            clientLogic.eliminar(dni);
            ra.addFlashAttribute("missatge", "Client eliminat correctament");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("error", "No es pot eliminar: el client té reserves associades");
        }
        return "redirect:/dashboard/clients";
    }

    // =====================================================
    // VALIDACIÓN Y ESTADO (RF82)
    // =====================================================

    /**
     * POST: cambia el estado de validación de un cliente (PENDENT, VALIDAT o REBUTJAT).
     * Es la acción rápida del listado, sin pasar por el formulario de edición.
     */
    @PostMapping("/dashboard/clients/{dni}/validacio")
    public String validar(@PathVariable String dni, @RequestParam String estat, RedirectAttributes ra) {
        clientLogic.canviarEstat(dni, estat);
        ra.addFlashAttribute("missatge", "Estat actualitzat a " + estat);
        return "redirect:/dashboard/clients";
    }

    /**
     * POST: activa o desactiva el estado premium de un cliente.
     */
    @PostMapping("/dashboard/clients/{dni}/premium")
    public String premium(@PathVariable String dni, @RequestParam boolean premium, RedirectAttributes ra) {
        clientLogic.canviarPremium(dni, premium);
        ra.addFlashAttribute("missatge", "Estat premium actualitzat");
        return "redirect:/dashboard/clients";
    }

    // =====================================================
    // REGISTRE DES DEL PORTAL CLIENT (RF81)
    // =====================================================

    /**
     * GET: muestra el formulario de registro público del portal.
     * Conserva los datos si venimos de un intento fallido.
     */
    @GetMapping("/registre")
    public String mostrarFormulariRegistre(Model model) {
        if (!model.containsAttribute("client")) {
            model.addAttribute("client", new ClientEntity());
        }
        return "registre";
    }

    /**
     * POST: procesa el registro de un cliente desde el portal.
     * Usa el alta de 4 parámetros, así que el cliente queda PENDENT
     * hasta que un administrador lo valide (RF82).
     * Si falla, vuelve al formulario con el error y los datos escritos.
     */
    @PostMapping("/registre")
    public String processarRegistreClient(
            @ModelAttribute("client") ClientEntity client,
            @RequestParam("fotoDniFrontal") MultipartFile fotoDniFrontal,
            @RequestParam("fotoDniPosterior") MultipartFile fotoDniPosterior,
            @RequestParam("fotoCarnet") MultipartFile fotoCarnet,
            RedirectAttributes ra) {
        String error;
        try {
            clientLogic.alta(client, fotoDniFrontal, fotoDniPosterior, fotoCarnet);
            ra.addFlashAttribute("missatge", "Sol·licitud de registre enviada amb èxit! L'administrador ha de validar el teu compte.");
            return "redirect:/cataleg";
        } catch (IllegalArgumentException e) {
            error = e.getMessage();
        } catch (Exception e) {
            log.error("Error en el registre del client extern", e);
            error = "No s'ha pogut completar el registre. Revisa que les dades i fitxers siguin correctes.";
        }

        // Conservamos lo escrito (nunca la contraseña ni la tarjeta)
        client.setPassword(null);
        client.setNumTargeta(null);
        client.setCvv(null);
        ra.addFlashAttribute("client", client);
        ra.addFlashAttribute("error", error);
        return "redirect:/registre";
    }

    // =====================================================
    // EL MEU PERFIL (PORTAL CLIENT)
    // =====================================================

    /**
     * GET: muestra el perfil del cliente que ha iniciado sesión.
     * Busca al cliente por el email de la sesión; si no hay sesión va al login,
     * y si no encuentra el perfil vuelve al catálogo con un aviso.
     */
    @GetMapping("/perfil")
    public String veurePerfil(Authentication auth, Model model, RedirectAttributes ra) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        return clientLogic.obtenirPerEmail(auth.getName()).map(c -> {
            model.addAttribute("client", c);
            return "client/perfil";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "No s'ha trobat el perfil del client.");
            return "redirect:/cataleg";
        });
    }

    /**
     * POST: guarda los cambios que el propio cliente hace en su perfil.
     * Si cambia el correo (que es su usuario de acceso), cierra la sesión
     * y lo manda al login para que entre con el correo nuevo.
     */
    @PostMapping("/perfil")
    public String guardarPerfil(@ModelAttribute("client") ClientEntity dades,
            Authentication auth,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes ra) {
        String emailActual = auth.getName();
        try {
            clientLogic.actualitzarPerfilClient(emailActual, dades);

            // Si el correo ha cambiado, la sesión actual ya no es válida: se cierra
            if (dades.getEmail() != null && !emailActual.equalsIgnoreCase(dades.getEmail().trim())) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
                ra.addFlashAttribute("missatge", "Correu electrònic actualitzat. Torna a iniciar sessió amb el nou correu.");
                return "redirect:/login";
            }

            ra.addFlashAttribute("missatge", "Les teves dades s'han actualitzat correctament!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Error d'integritat actualitzant perfil de {}", emailActual, e);
            ra.addFlashAttribute("error", "No s'han pogut desar els canvis. Revisa les dades introduïdes.");
        }
        return "redirect:/perfil";
    }
}