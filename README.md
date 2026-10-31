# Chamados IFRN

API REST para organizar os pedidos de suporte do campus. O solicitante abre um chamado, o técnico do setor responsável assume o atendimento e registra a solução. As interações guardam o histórico da conversa.

**Java 21 · Spring Boot 4 · Maven · PostgreSQL · Flyway · Docker**

## Executar

Com o Docker instalado e em execução:

```bash
git clone https://github.com/JoaoRoquePNeto/chamados-ifrn.git
cd chamados-ifrn
docker compose up --build
```

A API fica em `http://localhost:8080`. Na primeira execução, o Docker baixa as imagens, compila o projeto e prepara o banco automaticamente. Não há tela de login: o usuário da ação é informado no corpo da requisição.

Para parar, use `Ctrl+C` e `docker compose down`. Os dados continuam no volume `dados_postgres`.

O banco é configurado por `DB_URL`, `DB_USER` e `DB_PASSWORD`. O Compose já fornece valores para uso local; para personalizar, defina `DB_NAME`, `DB_USER` e `DB_PASSWORD` em um arquivo `.env`, que não é enviado ao GitHub.

## Experimentar

O arquivo [requisicoes.http](requisicoes.http) reúne todas as rotas, com exemplos prontos para a extensão REST Client do VS Code. Execute as requisições na ordem do arquivo: os IDs dos cadastros e chamados criados são reutilizados automaticamente.

Dados iniciais:

| ID | Usuário | Perfil | Setor |
| --- | --- | --- | --- |
| 1 | João Neto | SOLICITANTE | — |
| 2 | Lwkas Lwhan | SOLICITANTE | — |
| 3 | João Manoel | TECNICO | COEX (1) |
| 4 | Sara Lima | TECNICO | COSGEM (2) |

As categorias 1 (Estágios) e 2 (Projetos de extensão) pertencem à COEX (Coordenação de Extensão); 3 (Elétrica) e 4 (Mobiliário), à COSGEM (Coordenação de Serviços Gerais e Manutenção).

Os setores são encontrados no portal do IFRN Campus Pau dos Ferros: [COEX](https://portal.ifrn.edu.br/campus/paudosferros/contatos-2/) e [COSGEM](https://portal.ifrn.edu.br/campus/paudosferros/noticias/campus-pau-dos-ferros-suspende-atividades-presenciais-nos-dias-6-e-7-de-janeiro/). Os usuários são cadastros de demonstração, com e-mails e matrículas fictícios. Gustavo Emanuel aparece no exemplo de criação de usuário do arquivo de requisições.

Abrir um chamado:

```bash
curl -X POST http://localhost:8080/chamados \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Dúvida no termo de estágio","descricao":"Preciso de orientação para preencher o termo de compromisso.","local":"Sala da COEX","solicitanteId":1,"categoriaId":1}'
```

Assumir o chamado criado, substituindo `1` pelo ID retornado se necessário:

```bash
curl -X PATCH http://localhost:8080/chamados/1/assumir \
  -H "Content-Type: application/json" \
  -d '{"tecnicoId":3}'
```

## Rotas e regras

| Recurso | Operações |
| --- | --- |
| `/setores` | Criar, listar, buscar, atualizar e excluir |
| `/setores/{id}/categorias` | Listar categorias do setor |
| `/categorias` | Criar, listar, buscar, atualizar e excluir |
| `/usuarios` | Criar, listar, buscar e atualizar |
| `/usuarios/{id}/chamados` | Listar chamados do solicitante ou técnico |
| `/chamados` | Abrir, listar e detalhar com interações |
| `/chamados/{id}/assumir` | Técnico do setor assume um chamado aberto |
| `/chamados/{id}/resolver` | Técnico atribuído informa a solução |
| `/chamados/{id}/cancelar` | Solicitante cancela um chamado aberto |
| `/chamados/{id}/interacoes` | Solicitante ou técnico atribuído registra mensagem |

O fluxo permitido é `ABERTO → EM_ATENDIMENTO → RESOLVIDO` ou `ABERTO → CANCELADO`. Chamados encerrados não aceitam novas ações nem interações. A prioridade padrão é `MEDIA`.

As listagens aceitam `page`, `size` e `sort`, com 10 itens por padrão e máximo de 50. A resposta contém `content`, `number`, `size`, `totalElements` e `totalPages`.

Filtros disponíveis: setores por `nome`; categorias por `nome` e `setorId`; usuários por `nome`, `tipo` e `setorId`; chamados por `status`, `prioridade`, `setorId`, `categoriaId`, `solicitanteId`, `tecnicoId`, `dataInicio` e `dataFim`. Os chamados de um usuário aceitam `status`. Datas usam `AAAA-MM-DD`, incluindo os dois dias do período.

Exemplo: `GET /chamados?status=ABERTO&setorId=1&prioridade=ALTA&page=0&size=10&sort=dataAbertura,desc`.

Dados inválidos retornam **400**, recursos inexistentes **404** e conflitos de negócio **409**. O JSON de erro contém `timestamp`, `status`, `mensagem` e `caminho`.

Para preservar o histórico, uma categoria com chamados não muda de setor, e usuários com histórico não mudam de tipo ou setor. Antes de excluir um setor sem chamados, suas categorias e técnicos precisam ser removidos ou transferidos.

## Organização

`controller` recebe as requisições; `service` aplica as regras; `repository` consulta o banco; `model` representa as entidades; `dto` define os dados de entrada e paginação; `exception` padroniza os erros. O Lombok gera getters, setters e construtores de injeção para evitar código repetitivo.

## Migrations

O Flyway executa os scripts de `src/main/resources/db/migration` em ordem. A V1 cria as tabelas, a V2 insere os dados iniciais, a V3 atualiza os setores e os nomes dos usuários de demonstração e a V4 ajusta o setor de extensão e suas categorias. A tabela `flyway_schema_history` registra versão, descrição, execução e checksum de cada migration aplicada.

Se uma migration aplicada for alterada, seu checksum muda e a validação do Flyway falha. Por isso, mudanças posteriores devem entrar em uma nova migration, como `V5__adicionar_campo.sql`, preservando as anteriores. O `ddl-auto=validate` apenas confere se as entidades correspondem ao esquema; `update` não é usado porque alterações automáticas não oferecem o mesmo histórico versionado e revisável dos scripts.

Referências: [histórico do Flyway](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/flyway-schema-history-table) e [validação](https://documentation.red-gate.com/fd/validate-277578898.html).

## Testes

Com Java 21 e Maven instalados, execute `mvn test`. Os testes verificam as regras do ciclo de vida e as permissões dos participantes. O build do Docker também executa esses testes antes de gerar a imagem.

O GitHub Actions confere o build do Docker e a resposta da API com os dados iniciais a cada envio para a `main`.
