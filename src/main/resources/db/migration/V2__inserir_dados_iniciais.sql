INSERT INTO setores (nome, sigla) VALUES
('Coordenação de Tecnologia da Informação', 'COTIC'),
('Manutenção e Infraestrutura', 'MANUT');

INSERT INTO categorias (nome, descricao, setor_id) VALUES
('Redes e internet', 'Problemas com Wi-Fi e rede cabeada.', 1),
('Computadores e projetores', 'Suporte aos equipamentos dos laboratórios e salas.', 1),
('Elétrica', 'Problemas com iluminação e instalações elétricas.', 2),
('Mobiliário', 'Reparos em mesas, cadeiras e portas.', 2);

INSERT INTO usuarios (nome, email, matricula, tipo, setor_id) VALUES
('Ana Silva', 'ana@exemplo.com', '20260001', 'SOLICITANTE', NULL),
('Bruno Lima', 'bruno@exemplo.com', '20260002', 'SOLICITANTE', NULL),
('Carla Souza', 'carla@exemplo.com', '20260003', 'TECNICO', 1),
('Diego Alves', 'diego@exemplo.com', '20260004', 'TECNICO', 2);
