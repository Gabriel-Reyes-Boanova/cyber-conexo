package br.com.cyberconexo.controller;

import br.com.cyberconexo.dto.PuzzleResponse;
import br.com.cyberconexo.dto.PuzzleSummaryResponse;
import br.com.cyberconexo.service.PuzzleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/puzzles")
public class PuzzleController {

    private final PuzzleService service;

    public PuzzleController(PuzzleService service) { this.service = service; }

    @GetMapping
    public List<PuzzleSummaryResponse> list() { return service.listActive(); }

    @GetMapping("/{id}")
    public PuzzleResponse get(@PathVariable Long id) { return service.board(id); }
}
