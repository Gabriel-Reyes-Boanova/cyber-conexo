# 🛡️ Cyber Conexo

Jogo web educativo de associação de palavras sobre **serviços e nuvem, Apache/PHP, bancos de dados, JavaScript, Linux e cibersegurança**. O jogador vê 16 termos e precisa separá-los em 4 grupos de 4 palavras relacionadas, aprendendo com a explicação de cada grupo.

- **8 tabuleiros** em 3 níveis de dificuldade. Iniciante: serviços/VM/nuvem, Apache/PHP/bancos de dados e JavaScript. Depois: redes, hardening, criptografia, ameaças, ferramentas e logs
- **Nota de 0 a 100** calculada no servidor (25 pontos por grupo, menos 2 pontos por erro)
- **Dica "quase!"** quando 3 das 4 palavras estão certas
- **Ranking** por nota, erros e tempo
- Roda inteiro em **Docker**, pronto para uma **VM Ubuntu**

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Front | HTML, CSS e JavaScript (ES modules), servido por **Nginx** |
| API | Java 21, Spring Boot 3.5, Spring Web, **Spring Data JPA**, Bean Validation, Actuator |
| Banco | PostgreSQL 17 + **Flyway** (migrations SQL) |
| Admin do banco | pgAdmin 4 |
| Documentação da API | springdoc-openapi (Swagger UI, desligado por padrão) |
| Testes | JUnit 5, Mockito, AssertJ |
| Infra | Docker Compose |

## Arquitetura

```text
Navegador ──HTTP──▶ Nginx (container "frontend", porta 80)
                     ├─ /            → arquivos estáticos (front/public)
                     └─ /api/*       → Spring Boot (container "backend", porta 8080 interna)
                                          Controller → Service → Repository (JPA)
                                                                   │
                                                          PostgreSQL (container "database", interno)
                                                                   ▲
                                                          pgAdmin (somente localhost da VM)
```

Só a porta do Nginx é publicada para a rede. API e banco ficam na rede interna do Docker.

## Estrutura de pastas

```text
cyber-conexo/
├── backend/
│   ├── src/main/java/br/com/cyberconexo/
│   │   ├── config/        CORS opcional
│   │   ├── controller/    Puzzle, Game, Ranking
│   │   ├── dto/           contratos de entrada e saída (records)
│   │   ├── entity/        Puzzle, WordGroup, Word, GameSession
│   │   ├── exception/     erros de negócio + handler global
│   │   ├── repository/    interfaces Spring Data JPA
│   │   └── service/       PuzzleService, GameService, RankingService
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/  V1 a V4
│   ├── src/test/          testes das regras do jogo
│   ├── Dockerfile
│   └── pom.xml
├── front/
│   ├── public/            index.html, css/, js/
│   ├── nginx/             default.conf (proxy, limites, cabeçalhos de segurança)
│   └── Dockerfile
├── docs/
│   ├── CONTEXTO_IA.md     para continuar o projeto em outra IA
│   ├── GUIA_DO_GRUPO.md   estudo e apresentação
│   └── DEPLOY_VM.md       passo a passo na VM Ubuntu
├── compose.yaml
├── .env.example
└── .gitignore
```

## Como rodar (passo a passo)

**Pré-requisito:** Docker Engine com o plugin Compose (ou Docker Desktop). Na VM, veja [docs/DEPLOY_VM.md](docs/DEPLOY_VM.md).

1. **Clone** o repositório e entre na pasta:
   ```bash
   git clone <url-do-repositorio> cyber-conexo
   cd cyber-conexo
   ```
2. **Crie o `.env`** a partir do exemplo e troque as senhas:
   ```bash
   cp .env.example .env
   nano .env
   ```
3. **Suba os containers** (a primeira vez demora, pois compila o Java):
   ```bash
   docker compose up --build -d
   ```
4. **Confira** se está tudo saudável (o backend leva cerca de 40 segundos):
   ```bash
   docker compose ps
   docker compose logs -f backend
   ```
