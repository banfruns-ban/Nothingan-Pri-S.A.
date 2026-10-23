package cat.copernic.projecte1Grup3.Usuaris.Client.Controller;

import cat.copernic.projecte1Grup3.Usuaris.Client.Documents.clientDocuments;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Service.clientService;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.clientRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.clientRepositoryMongo;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class clientController {

    private static final Logger log = LoggerFactory.getLogger(clientController.class);
    private static final long MAX_IMATGE = 5L * 1024 * 1024;

    private final clientRepository clientRepository;
    private final clientRepositoryMongo clientRepositoryMongo;
    private final clientService clientLogic;

    public clientController(clientRepository clientRepository,
                            clientRepositoryMongo clientRepositoryMongo,
                            clientService clientLogic) {
        this.clientRepository = clientRepository;
        this.clientRepositoryMongo = clientRepositoryMongo;
        this.clientLogic = clientLogic;
    }

    // =====================================================
    // ALTA
    // =====================================================

    @GetMapping("/clients/nou")
    public String mostrarFormulari(Model model) {
        if (!model.containsAttribute("client")) {
            model.addAttribute("client", new clientEntity());
        }
        return "altaUsuari";
    }

    @PostMapping("/clients/alta")
    public String altaClient(
            @ModelAttribute("client") clientEntity client,
            @RequestParam("fotoDniFrontal") MultipartFile fotoDniFrontal,
            @RequestParam("fotoDniPosterior") MultipartFile fotoDniPosterior,
            @RequestParam("fotoCarnet") MultipartFile fotoCarnet,
            RedirectAttributes redirectAttributes) {

        if (client.getDni() == null || client.getDni().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "El DNI és obligatori");
            return "redirect:/clients/nou";
        }
        client.setDni(client.getDni().trim().toUpperCase());

        if (clientRepository.existsById(client.getDni())) {
            redirectAttributes.addFlashAttribute(
                    "error", "Ja existeix un client amb aquest DNI");
            return "redirect:/clients/nou";
        }

        if (!valida(fotoDniFrontal) || !valida(fotoDniPosterior) || !valida(fotoCarnet)) {
            redirectAttributes.addFlashAttribute(
                    "error", "Cal pujar les 3 imatges (PNG o JPG, màxim 5MB)");
            return "redirect:/clients/nou";
        }

        // Valores que decide el servidor, no el formulario
        client.setPremium(false);
        client.setEstatValidacio("PENDENT");

        try {
            clientRepository.save(client);                          // MySQL

            clientDocuments doc = new clientDocuments();
            doc.setDni(client.getDni());
            doc.setFotoDniFrontal(fotoDniFrontal.getBytes());
            doc.setTipusFotoDniFrontal(fotoDniFrontal.getContentType());
            doc.setFotoDniPosterior(fotoDniPosterior.getBytes());
            doc.setTipusFotoDniPosterior(fotoDniPosterior.getContentType());
            doc.setFotoCarnet(fotoCarnet.getBytes());
            doc.setTipusFotoCarnet(fotoCarnet.getContentType());
            clientRepositoryMongo.save(doc);                        // MongoDB

            log.info("Client guardat a Mongo: {}", doc.getDni());

        } catch (Exception e) {
            log.error("Error en l'alta del client {}", client.getDni(), e);
            if (clientRepository.existsById(client.getDni())) {
                clientRepository.deleteById(client.getDni());       // deshacer MySQL
            }
            redirectAttributes.addFlashAttribute(
                    "error", "No s'ha pogut completar el registre");
            return "redirect:/clients/nou";
        }

        redirectAttributes.addFlashAttribute(
                "missatge", "Client donat d'alta correctament!");
        return "redirect:/clients/nou";
    }

    // =====================================================
    // LISTADO Y DETALLE
    // =====================================================

    @GetMapping("/clients")
    public String llistar(@RequestParam(required = false) String q,
                          @RequestParam(required = false) String dni,
                          @RequestParam(required = false) String estat,
                          @RequestParam(required = false)
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataNaixement,
                          Model model) {
        model.addAttribute("clients", clientLogic.llistar(q, dni, estat, dataNaixement));
        model.addAttribute("q", q);
        model.addAttribute("dni", dni);
        model.addAttribute("estat", estat);
        model.addAttribute("dataNaixement", dataNaixement);
        return "llistaClients";
    }

    @GetMapping("/clients/{dni}")
    public String detall(@PathVariable String dni, Model model, RedirectAttributes ra) {
        return clientLogic.obtenir(dni).map(c -> {
            model.addAttribute("client", c);
            return "detallClient";   // vista de detalle (crear plantilla si no existe)
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Client no trobat");
            return "redirect:/clients";
        });
    }

    // Devuelve los bytes de la imagen guardada en Mongo: tipus = frontal | posterior | carnet
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
    // EDICIÓN
    // =====================================================

    @GetMapping("/clients/{dni}/editar")
    public String formulariEditar(@PathVariable String dni, Model model, RedirectAttributes ra) {
        return clientLogic.obtenir(dni).map(c -> {
            model.addAttribute("client", c);
            return "editarClient";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Client no trobat");
            return "redirect:/clients";
        });
    }

    @PostMapping("/clients/{dni}/editar")
    public String editar(@PathVariable String dni,
                         @ModelAttribute("client") clientEntity dades,
                         @RequestParam String estatValidacio,
                         @RequestParam(defaultValue = "false") boolean premium,
                         @RequestParam(value = "fotoDniFrontal", required = false) MultipartFile fotoDniFrontal,
                         @RequestParam(value = "fotoDniPosterior", required = false) MultipartFile fotoDniPosterior,
                         @RequestParam(value = "fotoCarnet", required = false) MultipartFile fotoCarnet,
                         RedirectAttributes ra) throws IOException {
        try {
            clientLogic.actualitzar(dni, dades, estatValidacio, premium,
                    fotoDniFrontal, fotoDniPosterior, fotoCarnet);
            ra.addFlashAttribute("missatge", "Client actualitzat correctament");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("error", "Aquest correu ja està en ús per un altre client");
            return "redirect:/clients";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/clients";
        }
       return "redirect:/clients";
    }

    // =====================================================
    // VALIDACIÓN DEL ADMINISTRADOR
    // =====================================================

    @PostMapping("/clients/{dni}/validacio")
    public String validar(@PathVariable String dni,
                          @RequestParam String estat,
                          RedirectAttributes ra) {
        clientLogic.canviarEstat(dni, estat);
        ra.addFlashAttribute("missatge", "Estat actualitzat a " + estat);
        return "redirect:/clients";
    }

    @PostMapping("/clients/{dni}/premium")
    public String premium(@PathVariable String dni,
                          @RequestParam boolean premium,
                          RedirectAttributes ra) {
        clientLogic.canviarPremium(dni, premium);
        ra.addFlashAttribute("missatge", "Estat premium actualitzat");
       return "redirect:/clients";
    }

    // =====================================================
    // AUXILIAR
    // =====================================================

    private boolean valida(MultipartFile f) {
        if (f == null || f.isEmpty() || f.getSize() > MAX_IMATGE) {
            return false;
        }
        String tipus = f.getContentType();
        return "image/png".equals(tipus) || "image/jpeg".equals(tipus);
    }
}