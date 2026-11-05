package cat.copernic.projecte1Grup3;

import java.net.URI;

import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/*.css", "/**/*.css").permitAll()
                        .requestMatchers("/", "/home", "/login", "/forgot-password", "/registre", "/cataleg").permitAll()
                        .requestMatchers("/dashboard/**").hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers("/ReservesClient/**").hasRole("CLIENT")
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
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if (role.equals("ROLE_ADMIN") || role.equals("ROLE_AGENT")) {
                    response.sendRedirect(contextPath + "/dashboard");
                    return;
                }
            }
            SavedRequest savedRequest = requestCache.getRequest(request, response);
            String reservationsPath = contextPath + "/ReservesClient";
            if (savedRequest != null) {
                URI savedUri = URI.create(savedRequest.getRedirectUrl());
                String savedPath = savedUri.getRawPath();
                if (savedPath.equals(reservationsPath)
                        || savedPath.startsWith(reservationsPath + "/")) {
                    String redirectUrl = contextPath + savedPath;
                    if (savedUri.getRawQuery() != null) {
                        redirectUrl += "?" + savedUri.getRawQuery();
                    }
                    requestCache.removeRequest(request, response);
                    response.sendRedirect(redirectUrl);
                    return;
                }
            }
            response.sendRedirect(contextPath + "/cataleg");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails client = User.builder()
                .username("client@correu.cat")
                .password(encoder.encode("grup3"))
                .roles("CLIENT")
                .build();

        UserDetails agent = User.builder()
                .username("agent@correu.cat")
                .password(encoder.encode("grup3"))
                .roles("AGENT")
                .build();

        UserDetails admin = User.builder()
                .username("admin@correu.cat")
                .password(encoder.encode("grup3"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(client, agent, admin);
    }
}