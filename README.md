# 🛡️ Cyber Conexo

Jogo web educativo de associação de palavras sobre Linux, Segurança da Informação, PostgreSQL e administração de servidores.

## Tecnologias
- Java 21 + Spring Boot 3.5
- Spring Web + Spring JDBC
- PostgreSQL 17
- Flyway (migrations SQL versionadas)
- pgAdmin 4
- HTML, CSS e JavaScript puro
- Docker Compose

> Flyway usa arquivos SQL versionados (`V1__...sql`). JavaScript é usado na interface; não é a ferramenta de migrations deste projeto.

## Como funciona
1. O jogador informa seu nome e inicia uma partida.
2. A API cria uma sessão e devolve 16 palavras embaralhadas.
3. O jogador seleciona quatro palavras.
4. O Spring valida a tentativa consultando o PostgreSQL.
5. Se estiver correta, o backend registra o grupo e soma 25 pontos.
6. Ao encontrar os quatro grupos, a partida termina e mostra a nota final.

O navegador não recebe previamente categorias, explicações ou gabarito. A validação e a pontuação pertencem ao backend.

## Estrutura
```text
cyber-conexo/
├── backend/
│   ├── src/main/java/br/com/cyberconexo/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── repository/
│   │   └── service/
│   ├── src/main/resources/
│   │   ├── db/migration/
│   │   └── static/{css,js}/
│   ├── Dockerfile
│   └── pom.xml
├── docs/
│   ├── CONTEXTO_IA.md
│   └── GUIA_DO_GRUPO.md
├── .env.example
├── .gitignore
└── compose.yaml
```

## Executar
Pré-requisito: Docker Desktop ou Docker Engine + Compose.

1. Copie `.env.example` para `.env` e altere as senhas.
   - PowerShell: `Copy-Item .env.example .env`
   - Linux/macOS: `cp .env.example .env`
2. Na raiz, execute:
   ```bash
   docker compose up --build -d
   ```
3. Confira:
   ```bash
   docker compose ps
   docker compose logs -f backend
   ```

Acessos:
- Jogo: http://localhost:8080
- pgAdmin: http://localhost:5050

No pgAdmin, registre o servidor com host `database`, porta `5432`, banco `cyberconexo` e usuário/senha do `.env`. Dentro do Docker, use `database`, não `localhost`.

Para parar: `docker compose down`. Para apagar também os dados do banco (destrutivo): `docker compose down -v`.

## Banco e migrations
- `V1__create_schema.sql`: cria tabelas e relacionamentos.
- `V2__seed_initial_puzzle.sql`: cadastra puzzle inicial.
Flyway executa automaticamente ao iniciar. Não edite migrations já aplicadas em ambiente compartilhado; crie uma V3, V4 etc.

| Tabela | Finalidade |
|---|---|
| puzzle | Tabuleiros |
| word_group | Categorias e explicações |
| word | Palavras |
| game_session | Partidas, erros e pontuação |
| game_found_group | Grupos já encontrados |

## API
- `GET /api/puzzles/1`: palavras do tabuleiro sem gabarito.
- `POST /api/games`: inicia partida. Body: `{"playerName":"Jogador","puzzleId":1}`
- `POST /api/games/{gameId}/guesses`: tenta um grupo. Body: `{"words":["CHMOD","CHOWN","UMASK","GETFACL"]}`
- `GET /api/games/{gameId}`: consulta progresso.

Cada grupo correto vale 25 pontos. Quatro grupos totalizam 100. Erros são contados, mas não retiram pontos no MVP.

## Puzzle incluído
1. Permissões Linux: CHMOD, CHOWN, UMASK, GETFACL.
2. Operações SQL/CRUD: SELECT, INSERT, UPDATE, DELETE.
3. Navegação Linux: PWD, LS, CD, FIND.
4. Administração Linux: SYSTEMCTL, JOURNALCTL, SS, UFW.

## Segurança e limitações
- Backend valida sessão, palavras, duplicações e grupos já encontrados.
- Senhas ficam no `.env`, que não deve ir para o Git.
- PostgreSQL não publica a porta 5432 no host por padrão.
- MVP sem login, painel administrativo, ranking, HTTPS ou rate limiting.
- O puzzle inicial é curto. Para cumprir 4–8 minutos, criem mais fases e testem com colegas.
- Confirmem com o professor se Spring Boot com Tomcat embarcado atende à alternativa de servidor “similar” ao Apache2 + PHP.

## Próximas melhorias
Mais puzzles, dificuldade progressiva, cronômetro, ranking, painel admin, testes automatizados e deploy em VM Ubuntu.
Consulte `docs/GUIA_DO_GRUPO.md` e `docs/CONTEXTO_IA.md`.
