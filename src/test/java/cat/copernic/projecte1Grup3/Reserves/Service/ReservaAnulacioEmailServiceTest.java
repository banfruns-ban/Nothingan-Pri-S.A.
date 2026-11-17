package cat.copernic.projecte1Grup3.Reserves.Service;

import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ReservaAnulacioEmailServiceTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final ReservaAnulacioEmailService service
            = new ReservaAnulacioEmailService(mailSender, "no-reply@example.com");

    @Test
    void enviarNotificacioCalculaElVintICincPerCentIInclouLaReserva() {
        ClientEntity client = new ClientEntity();
        client.setNom("Maria");
        client.setEmail("maria@example.com");

        ReservesEntity reserva = new ReservesEntity();
        reserva.setCodiReserva(42L);
        reserva.setClient(client);
        reserva.setCostTotal(new BigDecimal("80.00"));
        reserva.setAnulacioMotiu("Canvi de plans");

        service.enviarNotificacio(reserva);

        ArgumentCaptor<SimpleMailMessage> captor
                = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage missatge = captor.getValue();
        assertArrayEquals(
                new String[]{"maria@example.com"}, missatge.getTo());
        assertEquals(
                "Anul·lació de la reserva #42", missatge.getSubject());
        assertTrue(
                missatge.getText().contains("20.00 €"));
        assertTrue(
                missatge.getText().contains("no es tramita automàticament"));
    }

    @Test
    void enviarNotificacioArrodoneixElCalculeACentims() {
        ClientEntity client = new ClientEntity();
        client.setNom("Maria");
        client.setEmail("maria@example.com");

        ReservesEntity reserva = new ReservesEntity();
        reserva.setCodiReserva(43L);
        reserva.setClient(client);
        reserva.setCostTotal(new BigDecimal("0.02"));

        service.enviarNotificacio(reserva);

        ArgumentCaptor<SimpleMailMessage> captor
                = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertTrue(
                captor.getValue().getText().contains("0.01 €"));
    }

    @Test
    void enviarNotificacioRebutjaReservaSenseCorreu() {
        ClientEntity client = new ClientEntity();
        client.setNom("Maria");
        client.setEmail(" ");

        ReservesEntity reserva = new ReservesEntity();
        reserva.setClient(client);
        reserva.setCostTotal(new BigDecimal("80.00"));

        assertThrows(IllegalStateException.class, () -> service.enviarNotificacio(reserva));
        verifyNoInteractions(mailSender);
    }
}
