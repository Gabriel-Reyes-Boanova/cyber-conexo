package br.com.cyberconexo.dto;

public record RankingEntryResponse(int position, String playerName, String puzzleTitle,
                                   int score, int mistakes, Integer durationSeconds) { }
