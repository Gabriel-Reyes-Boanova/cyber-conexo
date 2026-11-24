package br.com.cyberconexo.dto;

import br.com.cyberconexo.entity.Difficulty;
import br.com.cyberconexo.entity.Topic;

public record PuzzleSummaryResponse(Long id, String title, String description, Topic topic, Difficulty difficulty) { }
