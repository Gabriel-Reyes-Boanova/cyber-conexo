package br.com.cyberconexo.controller;

import br.com.cyberconexo.dto.LoginRequest;
import br.com.cyberconexo.dto.PlayerResponse;
import br.com.cyberconexo.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService service;

    public PlayerController(PlayerService service) { this.service = service; }

    /** Entra com o nome: acha o jogador existente ou cria um novo. */
    @PostMapping("/login")
    public PlayerResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    /** Jogador e histórico atualizado (usado ao terminar uma partida). */
    @GetMapping("/{id}")
    public PlayerResponse get(@PathVariable UUID id) { return service.get(id); }
}
