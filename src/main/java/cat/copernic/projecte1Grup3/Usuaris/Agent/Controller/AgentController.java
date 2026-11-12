// ============================================================
// BLOQUE 1: CABECERA (package e imports)
// ============================================================
// package: indica en qué "carpeta lógica" vive la clase. Debe coincidir con la carpeta real del proyecto
// y evita choques entre clases con el mismo nombre en paquetes distintos.
package cat.copernic.projecte1Grup3.Usuaris.Agent.Controller;

// Los imports existen porque Java solo ve por defecto las clases de su propio paquete.
// Cada import está porque la clase se usa más abajo; si no se importa, no compila.
// --- Tus clases (entidad y servicio) ---
import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.AgentEntity;   // Se usa como tipo del formulario y de las listas
import cat.copernic.projecte1Grup3.Usuaris.Agent.Service.AgentService; // El controlador delega la lógica en el servicio

// --- Validación y errores ---
import jakarta.validation.Valid;                                // Activa las validaciones de la entidad (se usa en processarAlta)
import org.springframework.dao.DataIntegrityViolationException; // Error de la BD (duplicados, etc.) (se captura al guardar)
import org.springframework.validation.BindingResult;            // Guarda los errores de validación (se usa en processarAlta)

// --- Spring MVC ---
import org.springframework.stereotype.Controller;               // Marca la clase como controlador
import org.springframework.ui.Model;                            // Datos que viajan a la vista
import org.springframework.web.bind.annotation.*;               // @GetMapping, @PostMapping, @PathVariable... (el * importa todas)
import org.springframework.web.multipart.MultipartFile;         // Archivo subido desde el formulario
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Mensajes que sobreviven a una redirección

// --- Java: archivos y utilidades ---
import java.io.IOException;                 // Error de lectura/escritura (Java obliga a capturarlo)
import java.nio.file.Files;                 // Crear carpetas y copiar archivos
import java.nio.file.Path;                  // Representa una ruta en disco
import java.nio.file.Paths;                 // Crea un Path a partir de un texto
import java.nio.file.StandardCopyOption;    // Opciones de copia (REPLACE_EXISTING)
import java.util.List;                      // Lista de agentes
import java.util.Optional;                  // Resultado que puede existir o no

/**
 * Controlador Spring MVC per a la gestió d'agents.
 */
// ============================================================
// BLOQUE 2: DECLARACIÓN DE LA CLASE Y DEPENDENCIAS
// ============================================================
@Controller //Indica que la clase es un controlador
// POR QUÉ: sin esta anotación Spring no registra la clase y las rutas no existen.
// Hace que lo que devuelven los métodos (un String) se interprete como nombre de plantilla Thymeleaf.
@RequestMapping("/dashboard/agents") // Base de les URL de gestió d'agents
// POR QUÉ: evita escribir "/agents" en cada método. "/alta" aquí pasa a ser "/agents/alta".
public class AgentController { // public: Spring tiene que poder crearla desde fuera del paquete

    // private: solo se usa dentro de la clase. final: se asigna una vez (en el constructor) y no cambia,
    // así el controlador nunca existe sin su servicio.
    private final AgentService agentService; // Declara una variable que conecta controller con service

    // POR QUÉ un constructor: es la forma recomendada de inyectar dependencias.
    // No lleva @Autowired porque, con un único constructor, Spring lo usa automáticamente.
    public AgentController(AgentService agentService) {
        this.agentService = agentService; // this.campo = parámetro (this distingue el campo del parámetro, que se llaman igual)
    }

    // ============================================================
    // BLOQUE 3: MOSTRAR FORMULARIO DE ALTA  (GET /agents/alta)
    // ============================================================
    /**
     * Mostra el formulari d'alta d'un nou agent.
     */
    @GetMapping("/alta") // Protocolo HTTP que ejecuta la orden get (listar, ver, detalle, carga vista, formularios etc...)
    // POR QUÉ GET: solo se pide una página, no se modifica nada.
    public String mostrarFormulariAlta(Model model) { // String: devuelve el nombre de la vista. Model: lo pasa Spring solo
        // "agent": nombre con el que la vista lo recoge (th:object="${agent}").
        // new AgentEntity(): objeto vacío porque el formulario necesita algo a lo que enlazar sus campos.
        model.addAttribute("agent", new AgentEntity()); // Objeto vacío al que se enlaza el formulario
        return "agents/alta_agent";                     // Vista: templates/agents/alta_agent.html
    }

