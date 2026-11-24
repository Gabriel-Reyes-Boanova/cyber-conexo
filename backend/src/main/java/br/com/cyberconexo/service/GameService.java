package br.com.cyberconexo.service;

import br.com.cyberconexo.dto.*;
import br.com.cyberconexo.entity.GameSession;
import br.com.cyberconexo.entity.Puzzle;
import br.com.cyberconexo.entity.WordGroup;
import br.com.cyberconexo.exception.BadRequestException;
import br.com.cyberconexo.exception.ConflictException;
import br.com.cyberconexo.exception.ResourceNotFoundException;
import br.com.cyberconexo.repository.GameSessionRepository;
import br.com.cyberconexo.repository.PuzzleRepository;
import br.com.cyberconexo.repository.WordGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Regras do jogo. Toda validação e pontuação acontece aqui, nunca no navegador.
 */
@Service
public class GameService {

    /** 4 grupos x 25 pontos = nota máxima 100. */
    static final int POINTS_PER_GROUP = 25;
    static final int WORDS_PER_GROUP = 4;

    private final GameSessionRepository sessions;
    private final PuzzleRepository puzzles;
    private final WordGroupRepository groups;
    private final PuzzleService puzzleService;

    public GameService(GameSessionRepository sessions, PuzzleRepository puzzles,
                       WordGroupRepository groups, PuzzleService puzzleService) {
        this.sessions = sessions;
        this.puzzles = puzzles;
        this.groups = groups;
        this.puzzleService = puzzleService;
    }

    @Transactional
    public StartGameResponse start(StartGameRequest request) {
        Puzzle puzzle = puzzles.findByIdAndActiveTrue(request.puzzleId())
                .orElseThrow(() -> new ResourceNotFoundException("Tabuleiro não encontrado."));
        GameSession game = sessions.save(new GameSession(puzzle, request.playerName().trim()));
        List<WordGroup> puzzleGroups = groups.findAllWithWordsByPuzzleId(puzzle.getId());
        List<List<String>> hintGroups = puzzleGroups.stream().map(group -> {
            List<String> words = new ArrayList<>(group.getWords().stream().map(w -> w.getValue()).toList());
            Collections.shuffle(words);
            return words;
        }).toList();
        return new StartGameResponse(game.getId(), game.getPlayerName(), puzzle.getId(), puzzle.getTitle(),
                puzzleGroups.size(), puzzleService.shuffledWords(puzzle.getId()), hintGroups);
    }

    /**
     * Confere uma tentativa. A linha da partida fica travada (PESSIMISTIC_WRITE) até o fim da transação,
     * então dois envios simultâneos não conseguem pontuar o mesmo grupo duas vezes.
     */
    @Transactional
    public GuessResponse guess(UUID gameId, GuessRequest request) {
        GameSession game = sessions.findByIdForUpdate(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Partida não encontrada."));
        if (game.isCompleted()) {
            throw new ConflictException("Partida já concluída.");
        }

        Set<String> selected = request.words().stream().map(GameService::normalize).collect(Collectors.toSet());
        if (selected.size() != WORDS_PER_GROUP) {
            throw new BadRequestException("As quatro palavras devem ser diferentes.");
        }

        List<WordGroup> puzzleGroups = groups.findAllWithWordsByPuzzleId(game.getPuzzle().getId());
        int totalGroups = puzzleGroups.size();

        Set<String> boardWords = puzzleGroups.stream()
                .flatMap(g -> wordsOf(g).stream())
                .collect(Collectors.toSet());
        if (!boardWords.containsAll(selected)) {
            game.registerMistake();
            return wrong(game, totalGroups, false, "Uma ou mais palavras não pertencem ao tabuleiro.");
        }

        WordGroup match = puzzleGroups.stream()
                .filter(g -> wordsOf(g).equals(selected))
                .findFirst()
                .orElse(null);

        if (match == null) {
            boolean oneAway = puzzleGroups.stream()
                    .filter(g -> !game.hasFound(g.getId()))
                    .anyMatch(g -> overlap(wordsOf(g), selected) == WORDS_PER_GROUP - 1);
            game.registerMistake();
            return wrong(game, totalGroups, oneAway, oneAway
                    ? "Quase! Três dessas palavras pertencem ao mesmo grupo."
                    : "Essas palavras não formam um grupo. Tente outra combinação.");
        }

        if (game.hasFound(match.getId())) {
            throw new ConflictException("Você já encontrou esse grupo.");
        }

        game.registerFoundGroup(match.getId(), POINTS_PER_GROUP, totalGroups);
        String message = game.isCompleted()
                ? "Parabéns! Você encontrou todos os grupos."
                : "Boa! Grupo encontrado.";
        return new GuessResponse(true, false, message, match.getCategory(), match.getExplanation(), match.getLevel(),
                game.getScore(), game.getMistakes(), game.groupsFound(), totalGroups, game.isCompleted());
    }

    @Transactional(readOnly = true)
    public GameStateResponse state(UUID gameId) {
        GameSession game = sessions.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Partida não encontrada."));
        Puzzle puzzle = game.getPuzzle();
        long duration = game.getDurationSeconds() != null
                ? game.getDurationSeconds()
                : Duration.between(game.getStartedAt(), LocalDateTime.now()).toSeconds();
        return new GameStateResponse(game.getId(), game.getPlayerName(), puzzle.getId(), puzzle.getTitle(),
                game.getScore(), game.getMistakes(), game.groupsFound(),
                (int) groups.countByPuzzleId(puzzle.getId()), game.isCompleted(),
                duration, game.getStartedAt(), game.getFinishedAt());
    }

    // ----- auxiliares -----

    private GuessResponse wrong(GameSession game, int totalGroups, boolean oneAway, String message) {
        return new GuessResponse(false, oneAway, message, null, null, null,
                game.getScore(), game.getMistakes(), game.groupsFound(), totalGroups, false);
    }

    private static Set<String> wordsOf(WordGroup group) {
        return group.getWords().stream().map(w -> normalize(w.getValue())).collect(Collectors.toSet());
    }

    private static long overlap(Set<String> a, Set<String> b) {
        return a.stream().filter(b::contains).count();
    }

    static String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
