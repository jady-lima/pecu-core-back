package com.Pecucore.system.config;

import com.Pecucore.system.model.Usuario;
import com.Pecucore.system.repository.UsuarioRepository;
import com.Pecucore.system.service.TokenService;
import com.auth0.jwt.exceptions.JWTVerificationException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = retrieveToken(request);

        if (token != null) {
            try {
                String username = tokenService.validateToken(token);

                if (username == null || username.isBlank()) {
                    responderNaoAutenticado(response);
                    return;
                }

                Usuario usuario =
                        usuarioRepository.findByUsername(username);

                if (usuario == null) {
                    responderNaoAutenticado(response);
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
                SecurityContextHolder.clearContext();
                responderNaoAutenticado(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void responderNaoAutenticado(
            HttpServletResponse response
    ) throws IOException {

        SecurityContextHolder.clearContext();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                "{\"message\":\"Token inválido ou expirado. Faça login novamente.\"}"
        );
    }

    private String retrieveToken(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        return authHeader.substring(7);
    }
}