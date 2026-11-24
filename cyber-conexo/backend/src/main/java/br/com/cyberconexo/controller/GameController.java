package br.com.cyberconexo.controller;

import br.com.cyberconexo.dto.*;
import br.com.cyberconexo.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService service;

    public GameController(GameService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StartGameResponse start(@Valid @RequestBody StartGameRequest request) {
        return service.start(request);
    }

    @PostMapping("/{id}/guesses")
    public GuessResponse guess(@PathVariable UUID id, @Valid @RequestBody GuessRequest request) {
        return service.guess(id, request);
    }

    @GetMapping("/{id}")
    public GameStateResponse state(@PathVariable UUID id) { return service.state(id); }
}
