package com.pecucore.system.config;

import com.pecucore.system.dto.UsuarioRequestDTO;
import com.pecucore.system.model.Perfil;
import com.pecucore.system.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UsuarioService usuarioService;

    @Value("${admin.nome}")
    private String adminNome;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.senha}")
    private String adminSenha;

    public AdminSeeder(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(String... args) {
        if (usuarioService.existsByUsername(adminUsername)) {
            return;
        }

        UsuarioRequestDTO admin = new UsuarioRequestDTO(adminNome, adminUsername, adminSenha, Perfil.ADMIN);

        usuarioService.createUsuario(admin);
    }
}
