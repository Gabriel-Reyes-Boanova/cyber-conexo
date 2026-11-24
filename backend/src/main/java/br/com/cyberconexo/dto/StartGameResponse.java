package br.com.cyberconexo.dto;

import java.util.List;
import java.util.UUID;

public record StartGameResponse(UUID gameId, String playerName, Long puzzleId, String puzzleTitle,
                                int totalGroups, List<String> words, List<List<String>> hintGroups) { }
