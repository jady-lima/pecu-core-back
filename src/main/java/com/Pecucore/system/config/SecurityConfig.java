package com.Pecucore.system.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.Pecucore.system.model.Perfil;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private SecurityFilter securityFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String admin = Perfil.ADMIN.name();
        String produtor = Perfil.PRODUTOR.name();
        String veterinario = Perfil.VETERINARIO.name();

        return http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/","/swagger-ui/**","/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers("/usuarios/**").hasAuthority(admin)

                .requestMatchers(HttpMethod.GET, "/propriedades/**", "/lotes/**", "/animais/**", "/pesagens/**", "/alimentacoes/**", "/vacinas/**", "/vacinacoes/**").authenticated()

                .requestMatchers("/propriedades/**", "/lotes/**").hasAnyAuthority(admin, produtor)

                .requestMatchers(HttpMethod.DELETE, "/animais/**").hasAnyAuthority(admin, produtor)
                .requestMatchers(HttpMethod.POST, "/animais").hasAnyAuthority(admin, produtor, veterinario)
                .requestMatchers(HttpMethod.PUT, "/animais/**").hasAnyAuthority(admin, produtor, veterinario)

                .requestMatchers(HttpMethod.POST, "/pesagens").hasAnyAuthority(admin, produtor, veterinario)

                .requestMatchers(HttpMethod.POST, "/alimentacoes/**").hasAnyAuthority(admin, produtor, veterinario)

                .requestMatchers("/vacinas/**", "/vacinacoes/**").hasAnyAuthority(admin, produtor, veterinario)

                .requestMatchers("/analises/**").hasAnyAuthority(admin, produtor, veterinario)

                .requestMatchers("/custos/**").hasAuthority(produtor)

                .requestMatchers(HttpMethod.POST, "/metas-desempenho/**").hasAuthority(admin)
                .requestMatchers(HttpMethod.PUT, "/metas-desempenho/**").hasAuthority(admin)

                .anyRequest().authenticated()
            )
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}