# Regras e convenções do projeto Zela Severiano

> Este arquivo alimenta diretamente o contexto enviado aos modelos de IA no
> pipeline de revisão (`scripts/pr-review/build_project_context.py`) e serve
> como checklist de referência para revisão humana. Qualquer PR (das 20
> issues ou futuro) deve ser avaliado contra estas regras.

## Arquitetura (Camadas Pragmáticas)

O projeto mantém as 4 camadas e a regra de direção de dependência da Clean
Architecture (o que de fato protege o domínio de acoplamento a framework),
mas elimina a duplicação que só existia por "regra de camada", sem
desacoplamento real em troca: uma classe por operação de negócio em vez de
por agregado. Os DTOs de `application` **são mantidos** — eles continuam
sendo o que desacopla o formato de resposta HTTP do modelo de domínio (um
`Service` nunca deveria devolver a entidade de `domain` direto para o
`adapter.rest` serializar). Cada camada tem seu próprio pacote raiz sob
`io.github.franciscopaulinoq.zelaseveriano`. Motivação completa em
[`docs/adr/0001-camadas-pragmaticas.md`](../adr/0001-camadas-pragmaticas.md).

- **`domain`**: entidades de negócio puras (`model`), portas/interfaces de
  repositório e serviços (`repository`, `security`, `storage`) e exceções de
  domínio (`exception`). **Não pode depender de Spring, JPA, nem de nenhuma
  outra camada.** Inalterado pela refatoração.
- **`application`**: casos de uso agrupados por agregado/recurso REST em
  classes `<Agregado>Service` (`service`), não mais uma classe por operação
  (ex. `RelatoService` reúne registrar, listar, obter detalhe e atualizar
  status de um relato — antes eram 5 classes `UseCase` separadas). Cada
  método público continua sendo um caso de uso com responsabilidade única;
  o que muda é o agrupamento por arquivo, não a responsabilidade. DTOs
  (`dto`) continuam trafegando dados de entrada/saída entre `adapter` e
  `domain`, exatamente como antes. Depende de `domain`, nunca de `adapter`
  ou `infrastructure` diretamente (usa as interfaces do `domain`).
- **`adapter.rest`**: controllers, request/response, mappers REST
  (MapStruct) e o `GlobalExceptionHandler`. Todo endpoint devolve um tipo
  próprio de `adapter.rest.response` — nunca um DTO de `application`
  diretamente como corpo HTTP (isso desacopla o contrato público da forma
  interna que o `application` usa, que pode mudar por motivos que nada têm
  a ver com a API). Depende de `application`, nunca acessa `infrastructure`
  ou repositórios JPA diretamente.
- **`infrastructure`**: implementações concretas dos ports do `domain`
  (`persistence`, `security`, `storage`) — JPA, JWT, S3/R2. Implementa as
  interfaces definidas em `domain`. Inalterado pela refatoração.

Uma dependência de uma camada mais interna (`domain`) apontando para uma
mais externa (`adapter`/`infrastructure`) é sempre uma violação de
arquitetura, mesmo que compile.

### O que a refatoração corrigiu (e o que preservou)

- Corrigiu: 9 classes `UseCase` de método único viraram 4 `Service`s por
  agregado (`RelatoService`, `ContaService`, `PerfilService`,
  `CategoriaService`) — menos arquivos para tocar numa mesma mudança de
  negócio, sem juntar agregados diferentes na mesma classe.
  `RelatoDetalheDTO`/`RelatoListagemDTO` eram devolvidos direto como corpo
  HTTP pelo `RelatoController` (inconsistência de camada — os outros
  endpoints sempre passavam por um `Response` próprio); agora existem
  `RelatoDetalheResponse`/`RelatoListagemResponse` e todo endpoint segue o
  mesmo padrão `Service` (DTO) → mapper REST → `Response`.
- Preservou: os DTOs de `application` (`application.dto`) continuam
  existindo com os mesmos campos de antes — eles são o que impede um
  `Service` de vazar o modelo de `domain` (com suas associações completas,
  regras de negócio e forma pensada para persistência/consistência) direto
  para o contrato HTTP público.

## Convenções de nomenclatura

- Modelo de domínio, casos de uso, tabelas e campos: **português** (ex.:
  `Relato`, `RegistrarRelatoUseCase`, `criado_em`). DTOs/Requests/Responses
  seguem o mesmo idioma para consistência com o domínio.
- Enums de domínio (`StatusRelato`, `Role`) ficam em
  `domain.model.enums` e são persistidos como `@Enumerated(EnumType.STRING)`
  — nunca `ORDINAL` (adicionar um valor no meio quebraria dados existentes).
- Endpoints REST: prefixo `/api/v1/`, recursos no plural (`/relatos`,
  `/categorias`), sub-recursos de gestão sob `/api/v1/gestao/...`.

## Regras de negócio vigentes

- Um `Relato` nasce sempre com `status = PENDENTE`.
- Transições de status são controladas por `Relato.atualizarStatus` e só
  podem seguir o mapa definido lá. Qualquer nova transição deve ser
  adicionada nesse método, nunca via SQL direto ou verificação solta em um
  use case.
- Mudar o status de um relato para `REJEITADO` exige uma observação não
  vazia.
- Endpoints sob `/api/v1/gestao/**` exigem o papel `GESTOR`
  (`SecurityConfig` + claim `role` no JWT). Endpoints de cidadão nunca devem
  confiar em dado enviado pelo cliente para decidir permissão — sempre a
  partir do token.
- Um cidadão só enxerga/opera os próprios relatos, nunca os de terceiros
  (toda consulta de cidadão é escopada por `perfilId`, obtido do token, não
  de parâmetro de request).

## Padrões de qualidade esperados (SOLID / DRY)

- Cada método público de um `Service` deve ter uma única responsabilidade
  de negócio; um método não deve orquestrar duas operações não
  relacionadas. Um `Service` agrupa métodos do mesmo agregado/recurso REST
  — se um novo método pertence a um agregado diferente do que já está na
  classe, ele vai para outro `Service`, nunca "encaixado" no existente.
- Validação de regra de negócio pertence ao `domain` (ex.:
  `Relato.atualizarStatus`) ou ao topo do use case, nunca ao controller.
- Bean Validation (`jakarta.validation`) cobre validação de formato/sintaxe
  nos `Request`; validação de regra de negócio (que depende de estado
  persistido) é sempre feita no `application`/`domain`.
- Regras duplicadas em mais de um lugar (ex.: validação de CPF, cálculo de
  transição de status) devem ser extraídas para um único ponto reutilizável
  — não copiadas.
- Logging de erro/depuração sempre via SLF4J (`@Slf4j`); nunca
  `System.out`/`System.err`/`printStackTrace`.

## Tecnologias

Java 25, Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security),
PostgreSQL + Flyway, MapStruct, Lombok, JWT via `com.auth0:java-jwt`,
armazenamento de arquivos via S3-compatível (Cloudflare R2), documentação
via springdoc-openapi.
