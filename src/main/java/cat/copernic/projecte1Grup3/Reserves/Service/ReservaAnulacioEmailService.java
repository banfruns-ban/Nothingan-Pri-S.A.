package cat.copernic.projecte1Grup3.Reserves.Service;

import cat.copernic.projecte1Grup3.Reserves.Entity.ReservesEntity;
import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.ClientEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ReservaAnulacioEmailService {

    private static final BigDecimal PERCENTATGE_DEVOLUCIO = new BigDecimal("0.25");

    private final JavaMailSender mailSender;
    private final String remitent;

    public ReservaAnulacioEmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String remitent) {
        this.mailSender = mailSender;
        this.remitent = remitent;
    }

    public void enviarNotificacio(ReservesEntity reserva) {
        if (reserva == null || reserva.getClient() == null) {
            throw new IllegalStateException("La reserva no té cap client associat.");
        }

        ClientEntity client = reserva.getClient();
        if (client.getEmail() == null || client.getEmail().isBlank()) {
            throw new IllegalStateException("El client no té cap adreça de correu electrònic.");
        }
        if (reserva.getCostTotal() == null) {
            throw new IllegalStateException("La reserva no té cap cost total per calcular la devolució.");
        }

        BigDecimal importDevolucio = reserva.getCostTotal()
                .multiply(PERCENTATGE_DEVOLUCIO)
                .setScale(2, RoundingMode.HALF_UP);

        SimpleMailMessage missatge = new SimpleMailMessage();
        missatge.setFrom(remitent);
        missatge.setTo(client.getEmail());
        missatge.setSubject("Anul·lació de la reserva #" + reserva.getCodiReserva());
        missatge.setText("Hola " + client.getNom() + ",\n\n"
                + "La reserva #" + reserva.getCodiReserva() + " ha estat anul·lada.\n"
                + "L'import calculat per a la devolució (25% del cost del lloguer) és de "
                + importDevolucio.toPlainString() + " €; la fiança no s'inclou en aquest càlcul.\n\n"
                + "Aquest correu és informatiu: el reemborsament no es tramita automàticament "
                + "des del sistema.\n\n"
                + "Motiu de l'anul·lació: " + reserva.getAnulacioMotiu() + "\n\n"
                + "Atentament,\nNOTHINGAN PRI S.A");
        mailSender.send(missatge);
    }
}
