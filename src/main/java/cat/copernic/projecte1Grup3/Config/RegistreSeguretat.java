/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class RegistreSeguretat {

    private static final Logger log = LoggerFactory.getLogger(RegistreSeguretat.class);

    @EventListener
    public void loginFallit(AuthenticationFailureBadCredentialsEvent event) {
        log.warn("Intent d'inici de sessió fallit per a l'usuari: {}", event.getAuthentication().getName());
    }

    @EventListener
    public void loginCorrecte(AuthenticationSuccessEvent event) {
        log.info("Inici de sessió correcte: {}", event.getAuthentication().getName());
    }
}