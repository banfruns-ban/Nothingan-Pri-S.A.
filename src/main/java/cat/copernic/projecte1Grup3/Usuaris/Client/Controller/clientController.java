package cat.copernic.projecte1Grup3.Usuaris.Client.Controller;

import cat.copernic.projecte1Grup3.Usuaris.Client.Documents.clientDocuments;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Service.clientService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Controlador Spring MVC encargado de la gestión integral de Clientes (Peticiones HTTP GET/POST)
@Controller
public class clientController {

    private static final Logger log = LoggerFactory.getLogger(clientController.class);
    private final clientService clientLogic;

    // Inyección de dependencias por constructor
    public clientController(clientService clientLogic) {
        this.clientLogic = clientLogic;
    }

    // =====================================================
    // ALTA DE CLIENTE
    // =====================================================

    // GET /clients/nou: Carga la vista del formulario de alta inyectando un objeto cliente vacío
    @GetMapping("/clients/nou")
    public String mostrarFormulari(Model model) {
        if (!model.containsAttribute("client")) {
            model.addAttribute("client", new clientEntity());
        }
        return "client/altaUsuari";
    }

    // POST /clients/alta: Procesa el registro del cliente y la subida de sus 3 imágenes obligatorias
    @PostMapping("/clients/alta")
    public String altaClient(
            @ModelAttribute("client") clientEntity client,
            @RequestParam("fotoDniFrontal") MultipartFile fotoDniFrontal,
            @RequestParam("fotoDniPosterior") MultipartFile fotoDniPosterior,
            @RequestParam("fotoCarnet") MultipartFile fotoCarnet,
            RedirectAttributes ra) {
        try {
            clientLogic.alta(client, fotoDniFrontal, fotoDniPosterior, fotoCarnet);
            ra.addFlashAttribute("missatge", "Client donat d'alta correctament!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error en l'alta del client", e);
            ra.addFlashAttribute("error", "No s'ha pogut completar el registre");
        }
        return "redirect:/clients/nou";
    }

    // =====================================================
    // LISTADO Y DETALLE
    // =====================================================

    // GET /clients: Muestra el listado de clientes aplicando filtros opcionales (búsqueda, DNI, estado, etc.)
    @GetMapping("/clients")
    public String llistar(@RequestParam(required = false) String q,
                          @RequestParam(required = false) String dni,
                          @RequestParam(required = false) String estat,
                          @RequestParam(required = false) String email,
                          @RequestParam(required = false)
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataNaixement,
                          Model model) {
        model.addAttribute("clients", clientLogic.llistar(q, dni, estat, email, dataNaixement));
        model.addAttribute("q", q);
        model.addAttribute("dni", dni);
        model.addAttribute("estat", estat);
        model.addAttribute("dataNaixement", dataNaixement);
        model.addAttribute("email", email);
        return "client/llistaClients";
    }

    // GET /clients/{dni}: Carga la vista con la información detallada del cliente por su DNI
    @GetMapping("/clients/{dni}")
    public String detall(@PathVariable String dni, Model model, RedirectAttributes ra) {
        return clientLogic.obtenir(dni).map(c -> {
            model.addAttribute("client", c);
            return "client/detallClient";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Client no trobat");
            return "redirect:/clients";
        });
    }

    // GET /clients/{dni}/imatge/{tipus}: Sirve las imágenes desde MongoDB en formato binario hacia la vista HTML
    @GetMapping("/clients/{dni}/imatge/{tipus}")
    @ResponseBody
    public ResponseEntity<byte[]> imatge(@PathVariable String dni, @PathVariable String tipus) {
        Optional<clientDocuments> docs = clientLogic.obtenirDocuments(dni);
        if (docs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        clientDocuments d = docs.get();

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

    // GET /clients/{dni}/editar: Carga el formulario con los datos recuperados del cliente
    @GetMapping("/clients/{dni}/editar")
    public String formulariEditar(@PathVariable String dni, Model model, RedirectAttributes ra) {
        return clientLogic.obtenir(dni).map(c -> {
            model.addAttribute("client", c);
            return "client/editarClient";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Client no trobat");
            return "redirect:/clients";
        });
    }

    // POST /clients/{dni}/editar: Actualiza la información del cliente, su estado e imágenes adjuntas
    @PostMapping("/clients/{dni}/editar")
    public String editar(@PathVariable String dni,
                         @ModelAttribute("client") clientEntity dades,
                         @RequestParam String estatValidacio,
                         @RequestParam(defaultValue = "false") boolean premium,
                         @RequestParam(value = "fotoDniFrontal", required = false) MultipartFile fotoDniFrontal,
                         @RequestParam(value = "fotoDniPosterior", required = false) MultipartFile fotoDniPosterior,
                         @RequestParam(value = "fotoCarnet", required = false) MultipartFile fotoCarnet,
                         RedirectAttributes ra) {
        try {
            clientLogic.actualitzar(dni, dades, estatValidacio, premium, fotoDniFrontal, fotoDniPosterior, fotoCarnet);
            ra.addFlashAttribute("missatge", "Client actualitzat correctament");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("error", "Aquest correu ja està en ús per un altre client");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            log.error("Error llegint les imatges del client {}", dni, e);
            ra.addFlashAttribute("error", "No s'han pogut llegir les imatges");
        }
        return "redirect:/clients";
    }

    // POST /clients/{dni}/eliminar: Elimina la ficha del cliente validando dependencias con reservas
    @PostMapping("/clients/{dni}/eliminar")
    public String eliminar(@PathVariable String dni, RedirectAttributes ra) {
        try {
            clientLogic.eliminar(dni);
            ra.addFlashAttribute("missatge", "Client eliminat correctament");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("error", "No es pot eliminar: el client té reserves associades");
        }
        return "redirect:/clients";
    }

    // =====================================================
    // VALIDACIÓ Y ESTAT (RF82)
    // =====================================================

    // POST /clients/{dni}/validacio: RF82 - Cambia el estado del cliente (PENDENT -> VALIDAT / REBUTJAT)
    @PostMapping("/clients/{dni}/validacio")
    public String validar(@PathVariable String dni, @RequestParam String estat, RedirectAttributes ra) {
        clientLogic.canviarEstat(dni, estat);
        ra.addFlashAttribute("missatge", "Estat actualitzat a " + estat);
        return "redirect:/clients";
    }

    // POST /clients/{dni}/premium: Actualiza la condición de cliente Premium
    @PostMapping("/clients/{dni}/premium")
    public String premium(@PathVariable String dni, @RequestParam boolean premium, RedirectAttributes ra) {
        clientLogic.canviarPremium(dni, premium);
        ra.addFlashAttribute("missatge", "Estat premium actualitzat");
        return "redirect:/clients";
    }
}