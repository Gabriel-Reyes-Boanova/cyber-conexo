package br.com.cyberconexo.repository;

import br.com.cyberconexo.entity.Puzzle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PuzzleRepository extends JpaRepository<Puzzle, Long> {

    List<Puzzle> findByActiveTrueOrderByIdAsc();

    Optional<Puzzle> findByIdAndActiveTrue(Long id);
}
