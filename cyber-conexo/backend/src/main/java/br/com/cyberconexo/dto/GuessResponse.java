package br.com.cyberconexo.dto;

/**
 * Resultado de uma tentativa. category, explanation e level só vêm preenchidos
 * quando o grupo foi acertado — o gabarito nunca vaza em erros.
 */
public record GuessResponse(boolean correct, boolean oneAway, String message,
                            String category, String explanation, Integer level,
                            int score, int mistakes, int groupsFound, int totalGroups, boolean completed) { }
