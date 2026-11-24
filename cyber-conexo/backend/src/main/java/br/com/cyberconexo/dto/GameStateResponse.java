package br.com.cyberconexo.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record GameStateResponse(UUID gameId, String playerName, Long puzzleId, String puzzleTitle,
                                int score, int mistakes, int groupsFound, int totalGroups, boolean completed,
                                long durationSeconds, LocalDateTime startedAt, LocalDateTime finishedAt) { }
