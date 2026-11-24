package br.com.cyberconexo.dto;

import br.com.cyberconexo.entity.Difficulty;
import br.com.cyberconexo.entity.Topic;
import java.util.List;

/** Tabuleiro público: só as palavras embaralhadas, sem grupos nem explicações. */
public record PuzzleResponse(Long id, String title, String description, Topic topic, Difficulty difficulty,
                             List<String> words) { }
