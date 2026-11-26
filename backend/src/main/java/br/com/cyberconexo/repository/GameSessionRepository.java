package br.com.cyberconexo.repository;

import br.com.cyberconexo.entity.GameSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameSessionRepository extends JpaRepository<GameSession, UUID> {

    /** Trava a linha até o fim da transação: dois envios simultâneos não pontuam em dobro. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from GameSession g where g.id = :id")
    Optional<GameSession> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            select g from GameSession g join fetch g.puzzle join fetch g.player
            where g.completed = true
            order by g.score desc, g.mistakes asc, g.durationSeconds asc
            """)
    List<GameSession> findRanking(Pageable pageable);

    @Query("""
            select g from GameSession g join fetch g.puzzle join fetch g.player
            where g.completed = true and g.puzzle.id = :puzzleId
            order by g.score desc, g.mistakes asc, g.durationSeconds asc
            """)
    List<GameSession> findRankingByPuzzle(@Param("puzzleId") Long puzzleId, Pageable pageable);

    @Query("""
            select g from GameSession g join fetch g.puzzle
            where g.completed = true and g.player.id = :playerId
            """)
    List<GameSession> findCompletedByPlayer(@Param("playerId") UUID playerId);
}
