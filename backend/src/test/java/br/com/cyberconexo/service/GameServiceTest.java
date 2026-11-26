package br.com.cyberconexo.service;

import br.com.cyberconexo.dto.GuessRequest;
import br.com.cyberconexo.dto.GuessResponse;
import br.com.cyberconexo.entity.GameSession;
import br.com.cyberconexo.entity.Player;
import br.com.cyberconexo.entity.Puzzle;
import br.com.cyberconexo.entity.Word;
import br.com.cyberconexo.entity.WordGroup;
import br.com.cyberconexo.exception.BadRequestException;
import br.com.cyberconexo.exception.ConflictException;
import br.com.cyberconexo.repository.GameSessionRepository;
import br.com.cyberconexo.repository.PlayerRepository;
import br.com.cyberconexo.repository.PuzzleRepository;
import br.com.cyberconexo.repository.WordGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock GameSessionRepository sessions;
    @Mock PuzzleRepository puzzles;
    @Mock PlayerRepository players;
    @Mock WordGroupRepository groups;
    @Mock PuzzleService puzzleService;

    GameService service;
    GameSession game;
    UUID gameId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GameService(sessions, puzzles, players, groups, puzzleService);

        Puzzle puzzle = BeanUtils.instantiateClass(Puzzle.class);
        ReflectionTestUtils.setField(puzzle, "id", 1L);
        Player player = BeanUtils.instantiateClass(Player.class);
        ReflectionTestUtils.setField(player, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(player, "name", "Aluno");
        game = new GameSession(puzzle, player);

        List<WordGroup> puzzleGroups = List.of(
                group(1L, 3, "CHMOD", "CHOWN", "UMASK", "GETFACL"),
                group(2L, 2, "USERADD", "USERMOD", "PASSWD", "SUDO"),
                group(3L, 1, "PWD", "LS", "CD", "FIND"),
                group(4L, 4, "SYSTEMCTL", "JOURNALCTL", "SS", "UFW"));

        lenient().when(sessions.findByIdForUpdate(gameId)).thenReturn(Optional.of(game));
        lenient().when(groups.findAllWithWordsByPuzzleId(1L)).thenReturn(puzzleGroups);
    }

    @Test
    void correctGuessScoresTwentyFiveAndRevealsOnlyThatGroup() {
        GuessResponse r = service.guess(gameId, req("chmod", "CHOWN", "umask", "GETFACL"));

        assertThat(r.correct()).isTrue();
        assertThat(r.score()).isEqualTo(25);
        assertThat(r.groupsFound()).isEqualTo(1);
        assertThat(r.category()).isEqualTo("Categoria 1");
        assertThat(r.completed()).isFalse();
    }

    @Test
    void wrongGuessCountsMistakeWithoutRevealingAnswer() {
        GuessResponse r = service.guess(gameId, req("CHMOD", "LS", "SUDO", "UFW"));

        assertThat(r.correct()).isFalse();
        assertThat(r.oneAway()).isFalse();
        assertThat(r.mistakes()).isEqualTo(1);
        assertThat(r.score()).isZero();
        assertThat(r.category()).isNull();
        assertThat(r.explanation()).isNull();
    }

    @Test
    void wrongGuessReducesScoreByTwoPoints() {
        service.guess(gameId, req("CHMOD", "CHOWN", "UMASK", "GETFACL"));

        GuessResponse r = service.guess(gameId, req("CHMOD", "LS", "SUDO", "UFW"));

        assertThat(r.mistakes()).isEqualTo(1);
        assertThat(r.score()).isEqualTo(23);
    }

    @Test
    void mistakesBeforeFindingGroupsCannotMakeScoreNegative() {
        service.guess(gameId, req("CHMOD", "LS", "SUDO", "UFW"));
        service.guess(gameId, req("CHMOD", "LS", "SUDO", "UFW"));

        GuessResponse r = service.guess(gameId, req("CHMOD", "CHOWN", "UMASK", "GETFACL"));

        assertThat(r.score()).isEqualTo(21);
    }

    @Test
    void threeOfFourWordsIsReportedAsOneAway() {
        GuessResponse r = service.guess(gameId, req("CHMOD", "CHOWN", "UMASK", "UFW"));

        assertThat(r.correct()).isFalse();
        assertThat(r.oneAway()).isTrue();
        assertThat(r.mistakes()).isEqualTo(1);
    }

    @Test
    void sameGroupCannotScoreTwice() {
        service.guess(gameId, req("CHMOD", "CHOWN", "UMASK", "GETFACL"));

        assertThatThrownBy(() -> service.guess(gameId, req("CHMOD", "CHOWN", "UMASK", "GETFACL")))
                .isInstanceOf(ConflictException.class);
        assertThat(game.getScore()).isEqualTo(25);
    }

    @Test
    void repeatedWordsAreRejected() {
        assertThatThrownBy(() -> service.guess(gameId, req("CHMOD", "CHMOD", "UMASK", "GETFACL")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void findingAllGroupsCompletesGameWithMaximumScore() {
        service.guess(gameId, req("CHMOD", "CHOWN", "UMASK", "GETFACL"));
        service.guess(gameId, req("USERADD", "USERMOD", "PASSWD", "SUDO"));
        service.guess(gameId, req("PWD", "LS", "CD", "FIND"));
        GuessResponse last = service.guess(gameId, req("SYSTEMCTL", "JOURNALCTL", "SS", "UFW"));

        assertThat(last.completed()).isTrue();
        assertThat(last.score()).isEqualTo(100);
        assertThat(game.getDurationSeconds()).isNotNull();

        assertThatThrownBy(() -> service.guess(gameId, req("PWD", "LS", "CD", "FIND")))
                .isInstanceOf(ConflictException.class);
    }

    // ----- helpers -----

    private static GuessRequest req(String... words) {
        return new GuessRequest(List.of(words));
    }

    private static WordGroup group(long id, int level, String... values) {
        WordGroup g = BeanUtils.instantiateClass(WordGroup.class);
        ReflectionTestUtils.setField(g, "id", id);
        ReflectionTestUtils.setField(g, "level", level);
        ReflectionTestUtils.setField(g, "category", "Categoria " + id);
        ReflectionTestUtils.setField(g, "explanation", "Explicação " + id);
        List<Word> words = new ArrayList<>();
        for (String v : values) {
            Word w = BeanUtils.instantiateClass(Word.class);
            ReflectionTestUtils.setField(w, "value", v);
            words.add(w);
        }
        ReflectionTestUtils.setField(g, "words", words);
        return g;
    }
}
