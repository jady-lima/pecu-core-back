package com.pecucore.system.core.config;

import com.pecucore.system.core.model.Usuario;
import com.pecucore.system.core.service.UsuarioService;
import com.pecucore.system.core.service.TokenService;
import com.auth0.jwt.exceptions.JWTVerificationException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ErroSegurancaHandler erroSegurancaHandler;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = retrieveToken(request);

        if (token != null) {
            try {
                String username = tokenService.validarToken(token);

                if (username == null || username.isBlank()) {
                    erroSegurancaHandler.responderTokenInvalido(request, response);
                    return;
                }

                Usuario usuario =
                        usuarioService.getUsuarioByUsername(username);

                if (usuario == null) {
                    erroSegurancaHandler.responderTokenInvalido(request, response);
                    return;
                }

                var authentication =
                        new UsernamePasswordAuthenticationToken(
                                usuario,
                                null,
                                usuario.getAuthorities()
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

            } catch (JWTVerificationException exception) {
                erroSegurancaHandler.responderTokenInvalido(request, response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String retrieveToken(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        return authHeader.substring(7);
    }
}