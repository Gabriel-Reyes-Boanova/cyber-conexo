package br.com.cyberconexo.repository;

import br.com.cyberconexo.entity.WordGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface WordGroupRepository extends JpaRepository<WordGroup, Long> {

    /** Grupos do tabuleiro já com as palavras carregadas (evita N+1). */
    @Query("select distinct g from WordGroup g join fetch g.words where g.puzzle.id = :puzzleId")
    List<WordGroup> findAllWithWordsByPuzzleId(@Param("puzzleId") Long puzzleId);

    long countByPuzzleId(Long puzzleId);
}
