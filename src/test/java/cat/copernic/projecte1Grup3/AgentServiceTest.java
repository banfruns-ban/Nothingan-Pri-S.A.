package cat.copernic.projecte1Grup3.Usuaris.Agent.Service;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.AgentEntity;
import cat.copernic.projecte1Grup3.Usuaris.Agent.Repository.AgentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AgentService agentService;
    private AgentEntity agentExemple;

    @BeforeEach
    void setUp() {
        // Instanciación directa asegurando que ningún mock sea null
        agentService = new AgentService(passwordEncoder, agentRepository);
        agentExemple = new AgentEntity();
    }

    // ============================================================
    // TESTS: GUARDAR AGENT (LÓGICA CON PASSWORD)
    // ============================================================
    @Test
    @DisplayName("guardarAgent nuevo con contraseña debe encriptar y llamar a save()")
    void guardarAgent_NouAgentAmbPassword_EncriptaISalva() {
        // Arrange
        agentExemple.setDni("12345678A");
        agentExemple.setPassword("1234Pass");
        when(agentRepository.findById("12345678A")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("1234Pass")).thenReturn("encoded1234Pass");

        // Act
        agentService.guardarAgent(agentExemple);

        // Assert
        assertEquals("encoded1234Pass", agentExemple.getPassword());
        verify(agentRepository, times(1)).save(agentExemple);
    }

    @Test
    @DisplayName("guardarAgent existente sin cambio de contraseña debe conservar la clave original")
    void guardarAgent_ExistentSenseCanviPassword_ConservaPasswordGuardada() {
        // Arrange
        AgentEntity agentExistent = new AgentEntity();
        agentExistent.setDni("12345678A");
        agentExistent.setPassword("encodedOldPassword");

        agentExemple.setDni("12345678A");
        agentExemple.setPassword(""); // En blanco indica no modificar

        when(agentRepository.findById("12345678A")).thenReturn(Optional.of(agentExistent));

        // Act
        agentService.guardarAgent(agentExemple);

        // Assert
        assertEquals("encodedOldPassword", agentExemple.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(agentRepository, times(1)).save(agentExemple);
    }

    @Test
    @DisplayName("guardarAgent existente con nueva contraseña debe encriptarla y guardar")
    void guardarAgent_ExistentAmbNovaPassword_EncriptaNovaPassword() {
        // Arrange
        AgentEntity agentExistent = new AgentEntity();
        agentExistent.setDni("12345678A");
        agentExistent.setPassword("encodedOldPassword");

        agentExemple.setDni("12345678A");
        agentExemple.setPassword("novaPass123");

        when(agentRepository.findById("12345678A")).thenReturn(Optional.of(agentExistent));
        when(passwordEncoder.encode("novaPass123")).thenReturn("encodedNovaPass");

        // Act
        agentService.guardarAgent(agentExemple);

        // Assert
        assertEquals("encodedNovaPass", agentExemple.getPassword());
        verify(agentRepository, times(1)).save(agentExemple);
    }

    // ============================================================
    // TESTS: CONSULTAS
    // ============================================================
    @Test
    @DisplayName("llistarAgents debe retornar la lista completa de agentes")
    void llistarAgents_RetornaLlistaCompleta() {
        // Arrange
        List<AgentEntity> agentsEsperats = List.of(agentExemple, new AgentEntity());
        when(agentRepository.findAll()).thenReturn(agentsEsperats);

        // Act
        List<AgentEntity> resultat = agentService.llistarAgents();

        // Assert
        assertNotNull(resultat);
        assertEquals(2, resultat.size());
        verify(agentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("obtenirPerDni debe retornar Optional con el agente si existe")
    void obtenirPerDni_QuanExisteix_RetornaAgent() {
        // Arrange
        String dni = "12345678A";
        when(agentRepository.findById(dni)).thenReturn(Optional.of(agentExemple));

        // Act
        Optional<AgentEntity> resultat = agentService.obtenirPerDni(dni);

        // Assert
        assertTrue(resultat.isPresent());
        assertEquals(agentExemple, resultat.get());
        verify(agentRepository, times(1)).findById(dni);
    }

    @Test
    @DisplayName("obtenirPerDni debe retornar Optional vacío si no existe")
    void obtenirPerDni_QuanNoExisteix_RetornaOptionalBuit() {
        // Arrange
        String dni = "99999999Z";
        when(agentRepository.findById(dni)).thenReturn(Optional.empty());

        // Act
        Optional<AgentEntity> resultat = agentService.obtenirPerDni(dni);

        // Assert
        assertFalse(resultat.isPresent());
        verify(agentRepository, times(1)).findById(dni);
    }

    // ============================================================
    // TESTS: ELIMINAR
    // ============================================================
    @Test
    @DisplayName("eliminarAgent debe invocar deleteById() con el DNI proporcionado")
    void eliminarAgent_Exit() {
        // Arrange
        String dni = "12345678A";

        // Act
        agentService.eliminarAgent(dni);

        // Assert
        verify(agentRepository, times(1)).deleteById(dni);
    }

    // ============================================================
    // TESTS: LISTAR O FILTRAR
    // ============================================================
    @Test
    @DisplayName("llistarOFiltrarAgents con parámetro null debe llamar a findAll()")
    void llistarOFiltrarAgents_CercaNull_CridaFindAll() {
        // Arrange
        List<AgentEntity> agentsEsperats = List.of(agentExemple);
        when(agentRepository.findAll()).thenReturn(agentsEsperats);

        // Act
        List<AgentEntity> resultat = agentService.llistarOFiltrarAgents(null);

        // Assert
        assertEquals(1, resultat.size());
        verify(agentRepository, times(1)).findAll();
        verify(agentRepository, never()).cercarPerDniNomEmail(anyString());
    }

    @Test
    @DisplayName("llistarOFiltrarAgents con texto de espacios en blanco debe llamar a findAll()")
    void llistarOFiltrarAgents_CercaEspaisEnBlanc_CridaFindAll() {
        // Arrange
        List<AgentEntity> agentsEsperats = List.of(agentExemple);
        when(agentRepository.findAll()).thenReturn(agentsEsperats);

        // Act
        List<AgentEntity> resultat = agentService.llistarOFiltrarAgents("    ");

        // Assert
        assertEquals(1, resultat.size());
        verify(agentRepository, times(1)).findAll();
        verify(agentRepository, never()).cercarPerDniNomEmail(anyString());
    }

    @Test
    @DisplayName("llistarOFiltrarAgents con texto válido debe aplicar trim() y llamar al método de búsqueda")
    void llistarOFiltrarAgents_CercaValida_CridaCercarPerDniNomEmailAmbTrim() {
        // Arrange
        String cercaAmbEspais = "  12345678A  ";
        String cercaNeta = "12345678A";
        List<AgentEntity> agentsEsperats = List.of(agentExemple);

        when(agentRepository.cercarPerDniNomEmail(cercaNeta)).thenReturn(agentsEsperats);

        // Act
        List<AgentEntity> resultat = agentService.llistarOFiltrarAgents(cercaAmbEspais);

        // Assert
        assertEquals(1, resultat.size());
        verify(agentRepository, times(1)).cercarPerDniNomEmail(cercaNeta);
        verify(agentRepository, never()).findAll();
    }
}
