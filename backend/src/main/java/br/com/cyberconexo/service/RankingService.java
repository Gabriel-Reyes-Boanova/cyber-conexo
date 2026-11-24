package br.com.cyberconexo.service;

import br.com.cyberconexo.dto.RankingEntryResponse;
import br.com.cyberconexo.entity.GameSession;
import br.com.cyberconexo.repository.GameSessionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

/** Ranking: maior nota, depois menos erros, depois menor tempo. Só partidas concluídas. */
@Service
public class RankingService {

    private static final int MAX_LIMIT = 50;

    private final GameSessionRepository sessions;

    public RankingService(GameSessionRepository sessions) {
        this.sessions = sessions;
    }

    @Transactional(readOnly = true)
    public List<RankingEntryResponse> top(Long puzzleId, int limit) {
        PageRequest page = PageRequest.of(0, Math.max(1, Math.min(limit, MAX_LIMIT)));
        List<GameSession> rows = puzzleId == null
                ? sessions.findRanking(page)
                : sessions.findRankingByPuzzle(puzzleId, page);
        return IntStream.range(0, rows.size())
                .mapToObj(i -> {
                    GameSession g = rows.get(i);
                    return new RankingEntryResponse(i + 1, g.getPlayerName(), g.getPuzzle().getTitle(),
                            g.getScore(), g.getMistakes(), g.getDurationSeconds());
                })
                .toList();
    }
}