    // ============================================================
    // BLOQUE 4: PROCESAR ALTA Y EDICIÓN  (POST /agents/alta)
    // ============================================================
    /**
     * Processa el formulari d'alta i edició d'agents.
     */
    @PostMapping("/alta") // Protocolo HTTP que ejecuta la orden post (modificar, guardar, eliminar etc...)
    // POR QUÉ POST: el formulario envía datos para guardar. Misma URL que el GET anterior, pero distinto método HTTP.
    public String processarAlta(
            // @Valid: ejecuta las validaciones de la entidad.
            // @ModelAttribute("agent"): rellena un AgentEntity con los campos del formulario; "agent" debe coincidir con la vista.
            @Valid @ModelAttribute("agent") AgentEntity agent, // Datos del formulario + validación
            // BindingResult: guarda los fallos de validación. Debe ir JUSTO después del objeto validado,
            // si no Spring lanza error.
            BindingResult result, // Errores (debe ir justo después del objeto)
            // value = "fitxerFoto": es el name del <input type="file"> del HTML.
            // required = false: la foto es opcional, así que no da error si no llega.
            // (El <form> necesita enctype="multipart/form-data" para que el archivo llegue.)
            @RequestParam(value = "fitxerFoto", required = false) MultipartFile fitxerFoto, // Foto opcional
            // Model: para enviar mensajes (como "error") a la vista.
            Model model) { // Model: lleva datos del controlador a la vista (ej. mensajes de error). El return decide qué página se muestra

        // --- 4.1 Detección de alta o edición ---
        // Si ya existe un agente con ese DNI, se trata como edición
        // Optional: findById puede no encontrar nada; Optional obliga a comprobarlo y evita NullPointerException.
        Optional<AgentEntity> agentExistent = agentService.obtenirPerDni(agent.getDni()); //asegura que no da problemas en atributos que pueden ser nulos
        // POR QUÉ un boolean: convierte la búsqueda en un true/false fácil de usar más abajo.
        boolean esEdicio = agentExistent.isPresent();

        // Si alguna validación falló, vuelve al formulario SIN GUARDAR NADA
        // POR QUÉ no es "redirect:": una redirección perdería los errores y los datos escritos por el usuario.
        if (result.hasErrors()) {
            return "agents/alta_agent";
        }

        // --- 4.2 Gestión de la foto de perfil ---
        // && = "y". Se comprueba null primero: llamar a isEmpty() sobre null daría error. ! invierte ("no está vacío").
        if (fitxerFoto != null && !fitxerFoto.isEmpty()) { // Empty == vacío
            // Se ha subido una foto nueva: se guarda en disco
            // POR QUÉ try/catch: Java obliga a capturar IOException, porque escribir en disco puede fallar.
            try {
                // Nombre final: DNI + "_" + nombre original (evita que se pisen fotos entre agentes)
                // getOriginalFilename(): nombre que tenía el archivo en el ordenador del usuario. + concatena textos.
                String nomFitxer = agent.getDni() + "_" + fitxerFoto.getOriginalFilename(); // Crea un nombre unico en relación al agente

                
                // Carpeta de destino; se crea si no existe
                // Paths.get(...): ruta relativa a donde se ejecuta la aplicación.
                Path directoriUploads = Paths.get("uploads/agents");
                if (!Files.exists(directoriUploads)) {
                    // createDirectories (con "s"): crea también las carpetas padre. createDirectory fallaría si falta "uploads".
                    Files.createDirectories(directoriUploads);
                }

                // Copia el archivo al disco (sobrescribe si ya existía uno igual)
                // resolve: une carpeta + nombre sin que escribas las barras a mano.
                Path rutaFitxer = directoriUploads.resolve(nomFitxer);
                // Argumentos: de dónde leer (getInputStream), dónde escribir (rutaFitxer), qué hacer si existe (REPLACE_EXISTING).
                Files.copy(fitxerFoto.getInputStream(), rutaFitxer, StandardCopyOption.REPLACE_EXISTING);

                // En la BD solo se guarda el NOMBRE del archivo
                // POR QUÉ: guardar imágenes dentro de la BD la hace pesada y lenta.
                agent.setFoto(nomFitxer);
            } catch (IOException e) {
                // Error al escribir en disco: mensaje y vuelta al formulario
                // "error": clave que la vista lee con th:if / th:text.
                model.addAttribute("error", "Error en pujar la foto del perfil.");
                return "agents/alta_agent";
            }
        } else if (esEdicio) {
            // Edición sin foto nueva: se conserva la foto anterior para no perderla
            // POR QUÉ: el formulario no reenvía la foto antigua; sin esto, foto quedaría null y se perdería al guardar.
            // get(): saca el agente del Optional (seguro, porque esEdicio garantiza que existe).
            agent.setFoto(agentExistent.get().getFoto());
        }

        // --- 4.3 Guardado ---
        // POR QUÉ try/catch: la BD puede rechazar el guardado y así se da un mensaje claro en vez de un error 500.
        try {
            agentService.guardarAgent(agent);      // INSERT si es nuevo, UPDATE si el DNI ya existe
            // "redirect:" hace que el navegador pida otra URL (GET). Evita reenviar el formulario al recargar.
            // ?exit: parámetro que la vista puede leer para mostrar un aviso de éxito.
            return "redirect:/dashboard/agents/llista?exit"; // Post-Redirect-Get: evita reenviar el formulario al recargar
        } catch (DataIntegrityViolationException e) {
            // La BD rechaza el guardado (por ejemplo, email duplicado)
            model.addAttribute("error", "Ja existeix un usuari amb aquest DNI o correu electrònic.");
            return "agents/alta_agent";
        }
    }

