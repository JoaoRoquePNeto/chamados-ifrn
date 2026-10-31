UPDATE setores
SET nome = 'Coordenação de Extensão', sigla = 'COEX'
WHERE sigla = 'CTI';

UPDATE categorias
SET nome = 'Estágios', descricao = 'Orientações e suporte aos documentos de estágio.'
WHERE id = 1 AND setor_id = 1;

UPDATE categorias
SET nome = 'Projetos de extensão', descricao = 'Orientações e suporte aos projetos de extensão.'
WHERE id = 2 AND setor_id = 1;
