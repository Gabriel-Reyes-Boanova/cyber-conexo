package br.com.cyberconexo.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record StartGameRequest(
        @NotNull(message = "Entre com seu nome antes de jogar.")
        UUID playerId,

        @NotNull(message = "Escolha um tabuleiro.")
        Long puzzleId) { }
