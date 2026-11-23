package br.com.cyberconexo.service;
import br.com.cyberconexo.dto.ApiDtos.*;
import br.com.cyberconexo.repository.GameRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.sql.Timestamp;
import java.util.*;
@Service
public class GameService {
 private final GameRepository repo;
 public GameService(GameRepository repo){this.repo=repo;}
 public PuzzleResponse puzzle(Long id){
  if(!repo.puzzleExists(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Tabuleiro não encontrado.");
  Map<String,Object> p=repo.puzzle(id);
  return new PuzzleResponse(((Number)p.get("id")).longValue(),(String)p.get("title"),(String)p.get("description"),repo.words(id));
 }
 @Transactional
 public StartGameResponse start(StartGameRequest r){
  if(!repo.puzzleExists(r.puzzleId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Tabuleiro não encontrado.");
  UUID id=repo.createGame(r.puzzleId(),r.playerName());
  return new StartGameResponse(id,r.playerName().trim(),0,0,false,repo.words(r.puzzleId()));
 }
 @Transactional
 public GuessResponse guess(UUID id,GuessRequest request){
  Map<String,Object> game=getGame(id);
  if(Boolean.TRUE.equals(game.get("completed"))) throw new ResponseStatusException(HttpStatus.CONFLICT,"Partida concluída.");
  List<String> words=request.words().stream().map(String::trim).map(s->s.toUpperCase(Locale.ROOT)).toList();
  if(new HashSet<>(words).size()!=4) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"As quatro palavras devem ser diferentes.");
  Long puzzleId=((Number)game.get("puzzle_id")).longValue();
  if(!repo.wordsBelongToPuzzle(puzzleId,words)){
   repo.mistake(id); return result(false,"Uma ou mais palavras não pertencem ao tabuleiro.",null,null,id,puzzleId);
  }
  Map<String,Object> group=repo.matchingGroup(puzzleId,words);
  if(group==null){
   repo.mistake(id); return result(false,"Essas palavras não formam um grupo. Tente outra combinação.",null,null,id,puzzleId);
  }
  Long groupId=((Number)group.get("group_id")).longValue();
  if(repo.found(id,groupId)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Você já encontrou esse grupo.");
  repo.addFound(id,groupId);
  boolean complete=repo.foundCount(id)==repo.groupCount(puzzleId);
  repo.addScore(id,complete);
  return result(true,"Boa! Grupo encontrado.",(String)group.get("category"),(String)group.get("explanation"),id,puzzleId);
 }
 public GameStateResponse state(UUID id){
  Map<String,Object> g=getGame(id); Long puzzleId=((Number)g.get("puzzle_id")).longValue();
  Timestamp start=(Timestamp)g.get("started_at"), finish=(Timestamp)g.get("finished_at");
  return new GameStateResponse(id,(String)g.get("player_name"),((Number)g.get("score")).intValue(),((Number)g.get("mistakes")).intValue(),repo.foundCount(id),repo.groupCount(puzzleId),Boolean.TRUE.equals(g.get("completed")),start==null?null:start.toLocalDateTime(),finish==null?null:finish.toLocalDateTime());
 }
 private Map<String,Object> getGame(UUID id){
  try{return repo.game(id);}catch(org.springframework.dao.EmptyResultDataAccessException e){throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Partida não encontrada.");}
 }
 private GuessResponse result(boolean correct,String msg,String category,String explanation,UUID id,Long puzzleId){
  GameStateResponse s=state(id);
  return new GuessResponse(correct,msg,category,explanation,s.score(),s.mistakes(),s.groupsFound(),s.completed());
 }
}