5. **Jogue:** abra `http://localhost` (ou `http://IP-DA-VM`).
6. **pgAdmin:** abra `http://localhost:5050`, faça login com o e-mail e a senha do `.env` e registre o servidor:
   - Host: `database` · Porta: `5432` · Banco: `cyberconexo` · Usuário/senha: os do `.env`
   - Dentro do Docker o host é `database`, nunca `localhost`.

## Comandos do Docker

Rode todos na pasta do projeto (onde fica o `compose.yaml`). Os comandos marcados com **(destrutivo)** apagam dados e não pedem confirmação.

### Subir e atualizar

```bash
docker compose up --build -d              # constrói as imagens e sobe tudo em segundo plano
docker compose up -d                      # sobe sem reconstruir (usa as imagens já prontas)
docker compose up -d --force-recreate     # recria os containers mesmo sem mudanças
docker compose build --no-cache           # reconstrói as imagens do zero, ignorando o cache
```

### Parar e reiniciar

```bash
docker compose stop                       # para os containers, sem removê-los (os dados ficam)
docker compose start                      # liga de novo os containers parados
docker compose restart                    # reinicia todos os containers
docker compose restart backend            # reinicia só a API (troque pelo nome do serviço)
docker compose stop backend               # para só um serviço
docker compose down                       # para e REMOVE os containers e a rede (o banco é mantido)
```

Serviços do projeto: `frontend`, `backend`, `database` e `pgadmin`.

### Apagar dados e resetar

```bash
# (destrutivo) apaga o banco inteiro (partidas, ranking, tabuleiros) e o volume do pgAdmin
docker compose down -v

# (destrutivo) reset completo: apaga o banco e sobe tudo de novo
# o Flyway recria as tabelas e os tabuleiros na inicialização
docker compose down -v && docker compose up --build -d

# (destrutivo) apaga só as partidas e o ranking; os tabuleiros continuam
docker compose exec database psql -U cyberuser -d cyberconexo \
  -c "TRUNCATE game_session CASCADE;"

# (destrutivo) apaga só o volume do banco, mantendo o pgAdmin
docker compose rm -sf database
docker volume ls                          # veja o nome do volume (ex.: cyber-conexo_postgres_data)
docker volume rm cyber-conexo_postgres_data
docker compose up -d database
docker compose restart backend            # o Flyway recria o schema ao iniciar

# remove também as imagens construídas localmente (libera espaço; a próxima subida reconstrói)
docker compose down --rmi local
```

> Se você trocar a senha do banco no `.env`, o volume continua com a senha antiga. Nesse caso é preciso apagar o volume (`docker compose down -v`) para o banco ser criado com a senha nova.

### Ver estado e logs

```bash
docker compose ps                         # estado e saúde dos serviços
docker compose ps -a                      # inclui os containers parados
docker compose logs -f backend            # acompanha a API (Ctrl+C para sair)
docker compose logs --tail 100 backend    # últimas 100 linhas
docker compose logs -f                    # logs de todos os serviços
docker compose top                        # processos dentro dos containers
docker stats --no-stream                  # uso de CPU e memória
docker compose config                     # mostra o compose.yaml já com as variáveis do .env
```

### Entrar nos containers

```bash
docker compose exec database psql -U cyberuser -d cyberconexo   # console SQL do PostgreSQL
docker compose exec backend bash          # terminal dentro da API
docker compose exec frontend sh           # terminal dentro do Nginx
```

No `psql`: `\dt` lista as tabelas, `\d game_session` mostra as colunas, `SELECT * FROM flyway_schema_history;` mostra as migrations aplicadas e `\q` sai.

### Backup e restauração

```bash
# backup
docker compose exec -T database pg_dump -U cyberuser cyberconexo > backup_$(date +%F).sql

# restauração (em banco vazio)
docker compose down -v
docker compose up -d database
docker compose exec -T database psql -U cyberuser -d cyberconexo < backup_AAAA-MM-DD.sql
docker compose up -d
```

### Limpeza de espaço em disco

