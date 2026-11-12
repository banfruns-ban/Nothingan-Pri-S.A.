/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3;
import cat.copernic.projecte1Grup3.Usuaris.Client.Service.ClientService;
import cat.copernic.projecte1Grup3.Usuaris.Client.Documents.ClientDocuments;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepositoryMongo;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientRepositoryMongo clientRepositoryMongo;

    @InjectMocks
    private ClientService clientService;

    private ClientEntity clientExemple;
    private MockMultipartFile imatge;

    @BeforeEach
    void setUp() {
        clientExemple = new ClientEntity();
        clientExemple.setDni("12345678Z");
        clientExemple.setNom("Joan Garcia");
        clientExemple.setEmail("joan@test.com");
        clientExemple.setPassword("password123");
        clientExemple.setTelefon("612345678");
        clientExemple.setNacionalitat("Espanyola");
        clientExemple.setAdreca("Carrer Major 12");
        clientExemple.setTipusLlicencia("B");
        clientExemple.setDataNaixement(LocalDate.of(1990, 5, 14));
        clientExemple.setDataCaducitatDNI(LocalDate.of(2030, 3, 1));
        clientExemple.setDataCaducitatCarnet(LocalDate.now().plusYears(3));
        clientExemple.setEstatValidacio("VALIDAT");

        imatge = new MockMultipartFile("foto", "foto.png", "image/png", new byte[]{1, 2, 3});
    }

    // ---------- ALTA ----------

    @Test
    @DisplayName("Comprovar que alta rebutja un DNI amb format invàlid")
    void testAltaDniInvalid() {
        clientExemple.setDni("1234");

        assertThrows(IllegalArgumentException.class,
                () -> clientService.alta(clientExemple, imatge, imatge, imatge));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Comprovar que alta rebutja un DNI que ja existeix")
    void testAltaDniDuplicat() {
        when(clientRepository.existsById("12345678Z")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> clientService.alta(clientExemple, imatge, imatge, imatge));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Comprovar que alta rebutja una contrasenya de menys de 8 caràcters")
    void testAltaContrasenyaCurta() {
        clientExemple.setPassword("1234");
        when(clientRepository.existsById("12345678Z")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> clientService.alta(clientExemple, imatge, imatge, imatge));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Comprovar que alta rebutja si falta alguna de les 3 imatges")
    void testAltaSenseImatges() {
        when(clientRepository.existsById("12345678Z")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> clientService.alta(clientExemple, imatge, null, imatge));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Comprovar que alta rebutja un carnet de conduir caducat")
    void testAltaCarnetCaducat() {
        clientExemple.setDataCaducitatCarnet(LocalDate.now().minusDays(1));
        when(clientRepository.existsById("12345678Z")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> clientService.alta(clientExemple, imatge, imatge, imatge));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Comprovar que alta deixa el client en estat PENDENT i no premium, i desa a MySQL i MongoDB")
    void testAltaCorrecta() throws Exception {
        clientExemple.setEstatValidacio(null);
        when(clientRepository.existsById("12345678Z")).thenReturn(false);

        clientService.alta(clientExemple, imatge, imatge, imatge);

        assertEquals("PENDENT", clientExemple.getEstatValidacio());
        assertFalse(clientExemple.isPremium());
        verify(clientRepository, times(1)).saveAndFlush(clientExemple);
        verify(clientRepositoryMongo, times(1)).save(any(ClientDocuments.class));
    }

    // ---------- ACTUALITZAR ----------

    @Test
    @DisplayName("Comprovar que actualitzar rebutja un correu que ja té un altre client")
    void testActualitzarEmailDuplicat() {
        when(clientRepository.findById("12345678Z")).thenReturn(Optional.of(clientExemple));
        when(clientRepository.existsByEmailIgnoreCaseAndDniNot("altre@test.com", "12345678Z"))
                .thenReturn(true);

        ClientEntity dades = new ClientEntity();
        dades.setEmail("altre@test.com");

        assertThrows(IllegalArgumentException.class,
                () -> clientService.actualitzar("12345678Z", dades, "VALIDAT", false, null, null, null));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Comprovar que actualitzar no sobreescriu les dates amb null")
    void testActualitzarConservaDatesSiNoVenen() throws Exception {
        when(clientRepository.findById("12345678Z")).thenReturn(Optional.of(clientExemple));
        LocalDate dniAbans = clientExemple.getDataCaducitatDNI();
        LocalDate naixementAbans = clientExemple.getDataNaixement();

        ClientEntity dades = new ClientEntity();
        dades.setNom("Joan Garcia Soler");
        dades.setEmail("joan@test.com");

        clientService.actualitzar("12345678Z", dades, "VALIDAT", true, null, null, null);

        assertEquals(dniAbans, clientExemple.getDataCaducitatDNI());
        assertEquals(naixementAbans, clientExemple.getDataNaixement());
        assertEquals("Joan Garcia Soler", clientExemple.getNom());
        assertTrue(clientExemple.isPremium());
        verify(clientRepository, times(1)).saveAndFlush(clientExemple);
    }

    @Test
    @DisplayName("Comprovar que actualitzar rebutja un estat de validació no vàlid")
    void testActualitzarEstatInvalid() {
        when(clientRepository.findById("12345678Z")).thenReturn(Optional.of(clientExemple));

        assertThrows(IllegalArgumentException.class,
                () -> clientService.actualitzar("12345678Z", new ClientEntity(), "INVENTAT", false, null, null, null));

        verify(clientRepository, never()).saveAndFlush(any());
    }

    // ---------- ESTATS ----------

    @Test
    @DisplayName("Comprovar que canviarEstat modifica l'estat de validació del client")
    void testCanviarEstat() {
        when(clientRepository.findById("12345678Z")).thenReturn(Optional.of(clientExemple));

        clientService.canviarEstat("12345678Z", "REBUTJAT");

        assertEquals("REBUTJAT", clientExemple.getEstatValidacio());
        verify(clientRepository, times(1)).save(clientExemple);
    }

    @Test
    @DisplayName("Comprovar que canviarEstat rebutja un estat no vàlid")
    void testCanviarEstatInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> clientService.canviarEstat("12345678Z", "INVENTAT"));

        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Comprovar que canviarPremium modifica correctament l'estat premium")
    void testCanviarPremium() {
        when(clientRepository.findById("12345678Z")).thenReturn(Optional.of(clientExemple));

        clientService.canviarPremium("12345678Z", true);

        assertTrue(clientExemple.isPremium());
        verify(clientRepository, times(1)).save(clientExemple);
    }

    // ---------- ELIMINAR ----------

    @Test
    @DisplayName("Comprovar que eliminar esborra el client de MySQL i els documents de MongoDB")
    void testEliminar() {
        when(clientRepository.findById("12345678Z")).thenReturn(Optional.of(clientExemple));

        clientService.eliminar("12345678Z");

        verify(clientRepository, times(1)).delete(clientExemple);
        verify(clientRepository, times(1)).flush();
        verify(clientRepositoryMongo, times(1)).deleteById("12345678Z");
    }

    @Test
    @DisplayName("Comprovar que eliminar llança excepció si el client no existeix")
    void testEliminarNoExisteix() {
        when(clientRepository.findById("00000000T")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> clientService.eliminar("00000000T"));

        verify(clientRepository, never()).delete(any(ClientEntity.class));
        verify(clientRepositoryMongo, never()).deleteById(any());
    }

    // ---------- LLISTAR ----------

    @Test
    @DisplayName("Comprovar que llistar filtra els clients per estat de validació")
    void testLlistarPerEstat() {
        ClientEntity pendent = new ClientEntity();
        pendent.setDni("87654321X");
        pendent.setEstatValidacio("PENDENT");

        when(clientRepository.findAll()).thenReturn(List.of(clientExemple, pendent));

        List<ClientEntity> resultat = clientService.llistar(null, null, "PENDENT", null, null);

        assertEquals(1, resultat.size());
        assertEquals("87654321X", resultat.get(0).getDni());
    }
}