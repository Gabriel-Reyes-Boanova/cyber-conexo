package br.com.cyberconexo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record GuessRequest(
        @NotNull(message = "Envie as palavras selecionadas.")
        @Size(min = 4, max = 4, message = "Selecione exatamente quatro palavras.")
        List<@NotBlank(message = "Palavra vazia.") @Size(max = 60, message = "Palavra muito longa.") String> words) { }