    // ============================================================
    // BLOQUE 5: LISTAR Y BUSCAR  (GET /agents, /agents/, /agents/llista)
    // ============================================================
    /**
     * RF13 & RF14: Mostra la llista d'agents.
     */
    // Las llaves {} crean una lista de rutas: "" = /agents, "/" = /agents/, "/llista" = /agents/llista
    @GetMapping({"", "/", "/llista"}) // Tres URLs para el mismo método
    // @RequestParam: lee ?cerca=texto de la URL. name = nombre del parámetro.
    // required = false: al abrir la lista por primera vez no hay búsqueda, y cerca vale null.
    public String llistarAgents(@RequestParam(name = "cerca", required = false) String cerca, Model model) { // 
        // Si "cerca" está vacío devuelve todos; si tiene texto, filtra (es el filtraje)
        // POR QUÉ aquí no hay if: el servicio ya decide si filtrar o no.
        List<AgentEntity> llista = agentService.llistarOFiltrarAgents(cerca);
        // "agents" en plural porque la vista recorre varios.
        model.addAttribute("agents", llista);  // La vista la recorre con th:each
        return "agents/llistat_agents";
    }

    // ============================================================
    // BLOQUE 6: DETALLE DE UN AGENTE  (GET /agents/detall/{dni})
    // ============================================================
    /**
     * RF14 & RF15: Consulta els detalls d'un agent.
     */
    // {dni}: las llaves en la ruta crean una variable (ej. /agents/detall/12345678A).
    @GetMapping("/detall/{dni}")
    // @PathVariable("dni"): extrae esa variable de la URL. El nombre entre comillas debe coincidir con el de las llaves.
    public String consultarDetall(@PathVariable("dni") String dni, Model model) { // busca agente por DNI y pasa la vista del detalle
        // ({dni} se extrae de la URL)
        Optional<AgentEntity> agent = agentService.obtenirPerDni(dni);
        // Primero isPresent() y luego get(): llamar a get() sobre un Optional vacío daría error.
        if (agent.isPresent()) {
            model.addAttribute("agent", agent.get()); // Envía el agente a la vista
            return "agents/detall_agent";
        }
        // POR QUÉ redirigir: si alguien escribe a mano un DNI inexistente, no se rompe la aplicación.
        return "redirect:/dashboard/agents/llista"; // Si no existe, vuelve a la lista
    }

    // ============================================================
    // BLOQUE 7: FORMULARIO DE EDICIÓN  (GET /agents/editar/{dni})
    // ============================================================
    /**
     * RF12: Carrega les dades d'un agent al formulari per a la seva
     * modificació.
     */
    @GetMapping("/editar/{dni}") // GET: solo se muestra el formulario; guardar lo hace el POST /alta
    public String mostrarFormulariEditar(@PathVariable("dni") String dni, Model model) {
        Optional<AgentEntity> agent = agentService.obtenirPerDni(dni);
        if (agent.isPresent()) {
            model.addAttribute("agent", agent.get()); // Formulario con los datos ya rellenados
            // POR QUÉ la misma vista: evita mantener dos formularios casi idénticos.
            // Como el modelo ya lleva un agente con datos, los campos salen rellenos.
            return "agents/alta_agent";               // Reutiliza la misma vista que el alta
        }
        return "redirect:/dashboard/agents/llista";
    }

    // ============================================================
    // BLOQUE 8: ELIMINAR / DAR DE BAJA  (POST /agents/eliminar/{dni})
    // ============================================================
    /**
     * RF11: Donar de baixa un agent.
     */
    // POR QUÉ POST y no GET: borrar modifica datos, y un enlace GET podría activarse por error o por un rastreador.
    @PostMapping("/eliminar/{dni}") // POST porque borrar modifica datos
    // RedirectAttributes en vez de Model: Model se pierde al redirigir.
    public String eliminarAgent(@PathVariable("dni") String dni, RedirectAttributes redirect) {
        try {
            agentService.eliminarAgent(dni);
            // Flash attribute: el mensaje sobrevive a UNA redirección y desaparece
            // OJO: la clave "succes" debe escribirse igual en la vista; si allí pones "success", no se verá el mensaje.
            redirect.addFlashAttribute("succes", "Agent donat de baixa amb èxit.");
        } catch (IllegalStateException e) {
            // Error de regla de negocio: se muestra el mensaje de la excepción
            // e.getMessage(): el texto con el que se lanzó la excepción.
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            // Cualquier otro error inesperado
            // ORDEN: este catch va el último. Exception es la más general; si fuera primero,
            // el de IllegalStateException nunca se alcanzaría y Java daría error de compilación.
            redirect.addFlashAttribute("error", "Error en intentar donar de baixa.");
        }
        return "redirect:/dashboard/agents/llista"; // Siempre vuelve a la lista
    }
}
