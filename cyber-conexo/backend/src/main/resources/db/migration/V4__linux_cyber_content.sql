-- Conteúdo 100% Linux e cibersegurança.
-- O grupo de SQL do puzzle 1 foi trocado e foram criados mais 6 puzzles.

-- ===== Puzzle 1: Fundamentos de Linux =====
UPDATE puzzle SET title = 'Fundamentos de Linux',
    description = 'Comandos essenciais de arquivos, usuários e administração.',
    topic = 'LINUX', difficulty = 'INICIANTE'
WHERE id = 1;

UPDATE word_group SET level = 3 WHERE id = 1;
UPDATE word_group SET level = 1 WHERE id = 3;
UPDATE word_group SET level = 4 WHERE id = 4;

DELETE FROM word WHERE word_group_id = 2;
UPDATE word_group SET level = 2,
    category = 'Usuários e privilégios no Linux',
    explanation = 'useradd cria usuários; usermod altera contas existentes; passwd define ou troca senhas; sudo executa comandos com privilégios elevados de forma controlada e registrada.'
WHERE id = 2;
INSERT INTO word (word_group_id, value) VALUES
(2, 'USERADD'), (2, 'USERMOD'), (2, 'PASSWD'), (2, 'SUDO');

-- ===== Puzzles 2 a 7 =====
INSERT INTO puzzle (id, title, description, topic, difficulty, active) VALUES
(2, 'Arquivos e texto no terminal', 'Ler, copiar, filtrar e compactar arquivos pela linha de comando.', 'LINUX', 'INICIANTE', TRUE),
(3, 'Redes no Linux', 'Diagnóstico, DNS, interfaces e captura de tráfego.', 'LINUX', 'INTERMEDIARIO', TRUE),
(4, 'Hardening e defesa', 'Firewalls, monitoramento, isolamento e acesso remoto seguro.', 'MISTO', 'INTERMEDIARIO', TRUE),
(5, 'Criptografia e senhas', 'Cifras, chaves públicas, hashes e armazenamento de senhas.', 'SEGURANCA', 'INTERMEDIARIO', TRUE),
(6, 'Ameaças e ataques', 'Malware, engenharia social, ataques web e ataques de rede.', 'SEGURANCA', 'AVANCADO', TRUE),
(7, 'Ferramentas e logs de segurança', 'Varredura, análise de tráfego, auditoria e rastros nos logs.', 'MISTO', 'AVANCADO', TRUE);

INSERT INTO word_group (id, puzzle_id, category, explanation, level) VALUES
-- Puzzle 2
(5, 2, 'Ler o conteúdo de arquivos',
 'cat mostra o arquivo inteiro; head mostra as primeiras linhas; tail mostra as últimas (e com -f acompanha logs em tempo real); less permite navegar página a página.', 1),
(6, 2, 'Copiar, mover, remover e criar',
 'cp copia; mv move ou renomeia; rm remove (sem lixeira, cuidado!); mkdir cria diretórios.', 2),
(7, 2, 'Filtrar e transformar texto',
 'grep busca padrões; sed edita fluxos de texto; awk processa colunas e campos; sort ordena linhas.', 3),
(8, 2, 'Empacotar e compactar',
 'tar agrupa arquivos em um só; gzip, bzip2 e xz compactam com diferentes equilíbrios entre velocidade e tamanho.', 4),
-- Puzzle 3
(9, 3, 'Testar conectividade e rota',
 'ping testa se o destino responde; traceroute e tracepath mostram os saltos até o destino; mtr combina ping e traceroute em tempo real.', 1),
(10, 3, 'Consultar DNS',
 'dig, nslookup e host consultam registros DNS; resolvectl mostra a configuração do resolvedor systemd-resolved.', 2),
(11, 3, 'Ver e configurar interfaces',
 'ip é o comando moderno de rede; ifconfig é o legado; nmcli controla o NetworkManager; ethtool mostra detalhes da placa de rede.', 3),
(12, 3, 'Capturar e transferir dados',
 'tcpdump captura pacotes; netcat lê e escreve em conexões TCP/UDP; curl e wget baixam conteúdo por HTTP/HTTPS.', 4),
-- Puzzle 4
(13, 4, 'Firewalls no Linux',
 'iptables e nftables filtram pacotes no kernel; firewalld e ufw são camadas mais simples para configurá-los.', 1),
(14, 4, 'Detecção e monitoramento',
 'fail2ban bloqueia IPs após falhas de login; auditd registra eventos do sistema; aide detecta arquivos alterados; rkhunter procura rootkits.', 2),
(15, 4, 'Controle de acesso obrigatório e isolamento',
 'SELinux e AppArmor restringem o que cada processo pode fazer; seccomp filtra chamadas de sistema; namespaces isolam recursos (base dos containers).', 3),
(16, 4, 'Acesso remoto seguro com SSH',
 'sshd_config configura o servidor; ssh-keygen gera chaves; authorized_keys lista as chaves aceitas; known_hosts guarda as identidades dos servidores já visitados.', 4),
-- Puzzle 5
(17, 5, 'Cifras simétricas',
 'Usam a mesma chave para cifrar e decifrar. AES e ChaCha20 são os padrões atuais; 3DES e Blowfish são antigos.', 1),
(18, 5, 'Criptografia de chave pública',
 'Usam par de chaves. RSA cifra e assina; ECDSA e Ed25519 assinam; Diffie-Hellman permite combinar uma chave secreta por canal inseguro.', 2),
(19, 5, 'Funções de hash',
 'Geram um resumo de tamanho fixo para verificar integridade. SHA-256, SHA-512 e BLAKE2 são seguros; MD5 está quebrado e não deve ser usado para segurança.', 3),
