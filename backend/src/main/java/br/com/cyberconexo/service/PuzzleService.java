package br.com.cyberconexo.service;

import br.com.cyberconexo.dto.PuzzleResponse;
import br.com.cyberconexo.dto.PuzzleSummaryResponse;
import br.com.cyberconexo.entity.Puzzle;
import br.com.cyberconexo.exception.ResourceNotFoundException;
import br.com.cyberconexo.repository.PuzzleRepository;
import br.com.cyberconexo.repository.WordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PuzzleService {

    private final PuzzleRepository puzzles;
    private final WordRepository words;

    public PuzzleService(PuzzleRepository puzzles, WordRepository words) {
        this.puzzles = puzzles;
        this.words = words;
    }

    @Transactional(readOnly = true)
    public List<PuzzleSummaryResponse> listActive() {
        return puzzles.findByActiveTrueOrderByIdAsc().stream()
                .map(p -> new PuzzleSummaryResponse(p.getId(), p.getTitle(), p.getDescription(),
                        p.getTopic(), p.getDifficulty()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PuzzleResponse board(Long id) {
        Puzzle p = puzzles.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tabuleiro não encontrado."));
        return new PuzzleResponse(p.getId(), p.getTitle(), p.getDescription(), p.getTopic(), p.getDifficulty(),
                shuffledWords(p.getId()));
    }

    /** Palavras do tabuleiro em ordem aleatória, sem indicar a qual grupo cada uma pertence. */
    List<String> shuffledWords(Long puzzleId) {
        List<String> list = new ArrayList<>(words.findValuesByPuzzleId(puzzleId));
        Collections.shuffle(list);
        return list;
    }
}
