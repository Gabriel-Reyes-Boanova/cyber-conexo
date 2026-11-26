package br.com.cyberconexo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Informe seu nome.")
        @Size(min = 2, max = 30, message = "O nome deve ter entre 2 e 30 caracteres.")
        @Pattern(regexp = "^[\\p{L}\\p{N} _.-]+$", message = "Use apenas letras, números, espaço, ponto, hífen e sublinhado.")
        String name) { }
