package br.com.cyberconexo.controller;
import br.com.cyberconexo.dto.ApiDtos.*;
import br.com.cyberconexo.service.GameService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController
@RequestMapping("/api")
public class GameController {
 private final GameService service;
 public GameController(GameService service){this.service=service;}
 @GetMapping("/puzzles/{id}") public PuzzleResponse puzzle(@PathVariable Long id){return service.puzzle(id);}
 @PostMapping("/games") public StartGameResponse start(@Valid @RequestBody StartGameRequest request){return service.start(request);}
 @PostMapping("/games/{id}/guesses") public GuessResponse guess(@PathVariable UUID id,@Valid @RequestBody GuessRequest request){return service.guess(id,request);}
 @GetMapping("/games/{id}") public GameStateResponse state(@PathVariable UUID id){return service.state(id);}
}
