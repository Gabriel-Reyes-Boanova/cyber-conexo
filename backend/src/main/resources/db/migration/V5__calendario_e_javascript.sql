-- V5: conteúdo alinhado ao calendário da disciplina + JavaScript iniciante.
-- Tabuleiros 1 e 2 (iniciante) trocados: saem os comandos de Linux, entram serviços/nuvem e Apache/PHP/bancos.
-- Tabuleiro 8 (novo): JavaScript para iniciantes.

-- Novos assuntos aceitos
ALTER TABLE puzzle DROP CONSTRAINT ck_puzzle_topic;
ALTER TABLE puzzle ADD CONSTRAINT ck_puzzle_topic
    CHECK (topic IN ('LINUX', 'SEGURANCA', 'MISTO', 'INFRA', 'WEB', 'JAVASCRIPT'));

-- Os grupos 1 a 8 mudam de conteúdo, então as partidas antigas desses tabuleiros
-- (e as notas do ranking) deixam de fazer sentido e são removidas.
DELETE FROM game_session WHERE puzzle_id IN (1, 2);
DELETE FROM word WHERE word_group_id BETWEEN 1 AND 8;

-- ===== Tabuleiro 1: Serviços, VM e nuvem (Unidades I e II) =====
UPDATE puzzle SET title = 'Serviços, VM e nuvem',
    description = 'Gerenciar serviços, máquina virtual, on-premise, SaaS, PaaS e IaaS (Unidades I e II).',
    topic = 'INFRA', difficulty = 'INICIANTE'
WHERE id = 1;

UPDATE word_group SET level = 1,
    category = 'Comandos para gerenciar um serviço',
    explanation = 'No Linux, o systemctl controla os serviços: "systemctl start" liga, "stop" desliga, "restart" reinicia e "status" mostra se o serviço está rodando.'
WHERE id = 1;
UPDATE word_group SET level = 2,
    category = 'Exemplos de SaaS (software pronto, é só usar)',
    explanation = 'No SaaS você só usa o programa pelo navegador ou app, e o fornecedor cuida de tudo o resto. Gmail, Zoom, Dropbox e Canva são exemplos.'
WHERE id = 2;
UPDATE word_group SET level = 3,
    category = 'Modelos de entrega de TI',
    explanation = 'On-premise: os servidores ficam na sua empresa e você cuida de tudo. IaaS: você aluga a infraestrutura (servidor virtual). PaaS: você aluga uma plataforma para rodar seu código. SaaS: você usa o software pronto.'
WHERE id = 3;
UPDATE word_group SET level = 4,
    category = 'Instalação da máquina virtual (VM)',
    explanation = 'O hipervisor (como o VirtualBox) cria e executa máquinas virtuais. A ISO é a imagem de instalação do sistema operacional. O snapshot salva o estado da VM para você voltar se algo der errado.'
WHERE id = 4;

INSERT INTO word (word_group_id, value) VALUES
(1, 'START'), (1, 'STOP'), (1, 'RESTART'), (1, 'STATUS'),
(2, 'GMAIL'), (2, 'ZOOM'), (2, 'DROPBOX'), (2, 'CANVA'),
(3, 'ON-PREMISE'), (3, 'SAAS'), (3, 'PAAS'), (3, 'IAAS'),
(4, 'VIRTUALBOX'), (4, 'HIPERVISOR'), (4, 'ISO'), (4, 'SNAPSHOT');

-- ===== Tabuleiro 2: Apache, PHP e bancos de dados (Unidades III, IV e V) =====
UPDATE puzzle SET title = 'Apache, PHP e bancos de dados',
    description = 'Servidor web HTTPD, PHP, PostgreSQL e MySQL (Unidades III a V).',
    topic = 'WEB', difficulty = 'INICIANTE'
WHERE id = 2;

UPDATE word_group SET level = 1,
    category = 'Comandos SQL do dia a dia',
    explanation = 'SELECT consulta dados; INSERT adiciona registros; UPDATE altera registros; DELETE remove registros. Cuidado com UPDATE e DELETE sem WHERE: eles atingem a tabela inteira.'
WHERE id = 5;
UPDATE word_group SET level = 2,
    category = 'Sistemas de banco de dados',
    explanation = 'PostgreSQL e MySQL são os bancos estudados na disciplina. MariaDB nasceu como uma cópia (fork) do MySQL. SQLite guarda o banco em um único arquivo, sem servidor.'
WHERE id = 6;
UPDATE word_group SET level = 3,
    category = 'Linguagens usadas em páginas web',
    explanation = 'HTML monta a estrutura da página; CSS cuida da aparência; JavaScript dá comportamento no navegador; PHP roda no servidor (integrado ao Apache) e gera a página.'
WHERE id = 7;
UPDATE word_group SET level = 4,
    category = 'Servidores web',
    explanation = 'O Apache HTTPD (estudo de caso da disciplina) recebe os pedidos do navegador e entrega as páginas. Nginx, IIS (da Microsoft) e Lighttpd são outros servidores web.'
WHERE id = 8;

INSERT INTO word (word_group_id, value) VALUES
(5, 'SELECT'), (5, 'INSERT'), (5, 'UPDATE'), (5, 'DELETE'),
(6, 'POSTGRESQL'), (6, 'MYSQL'), (6, 'MARIADB'), (6, 'SQLITE'),
(7, 'HTML'), (7, 'CSS'), (7, 'JAVASCRIPT'), (7, 'PHP'),
(8, 'APACHE'), (8, 'NGINX'), (8, 'IIS'), (8, 'LIGHTTPD');

-- ===== Tabuleiro 8: JavaScript para iniciantes =====
INSERT INTO puzzle (id, title, description, topic, difficulty, active) VALUES
(8, 'JavaScript para iniciantes', 'Tipos de dados, variáveis, repetições e mensagens na tela.', 'JAVASCRIPT', 'INICIANTE', TRUE);

INSERT INTO word_group (id, puzzle_id, category, explanation, level) VALUES
(29, 8, 'Tipos de dados',
 'String é texto; number é número; boolean é verdadeiro ou falso (true/false); array é uma lista de valores.', 1),
(30, 8, 'Palavras para declarar algo',
 'var, let e const criam variáveis (prefira let e const); function cria uma função, um bloco de código que você pode chamar várias vezes.', 2),
(31, 8, 'Laços de repetição',
 'for repete um número conhecido de vezes; while repete enquanto a condição for verdadeira; do while executa pelo menos uma vez; forEach percorre cada item de um array.', 3),
(32, 8, 'Mostrar e pedir informações',
 'console.log escreve no console do navegador (tecla F12); alert mostra uma janelinha de aviso; prompt pede um texto ao usuário; confirm pergunta OK ou Cancelar.', 4);

INSERT INTO word (word_group_id, value) VALUES
(29, 'STRING'), (29, 'NUMBER'), (29, 'BOOLEAN'), (29, 'ARRAY'),
(30, 'VAR'), (30, 'LET'), (30, 'CONST'), (30, 'FUNCTION'),
(31, 'FOR'), (31, 'WHILE'), (31, 'DO WHILE'), (31, 'FOREACH'),
(32, 'CONSOLE.LOG'), (32, 'ALERT'), (32, 'PROMPT'), (32, 'CONFIRM');

SELECT setval(pg_get_serial_sequence('puzzle', 'id'), (SELECT MAX(id) FROM puzzle));
SELECT setval(pg_get_serial_sequence('word_group', 'id'), (SELECT MAX(id) FROM word_group));
SELECT setval(pg_get_serial_sequence('word', 'id'), (SELECT MAX(id) FROM word));
