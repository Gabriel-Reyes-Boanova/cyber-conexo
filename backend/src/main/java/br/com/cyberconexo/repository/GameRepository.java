package br.com.cyberconexo.repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public class GameRepository {
 private final JdbcTemplate jdbc;
 public GameRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }
 public boolean puzzleExists(Long id) {
  Boolean found=jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM puzzle WHERE id=? AND active=TRUE)", Boolean.class,id);
  return Boolean.TRUE.equals(found);
 }
 public Map<String,Object> puzzle(Long id) {
  return jdbc.queryForMap("SELECT id,title,description FROM puzzle WHERE id=?",id);
 }
 public List<String> words(Long id) {
  return jdbc.queryForList("SELECT w.value FROM word w JOIN word_group g ON g.id=w.word_group_id WHERE g.puzzle_id=? ORDER BY random()",String.class,id);
 }
 public UUID createGame(Long puzzleId,String name) {
  UUID id=UUID.randomUUID();
  jdbc.update("INSERT INTO game_session(id,puzzle_id,player_name) VALUES(?,?,?)",id,puzzleId,name.trim());
  return id;
 }
 public Map<String,Object> game(UUID id) {
  return jdbc.queryForMap("SELECT id,puzzle_id,player_name,score,mistakes,completed,started_at,finished_at FROM game_session WHERE id=?",id);
 }
 public boolean wordsBelongToPuzzle(Long puzzleId,List<String> words) {
  String marks=String.join(",",Collections.nCopies(4,"?"));
  Object[] args=new Object[5]; args[0]=puzzleId;
  for(int i=0;i<4;i++) args[i+1]=words.get(i);
  Integer n=jdbc.queryForObject("SELECT COUNT(DISTINCT UPPER(w.value)) FROM word w JOIN word_group g ON g.id=w.word_group_id WHERE g.puzzle_id=? AND UPPER(w.value) IN ("+marks+")",Integer.class,args);
  return n!=null && n==4;
 }
 public Map<String,Object> matchingGroup(Long puzzleId,List<String> words) {
  String marks=String.join(",",Collections.nCopies(4,"?"));
  Object[] args=new Object[5]; args[0]=puzzleId;
  for(int i=0;i<4;i++) args[i+1]=words.get(i);
  List<Map<String,Object>> rows=jdbc.queryForList("SELECT g.id group_id,g.category,g.explanation FROM word_group g JOIN word w ON w.word_group_id=g.id WHERE g.puzzle_id=? AND UPPER(w.value) IN ("+marks+") GROUP BY g.id,g.category,g.explanation HAVING COUNT(DISTINCT UPPER(w.value))=4",args);
  return rows.isEmpty()?null:rows.get(0);
 }
 public boolean found(UUID gameId,Long groupId) {
  Integer n=jdbc.queryForObject("SELECT COUNT(*) FROM game_found_group WHERE game_session_id=? AND word_group_id=?",Integer.class,gameId,groupId);
  return n!=null && n>0;
 }
 public void addFound(UUID gameId,Long groupId) {
  jdbc.update("INSERT INTO game_found_group(game_session_id,word_group_id) VALUES(?,?)",gameId,groupId);
 }
 public void mistake(UUID gameId) { jdbc.update("UPDATE game_session SET mistakes=mistakes+1 WHERE id=?",gameId); }
 public void addScore(UUID gameId,boolean complete) {
  jdbc.update("UPDATE game_session SET score=score+25,completed=?,finished_at=CASE WHEN ? THEN CURRENT_TIMESTAMP ELSE finished_at END WHERE id=?",complete,complete,gameId);
 }
 public int foundCount(UUID id) {
  Integer n=jdbc.queryForObject("SELECT COUNT(*) FROM game_found_group WHERE game_session_id=?",Integer.class,id); return n==null?0:n;
 }
 public int groupCount(Long id) {
  Integer n=jdbc.queryForObject("SELECT COUNT(*) FROM word_group WHERE puzzle_id=?",Integer.class,id); return n==null?0:n;
 }
}
