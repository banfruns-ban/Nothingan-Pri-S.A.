package cat.copernic.projecte1Grup3.Config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GestorExcepcionsGlobal {

    private static final Logger log = LoggerFactory.getLogger(GestorExcepcionsGlobal.class);

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String integritat(DataIntegrityViolationException e,
                             HttpServletRequest req,
                             RedirectAttributes ra) {
        log.error("Error d'integritat a {} {}: {}", req.getMethod(), req.getRequestURI(),
                e.getMostSpecificCause().getMessage(), e);
        ra.addFlashAttribute("error", "No s'ha pogut desar: revisa que les dades siguin vàlides.");
        return "redirect:/dashboard";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String estatIlegal(IllegalStateException e,
                              HttpServletRequest req,
                              RedirectAttributes ra) {
        log.error("Estat il·legal a {} {}", req.getMethod(), req.getRequestURI(), e);
        ra.addFlashAttribute("error", "S'ha produït un error inesperat.");
        return "redirect:/dashboard";
    }

    @ExceptionHandler(NullPointerException.class)
    public String nul(NullPointerException e,
                      HttpServletRequest req,
                      RedirectAttributes ra) {
        log.error("NullPointerException a {} {}", req.getMethod(), req.getRequestURI(), e);
        ra.addFlashAttribute("error", "S'ha produït un error inesperat.");
        return "redirect:/dashboard";
    }
}