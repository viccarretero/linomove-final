package com.mbvg.linomove.MBVGSeguridad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class MBVGSecurityConfig {

    @Autowired
    private MBVGAuthenticationSuccessHandler successHandler;

    @Autowired
    private MBVGRecaptchaFilter recaptchaFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/registro",
                    "/login-cliente",
                    "/login-conductor",
                    "/login-admin",
                    "/procesar-login",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/static/**",
                    "/api/**",
                    "/error"
                ).permitAll()

                // Panel admin web deshabilitado.
                // El admin se maneja desde la aplicación de escritorio.
                .requestMatchers("/admin/**").denyAll()

                .requestMatchers("/conductor/**").hasRole("CONDUCTOR")
                .requestMatchers("/cliente/**").hasRole("CLIENTE")
                .anyRequest().authenticated()
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    String uri = request.getRequestURI();
                    String context = request.getContextPath();

                    if (uri.startsWith(context + "/admin")) {
                        response.sendRedirect(context + "/");
                    } else if (uri.startsWith(context + "/conductor")) {
                        response.sendRedirect(context + "/login-conductor");
                    } else {
                        response.sendRedirect(context + "/login-cliente");
                    }
                })

                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    String uri = request.getRequestURI();
                    String context = request.getContextPath();

                    if (uri.startsWith(context + "/admin")) {
                        response.sendRedirect(context + "/");
                    } else if (uri.startsWith(context + "/conductor")) {
                        response.sendRedirect(context + "/login-conductor?error");
                    } else {
                        response.sendRedirect(context + "/login-cliente?error");
                    }
                })
            )

            .addFilterBefore(recaptchaFilter, UsernamePasswordAuthenticationFilter.class)

            .formLogin(form -> form
                .loginPage("/login-cliente")
                .loginProcessingUrl("/procesar-login")
                .successHandler(successHandler)

                .failureHandler((request, response, exception) -> {
                    String tipoUsuario = request.getParameter("tipoUsuario");
                    String context = request.getContextPath();

                    if ("conductor".equalsIgnoreCase(tipoUsuario)) {
                        response.sendRedirect(context + "/login-conductor?error");
                    } else {
                        response.sendRedirect(context + "/login-cliente?error");
                    }
                })

                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}