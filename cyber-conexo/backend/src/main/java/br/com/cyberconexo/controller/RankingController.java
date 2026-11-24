package br.com.cyberconexo.controller;

import br.com.cyberconexo.dto.RankingEntryResponse;
import br.com.cyberconexo.service.RankingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    private final RankingService service;

    public RankingController(RankingService service) { this.service = service; }

    @GetMapping
    public List<RankingEntryResponse> top(@RequestParam(required = false) Long puzzleId,
                                          @RequestParam(defaultValue = "10") int limit) {
        return service.top(puzzleId, limit);
    }
}
