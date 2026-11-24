package br.com.cyberconexo.repository;

import br.com.cyberconexo.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {

    /** Só o texto das palavras: o front nunca recebe o grupo a que cada uma pertence. */
    @Query("select w.value from Word w where w.wordGroup.puzzle.id = :puzzleId")
    List<String> findValuesByPuzzleId(@Param("puzzleId") Long puzzleId);
}
