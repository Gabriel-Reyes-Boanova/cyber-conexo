docker compose pspackage br.com.cyberconexo.repository;

import br.com.cyberconexo.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {

    @Query("select p from Player p where lower(p.name) = lower(:name)")
    Optional<Player> findByNameIgnoreCase(@Param("name") String name);

    /**
     * Cria o jogador só se o nome ainda não existir. O índice único em lower(name) resolve a corrida
     * entre dois acessos simultâneos com o mesmo nome. Devolve 1 se criou, 0 se já existia.
     */
    @Modifying
    @Query(value = """
            insert into player (id, name) values (gen_random_uuid(), :name)
            on conflict ((lower(name))) do nothing
            """, nativeQuery = true)
    int insertIfAbsent(@Param("name") String name);
}
