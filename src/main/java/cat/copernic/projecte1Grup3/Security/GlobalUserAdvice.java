package cat.copernic.projecte1Grup3.Security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.ClientRepository;

@ControllerAdvice
public class GlobalUserAdvice {

    private final ClientRepository clientRepository;

    public GlobalUserAdvice(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @ModelAttribute("inicialUsuari")
    public String obtenirInicialUsuari(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        String email = auth.getName();
        return clientRepository.findByEmailIgnoreCase(email)
                .map(c -> (c.getNom() != null && !c.getNom().isBlank())
                        ? c.getNom().trim().substring(0, 1).toUpperCase()
                        : "C")
                .orElse(email != null && !email.isBlank() ? email.substring(0, 1).toUpperCase() : "U");
    }
}