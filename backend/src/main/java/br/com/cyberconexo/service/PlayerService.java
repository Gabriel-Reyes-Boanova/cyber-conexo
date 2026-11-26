package br.com.cyberconexo.service;

import br.com.cyberconexo.dto.LoginRequest;
import br.com.cyberconexo.dto.PlayerProgressEntry;
import br.com.cyberconexo.dto.PlayerResponse;
import br.com.cyberconexo.entity.GameSession;
import br.com.cyberconexo.entity.Player;
import br.com.cyberconexo.exception.ResourceNotFoundException;
import br.com.cyberconexo.repository.GameSessionRepository;
import br.com.cyberconexo.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Identifica o jogador pelo nome e devolve o que ele já concluiu. */
@Service
public class PlayerService {

    /** Mesma ordem do ranking: maior nota, menos erros, menor tempo. */
    private static final Comparator<GameSession> BEST_FIRST = Comparator
            .comparingInt(GameSession::getScore).reversed()
            .thenComparingInt(GameSession::getMistakes)
            .thenComparing(g -> g.getDurationSeconds() == null ? Integer.MAX_VALUE : g.getDurationSeconds());

    private final PlayerRepository players;
    private final GameSessionRepository sessions;

    public PlayerService(PlayerRepository players, GameSessionRepository sessions) {
        this.players = players;
        this.sessions = sessions;
    }

    /** Se o nome já existe, devolve o jogador com o histórico; senão, cria. */
    @Transactional
    public PlayerResponse login(LoginRequest request) {
        String name = request.name().trim();
        boolean created = players.insertIfAbsent(name) > 0;
        Player player = players.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado."));
        return response(player, created);
    }

    @Transactional(readOnly = true)
    public PlayerResponse get(UUID id) {
        Player player = players.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jogador não encontrado."));
        return response(player, false);
    }

    private PlayerResponse response(Player player, boolean created) {
        return new PlayerResponse(player.getId(), player.getName(), created, progress(player.getId()));
    }

    /** Uma linha por tabuleiro concluído, com a melhor partida. */
    private List<PlayerProgressEntry> progress(UUID playerId) {
        Map<Long, GameSession> best = new LinkedHashMap<>();
        for (GameSession g : sessions.findCompletedByPlayer(playerId)) {
            best.merge(g.getPuzzle().getId(), g, (a, b) -> BEST_FIRST.compare(a, b) <= 0 ? a : b);
        }
        return best.values().stream()
                .map(g -> new PlayerProgressEntry(g.getPuzzle().getId(), g.getScore(), g.getMistakes(), g.getDurationSeconds()))
                .toList();
    }
}
