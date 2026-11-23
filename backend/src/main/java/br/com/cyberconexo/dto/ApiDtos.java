package br.com.cyberconexo.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
public final class ApiDtos {
 private ApiDtos() {}
 public record PuzzleResponse(Long id, String title, String description, List<String> words) {}
 public record StartGameRequest(@NotBlank @Size(max=80) String playerName, @NotNull Long puzzleId) {}
 public record StartGameResponse(UUID gameId, String playerName, int score, int mistakes, boolean completed, List<String> words) {}
 public record GuessRequest(@NotEmpty @Size(min=4,max=4) List<@NotBlank String> words) {}
 public record GuessResponse(boolean correct, String message, String category, String explanation, int score, int mistakes, int groupsFound, boolean completed) {}
 public record GameStateResponse(UUID gameId, String playerName, int score, int mistakes, int groupsFound, int totalGroups, boolean completed, LocalDateTime startedAt, LocalDateTime finishedAt) {}
}