```bash
docker system df                          # quanto espaço o Docker está usando
docker image prune                        # remove imagens soltas (sem nome)
docker builder prune                      # limpa o cache de construção
docker system prune                       # remove containers parados, redes e imagens soltas sem uso
docker volume ls                          # lista os volumes (confira antes de apagar qualquer um)
docker volume prune                       # (destrutivo) remove volumes sem container associado
```

> Evite `docker system prune -a --volumes` e `docker volume prune` com o projeto parado: eles podem apagar o volume do banco.

### Desenvolvimento sem Docker (opcional)

Precisa de Java 21, Maven e um PostgreSQL local.

```bash
export SPRING_DATASOURCE_PASSWORD=sua_senha
export CORS_ALLOWED_ORIGINS=http://localhost:5500   # se abrir o front pelo Live Server
cd backend && mvn spring-boot:run
```

Com `SWAGGER_ENABLED=true`, a documentação interativa fica em `http://localhost:8080/swagger-ui.html`. Para testes: `mvn test`.

## API

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/puzzles` | Lista os tabuleiros ativos |
| GET | `/api/puzzles/{id}` | Palavras embaralhadas, sem gabarito |
| POST | `/api/games` | Inicia partida. Body: `{"playerName":"ana","puzzleId":1}` |
| POST | `/api/games/{id}/guesses` | Tenta um grupo. Body: `{"words":["CHMOD","CHOWN","UMASK","GETFACL"]}` |
| GET | `/api/games/{id}` | Estado da partida (nota, erros, tempo) |
| GET | `/api/ranking?puzzleId=1&limit=10` | Melhores partidas concluídas |

Erros seguem o formato `{ "status": 409, "error": "Conflict", "message": "...", "timestamp": "..." }`.

## Regras do jogo

- 16 palavras, 4 grupos de 4; o jogador seleciona exatamente 4 por tentativa.
- Cada grupo certo vale 25 pontos (máximo 100); cada tentativa errada reduz 2 pontos, sem deixar a nota ficar negativa.
- Um grupo não pontua duas vezes na mesma partida.
- Categoria, explicação e nível de um grupo só são enviados depois que ele é descoberto.
- Ao concluir, a tela de resultado mostra os quatro grupos e suas explicações, além da opção de voltar ao menu.

## Banco de dados e migrations

| Migration | O que faz |
|---|---|
| `V1__create_schema.sql` | Cria as tabelas e relacionamentos |
| `V2__seed_initial_puzzle.sql` | Primeiro tabuleiro |
| `V3__add_topic_difficulty_level.sql` | Assunto, dificuldade, nível dos grupos e duração da partida |
| `V4__linux_cyber_content.sql` | Remove o grupo de SQL e cadastra o conteúdo Linux e segurança |
| `V5__calendario_e_javascript.sql` | Troca os 2 tabuleiros iniciantes de Linux por conteúdo do calendário (serviços/VM/nuvem e Apache/PHP/bancos) e adiciona JavaScript iniciante |

O Flyway roda sozinho ao iniciar o backend. **Nunca edite uma migration já aplicada**: crie a próxima (`V6__...`). Tabelas: `puzzle`, `word_group`, `word`, `game_session`, `game_found_group`.

Para adicionar um tabuleiro novo, crie uma migration com `INSERT` em `puzzle`, `word_group` (4 grupos, níveis 1 a 4) e `word` (4 palavras por grupo, sem repetir palavra no mesmo tabuleiro, em MAIÚSCULAS).

## Segurança

- A validação e a pontuação ficam no backend; o JavaScript não consegue conceder pontos.
- Consultas via Spring Data (parametrizadas), sem SQL montado por concatenação.
- Dois envios simultâneos não pontuam em dobro (trava `PESSIMISTIC_WRITE` na partida + chave primária em `game_found_group`).
- Nome do jogador validado no backend e exibido no front só com `textContent` (sem XSS).
- Nginx com limite de requisições (mais rígido nas tentativas), cabeçalhos de segurança e CSP.
- Banco e API não são publicados; pgAdmin só em `127.0.0.1`.
- Senhas apenas no `.env` (fora do Git); a API não tem senha padrão.
- Erros nunca expõem stack trace ao cliente.