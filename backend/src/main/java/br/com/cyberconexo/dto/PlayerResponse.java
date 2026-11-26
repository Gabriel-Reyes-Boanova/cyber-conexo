package br.com.cyberconexo.dto;

import java.util.List;
import java.util.UUID;

/** Jogador + o que ele já concluiu. newPlayer é true só no primeiro acesso com aquele nome. */
public record PlayerResponse(UUID playerId, String name, boolean newPlayer, List<PlayerProgressEntry> progress) { }
