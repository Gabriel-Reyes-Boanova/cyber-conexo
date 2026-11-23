INSERT INTO puzzle (id, title, description, active)
VALUES (1, 'Fundamentos Linux e Segurança', 'Encontre quatro grupos de palavras relacionadas.', TRUE);
INSERT INTO word_group (id, puzzle_id, category, explanation) VALUES
(1, 1, 'Permissões e controle de acesso no Linux',
 'chmod altera permissões; chown altera proprietário e grupo; umask define a máscara padrão; getfacl consulta ACLs.'),
(2, 1, 'Operações SQL usadas em CRUD',
 'SELECT consulta dados; INSERT adiciona registros; UPDATE altera registros; DELETE remove registros. Tenha cuidado com UPDATE e DELETE sem WHERE.'),
(3, 1, 'Navegação e localização no Linux',
 'pwd mostra o diretório atual; ls lista conteúdo; cd muda de diretório; find procura arquivos e diretórios.'),
(4, 1, 'Administração e diagnóstico de Linux',
 'systemctl gerencia serviços systemd; journalctl consulta logs; ss exibe sockets; ufw facilita a configuração do firewall.');
INSERT INTO word (word_group_id, value) VALUES
(1, 'CHMOD'), (1, 'CHOWN'), (1, 'UMASK'), (1, 'GETFACL'),
(2, 'SELECT'), (2, 'INSERT'), (2, 'UPDATE'), (2, 'DELETE'),
(3, 'PWD'), (3, 'LS'), (3, 'CD'), (3, 'FIND'),
(4, 'SYSTEMCTL'), (4, 'JOURNALCTL'), (4, 'SS'), (4, 'UFW');
SELECT setval(pg_get_serial_sequence('puzzle', 'id'), (SELECT MAX(id) FROM puzzle));
SELECT setval(pg_get_serial_sequence('word_group', 'id'), (SELECT MAX(id) FROM word_group));
SELECT setval(pg_get_serial_sequence('word', 'id'), (SELECT MAX(id) FROM word));
