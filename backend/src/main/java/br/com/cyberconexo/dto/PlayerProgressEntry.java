package br.com.cyberconexo.dto;

/** Melhor partida concluída do jogador em um tabuleiro. */
public record PlayerProgressEntry(Long puzzleId, int score, int mistakes, Integer durationSeconds) { }
