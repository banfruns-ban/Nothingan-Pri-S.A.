// ============================================================
// BLOQUE 1: CABECERA (package e imports)
// ============================================================
// package: indica en qué "carpeta lógica" vive la clase. Debe coincidir con la carpeta real del proyecto.
package cat.copernic.projecte1Grup3.Usuaris.Agent.Service;

// Los imports existen porque Java solo ve por defecto lo que está en su propio paquete.
// Cada import está porque se usa más abajo; si no se importa, no compila.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.AgentEntity;         // Entidad con la que trabaja el servicio
import org.springframework.beans.factory.annotation.Autowired;               // Inyección automática de dependencias
import org.springframework.stereotype.Service;                               // Marca la clase como servicio
import java.util.List;                                                       // Los métodos de listado devuelven varios agentes
import java.util.Optional;                                                   // Contenedor que puede tener o no un valor
import cat.copernic.projecte1Grup3.Usuaris.Agent.Repository.AgentRepository;

// ============================================================
// BLOQUE 2: DECLARACIÓN DE LA CLASE Y DEPENDENCIAS
// ============================================================
@Service // Spring crea una instancia y la gestiona como bean (capa de lógica de negocio)
// POR QUÉ: Spring crea UNA instancia y la guarda para entregársela a quien la pida (el controlador).
// También deja claro que esta capa contiene la lógica de negocio.
public class AgentService { // public: el controlador (otro paquete) tiene que poder usarla
    private static final Logger log = LoggerFactory.getLogger(AgentService.class);
    @Autowired // Inyecta la dependencia de la clase repository (pom y lo de arriba)
    // POR QUÉ: Spring busca un objeto del tipo AgentRepository y lo asigna aquí. Tú nunca haces "new",
    // lo que desacopla las clases.
    // (Matiz: el pom.xml solo aporta las librerías; lo que permite la conexión son @Service aquí
    //  y @Repository en el repositorio, que hacen que Spring cree ambos objetos.)
    private AgentRepository agentRepository; // Enlace con la capa de acceso a datos
    // private: solo la usa esta clase. El tipo y el nombre coinciden; funciona, pero a veces se llama
    // "repository" para no confundir tipo y variable.

    // ============================================================
    // BLOQUE 3: GUARDAR (alta y edición)
    // ============================================================
    // save() sirve para crear y para editar:
    //   - si el DNI no existe -> INSERT
    //   - si el DNI ya existe -> UPDATE
    // void: no devuelve nada, solo ejecuta. El parámetro es el agente a guardar.
    // public: el controlador tiene que poder llamarlo.
    public void guardarAgent(AgentEntity agent) {
        agentRepository.save(agent); 
        log.info("Agent donat d'alta: {}", agent);
        // Guarda o actualiza un registro en la base de datos, en un metodo de JPARepository
    }

    // ============================================================
    // BLOQUE 4: CONSULTAS (listar y buscar por DNI)
    // ============================================================
    // RF13: Llistar agents
    // Sin parámetros porque lista todo. Ahora mismo el controlador no lo usa
    // (usa llistarOFiltrarAgents), pero queda disponible.
    public List<AgentEntity> llistarAgents() {
        return agentRepository.findAll(); // Devuelve todos los agentes
    }

    // RF14 / RF15: Buscar per DNI / Consultar detall
    // POR QUÉ Optional: el agente puede no existir; Optional obliga a quien lo llama a comprobarlo
    // y evita NullPointerException.
    public Optional<AgentEntity> obtenirPerDni(String dni) {
        return agentRepository.findById(dni); // Optional: puede contener el agente o estar vacío
        // findById busca por clave primaria (el DNI).
        // Se utiliza en editar agente, ver detalles...
    }

    // ============================================================
    // BLOQUE 5: ELIMINAR
    // ============================================================
    // Solo recibe el DNI, no el objeto entero, porque basta la clave primaria para borrar.
    // Aquí iría una regla de negocio (ej. no borrar si tiene incidencias): lanzaría IllegalStateException,
    // que el controlador ya captura. Ahora mismo el servicio no la lanza.
    public void eliminarAgent(String dni) {
        agentRepository.deleteById(dni); // Borra por clave primaria (DNI)
        log.warn("Agent eliminat: {}", dni);
    }

    // ============================================================
    // BLOQUE 6: LISTAR O FILTRAR (buscador de la lista)
    // ============================================================
    /**
     * Llista tots els agents o els filtra si s'ha introduït un text de cerca.
     */
    // POR QUÉ aquí: reúne en un solo método la decisión "¿hay texto de búsqueda?",
    // así el controlador no tiene que decidirlo.
    public List<AgentEntity> llistarOFiltrarAgents(String cerca) {
        // Sin texto de búsqueda (null, vacío o solo espacios): devuelve todos
        // cerca == null: el parámetro puede no venir en la URL.
        // ||: "o". Si el primero es verdadero, Java ni evalúa el segundo.
        // El ORDEN importa: si pusieras cerca.trim() primero y fuera null, daría NullPointerException.
        // trim(): quita espacios al inicio y al final. isEmpty(): comprueba que no queda nada.
        if (cerca == null || cerca.trim().isEmpty()) {
            return agentRepository.findAll();
        }
        // Con texto: usa la consulta personalizada; trim() quita los espacios sobrantes
        // Se hace trim() otra vez para enviar el texto limpio a la consulta.
        return agentRepository.cercarPerDniNomEmail(cerca.trim());
    }
}
