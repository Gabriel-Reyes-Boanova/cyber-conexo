# 🛡️ Cyber Conexo

Jogo web educativo de associação de palavras sobre **Linux e cibersegurança**. O jogador vê 16 termos e precisa separá-los em 4 grupos de 4 palavras relacionadas, aprendendo com a explicação de cada grupo.

- **7 tabuleiros** em 3 níveis de dificuldade (permissões, arquivos, redes, hardening, criptografia, ameaças, ferramentas e logs)
- **Nota de 0 a 100** calculada no servidor (25 pontos por grupo)
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

Comandos úteis:

```bash
docker compose logs -f backend      # acompanhar a API
docker compose restart backend      # reiniciar só a API
docker compose down                 # parar tudo (mantém os dados)
docker compose down -v              # parar e APAGAR o banco (destrutivo)
docker compose up --build -d        # reconstruir após mudar o código
```

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
- Cada grupo certo vale 25 pontos (máximo 100). Erros são contados, mas não tiram pontos.
- Um grupo não pontua duas vezes na mesma partida.
- Categoria, explicação e nível de um grupo só são enviados depois que ele é descoberto.

## Banco de dados e migrations

| Migration | O que faz |
|---|---|
| `V1__create_schema.sql` | Cria as tabelas e relacionamentos |
| `V2__seed_initial_puzzle.sql` | Primeiro tabuleiro |
| `V3__add_topic_difficulty_level.sql` | Assunto, dificuldade, nível dos grupos e duração da partida |
| `V4__linux_cyber_content.sql` | Remove o grupo de SQL e cadastra o conteúdo Linux e segurança |

O Flyway roda sozinho ao iniciar o backend. **Nunca edite uma migration já aplicada**: crie a próxima (`V5__...`). Tabelas: `puzzle`, `word_group`, `word`, `game_session`, `game_found_group`.

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


