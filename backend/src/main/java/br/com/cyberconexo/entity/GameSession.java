package br.com.cyberconexo.entity;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Uma partida. A pontuação é sempre calculada no backend. */
@Entity
@Table(name = "game_session")
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "puzzle_id", nullable = false)
    private Puzzle puzzle;

    @Column(name = "player_name", nullable = false, length = 80)
    private String playerName;

    @Column(nullable = false)
    private int score = 0;

    @Column(nullable = false)
    private int mistakes = 0;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    /** Grupos já encontrados (tabela game_found_group). O Set impede pontuar o mesmo grupo duas vezes. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "game_found_group", joinColumns = @JoinColumn(name = "game_session_id"))
    @Column(name = "word_group_id", nullable = false)
    private Set<Long> foundGroupIds = new HashSet<>();

    protected GameSession() { }

    public GameSession(Puzzle puzzle, String playerName) {
        this.puzzle = puzzle;
        this.playerName = playerName;
        this.startedAt = LocalDateTime.now();
    }

    // ----- regras da partida -----

    public void registerMistake() {
        this.mistakes++;
        this.score = Math.max(0, this.score - 2);
    }

    /** Marca o grupo como encontrado e recalcula pontos; encerra a partida se foi o último. */
    public void registerFoundGroup(Long groupId, int pointsPerGroup, int totalGroups) {
        if (!foundGroupIds.add(groupId)) {
            return; // já contado: nunca pontua duas vezes
        }
        this.score = Math.max(0, foundGroupIds.size() * pointsPerGroup - mistakes * 2);
        if (foundGroupIds.size() >= totalGroups) {
            this.completed = true;
            this.finishedAt = LocalDateTime.now();
            this.durationSeconds = (int) Duration.between(startedAt, finishedAt).toSeconds();
        }
    }

    public boolean hasFound(Long groupId) {
        return foundGroupIds.contains(groupId);
    }

    public int groupsFound() {
        return foundGroupIds.size();
    }

    // ----- getters -----

    public UUID getId() { return id; }
    public Puzzle getPuzzle() { return puzzle; }
    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public int getMistakes() { return mistakes; }
    public boolean isCompleted() { return completed; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public Set<Long> getFoundGroupIds() { return foundGroupIds; }
}
