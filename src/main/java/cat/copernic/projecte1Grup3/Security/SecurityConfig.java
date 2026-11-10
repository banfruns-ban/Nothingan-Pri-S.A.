package cat.copernic.projecte1Grup3.Security;

import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .requestMatchers("/*.css", "/**/*.css").permitAll()
                .requestMatchers("/", "/home", "/login", "/forgot-password", "/registre", "/cataleg").permitAll()
                // Restricció d'alta, edició i baixa d'agents NOMÉS per a ADMIN
                .requestMatchers("/dashboard/agents/alta",
                        "/dashboard/agents/editar/**",
                        "/dashboard/agents/eliminar/**").hasRole("ADMIN")
                .requestMatchers("/dashboard/**").hasAnyRole("ADMIN", "AGENT")
                .requestMatchers("/reserves-client/**").hasRole("CLIENT")
                .anyRequest().authenticated())
                .requestCache(cache -> cache.requestCache(requestCache()))
                .formLogin(form -> form
                .loginPage("/login")
                .successHandler(customSuccessHandler(requestCache()))
                .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
                .exceptionHandling(exceptions -> exceptions
                .accessDeniedHandler((request, response, exception) -> {
                    if (request.getRequestURI().startsWith(request.getContextPath() + "/dashboard")) {
                        response.sendRedirect(request.getContextPath() + "/");
                    } else {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    }
                }));

        return http.build();
    }

    @Bean
    RequestCache requestCache() {
        return new HttpSessionRequestCache();
    }

    @Bean
    AuthenticationSuccessHandler customSuccessHandler(RequestCache requestCache) {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            String contextPath = request.getContextPath();
            
            // Comproveu si l'usuari és ADMIN o AGENT per enviar-lo al dashboard
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if (role.equals("ROLE_ADMIN") || role.equals("ROLE_AGENT")) {
                    response.sendRedirect(contextPath + "/dashboard");
                    return;
                }
            }

            
            // Per a clients: recuperem la URL prèvia que intentava visitar (si existeix)
            SavedRequest savedRequest = requestCache.getRequest(request, response);
            if (savedRequest != null) {
                String targetUrl = savedRequest.getRedirectUrl();
                response.sendRedirect(targetUrl);
                return;
            }
            
            // Per defecte si no venia de cap lloc específic, va al catàleg
            response.sendRedirect(contextPath + "/cataleg");

           
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