(20, 5, 'Armazenamento seguro de senhas',
 'bcrypt, scrypt, Argon2 e PBKDF2 são lentos de propósito e usam salt, dificultando ataques de força bruta. Hash rápido como SHA-256 puro não é indicado para senhas.', 4),
-- Puzzle 6
(21, 6, 'Engenharia social',
 'Enganam pessoas, não sistemas: phishing (e-mail), vishing (voz), smishing (SMS) e pretexting (história falsa para obter informação).', 1),
(22, 6, 'Tipos de malware',
 'Ransomware sequestra arquivos; trojan se disfarça de programa legítimo; rootkit esconde a presença do invasor; worm se espalha sozinho pela rede.', 2),
(23, 6, 'Ataques a aplicações web',
 'XSS injeta scripts no navegador da vítima; CSRF força ações em nome do usuário logado; SSRF faz o servidor requisitar alvos internos; SQL Injection altera consultas ao banco.', 3),
(24, 6, 'Ataques de rede',
 'DDoS derruba serviços com tráfego massivo; MITM intercepta a comunicação; ARP Spoofing engana a rede local; SYN Flood esgota conexões TCP.', 4),
-- Puzzle 7
(25, 7, 'Varredura e reconhecimento',
 'nmap mapeia hosts e portas; masscan faz varreduras muito rápidas; nikto verifica falhas em servidores web; gobuster descobre diretórios e arquivos expostos. Use apenas em ambientes autorizados.', 1),
(26, 7, 'Análise de tráfego e detecção de intrusão',
 'Wireshark analisa pacotes; Snort, Suricata e Zeek são sistemas de detecção de intrusão e monitoramento de rede.', 2),
(27, 7, 'Auditoria e varredura de vulnerabilidades',
 'Lynis audita o hardening do Linux; OpenSCAP verifica conformidade; Trivy procura vulnerabilidades em imagens e pacotes; ClamAV é um antivírus de código aberto.', 3),
(28, 7, 'Logs de autenticação e sistema',
 'auth.log registra logins e uso de sudo; syslog reúne mensagens gerais; wtmp guarda histórico de logins (lido com last); lastlog mostra o último acesso de cada usuário.', 4);

INSERT INTO word (word_group_id, value) VALUES
(5, 'CAT'), (5, 'HEAD'), (5, 'TAIL'), (5, 'LESS'),
(6, 'CP'), (6, 'MV'), (6, 'RM'), (6, 'MKDIR'),
(7, 'GREP'), (7, 'SED'), (7, 'AWK'), (7, 'SORT'),
(8, 'TAR'), (8, 'GZIP'), (8, 'BZIP2'), (8, 'XZ'),
(9, 'PING'), (9, 'TRACEROUTE'), (9, 'MTR'), (9, 'TRACEPATH'),
(10, 'DIG'), (10, 'NSLOOKUP'), (10, 'HOST'), (10, 'RESOLVECTL'),
(11, 'IP'), (11, 'IFCONFIG'), (11, 'NMCLI'), (11, 'ETHTOOL'),
(12, 'TCPDUMP'), (12, 'NETCAT'), (12, 'CURL'), (12, 'WGET'),
(13, 'UFW'), (13, 'IPTABLES'), (13, 'NFTABLES'), (13, 'FIREWALLD'),
(14, 'FAIL2BAN'), (14, 'AUDITD'), (14, 'AIDE'), (14, 'RKHUNTER'),
(15, 'SELINUX'), (15, 'APPARMOR'), (15, 'SECCOMP'), (15, 'NAMESPACES'),
(16, 'SSHD_CONFIG'), (16, 'SSH-KEYGEN'), (16, 'AUTHORIZED_KEYS'), (16, 'KNOWN_HOSTS'),
(17, 'AES'), (17, 'CHACHA20'), (17, '3DES'), (17, 'BLOWFISH'),
(18, 'RSA'), (18, 'ECDSA'), (18, 'ED25519'), (18, 'DIFFIE-HELLMAN'),
(19, 'SHA256'), (19, 'SHA512'), (19, 'BLAKE2'), (19, 'MD5'),
(20, 'BCRYPT'), (20, 'ARGON2'), (20, 'SCRYPT'), (20, 'PBKDF2'),
(21, 'PHISHING'), (21, 'VISHING'), (21, 'SMISHING'), (21, 'PRETEXTING'),
(22, 'RANSOMWARE'), (22, 'TROJAN'), (22, 'ROOTKIT'), (22, 'WORM'),
(23, 'XSS'), (23, 'CSRF'), (23, 'SSRF'), (23, 'SQL INJECTION'),
(24, 'DDOS'), (24, 'MITM'), (24, 'ARP SPOOFING'), (24, 'SYN FLOOD'),
(25, 'NMAP'), (25, 'MASSCAN'), (25, 'NIKTO'), (25, 'GOBUSTER'),
(26, 'WIRESHARK'), (26, 'SNORT'), (26, 'SURICATA'), (26, 'ZEEK'),
(27, 'LYNIS'), (27, 'OPENSCAP'), (27, 'TRIVY'), (27, 'CLAMAV'),
(28, 'AUTH.LOG'), (28, 'SYSLOG'), (28, 'WTMP'), (28, 'LASTLOG');

SELECT setval(pg_get_serial_sequence('puzzle', 'id'), (SELECT MAX(id) FROM puzzle));
SELECT setval(pg_get_serial_sequence('word_group', 'id'), (SELECT MAX(id) FROM word_group));
SELECT setval(pg_get_serial_sequence('word', 'id'), (SELECT MAX(id) FROM word));
