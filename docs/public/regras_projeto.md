# Regras e convenções do projeto Zela Severiano

## Arquitetura (Camadas Pragmáticas)

O projeto usa 4 camadas e a regra de direção de dependência da Clean
Architecture, com casos de uso agrupados por agregado. Os DTOs de
`application` formam o contrato HTTP e o desacoplam do modelo de domínio
(um `Service` nunca devolve a entidade de `domain` direto para o
`adapter.rest` serializar). Cada camada tem seu próprio pacote raiz sob
`io.github.franciscopaulinoq.zelaseveriano`.

- **`domain`**: entidades de negócio puras (`model`), portas/interfaces de
  repositório e serviços (`repository`, `security`, `storage`) e exceções de
  domínio (`exception`). **Não pode depender de nenhuma outra camada.**
- **`application`**: casos de uso agrupados por agregado/recurso REST em
  classes `<Agregado>Service` (`service`); por exemplo, `RelatoService`
  reúne registrar, listar, obter detalhe e atualizar status de um relato.
  Cada método público é um caso de uso com responsabilidade única. DTOs
  (`dto`) trafegam dados de entrada/saída entre `adapter` e `domain`.
  Depende de `domain`, nunca de `adapter` ou `infrastructure` diretamente
  (usa as interfaces do `domain`).
- **`adapter.rest`**: controllers e o `GlobalExceptionHandler`. Os
  controllers recebem e devolvem diretamente os DTOs de `application`.
  Depende de `application`, nunca acessa `infrastructure` ou repositórios
  JPA diretamente.
- **`infrastructure`**: implementações concretas dos ports do `domain`
  (`persistence`, `security`, `storage`) — JPA, JWT, S3/R2. Implementa as
  interfaces definidas em `domain`.

Uma dependência de uma camada mais interna (`domain`) apontando para uma
mais externa (`adapter`/`infrastructure`) é uma violação de arquitetura,
mesmo que compile.

A arquitetura deve ser seguida, mas não é um dogma. Concessões são aceitas
quando deixam o código mais simples e fácil de manter: uma arquitetura
rígida que gera código repetitivo, complexo e em que qualquer alteração
mínima se espalha por muitos arquivos não é o objetivo. O isolamento da
Clean Architecture vale sobretudo para o que é externo à aplicação (banco
de dados, mensageria, e-mail, armazenamento de arquivos), para facilitar a
troca ou a adição de implementações, e não para engessar o código da
aplicação.

## Convenções de nomenclatura

- Modelo de domínio, casos de uso, tabelas e campos: **português** (ex.:
  `Relato`, `RelatoService`, `criado_em`). DTOs seguem o mesmo idioma para
  consistência com o domínio.
- Enums de domínio (`StatusRelato`, `Role`) ficam em
  `domain.model.enums` e são persistidos como `@Enumerated(EnumType.STRING)`
  — nunca `ORDINAL` (adicionar um valor no meio quebraria dados existentes).
- Endpoints REST: prefixo `/api/v1/`, recursos no plural (`/relatos`,
  `/categorias`), sub-recursos de gestão sob `/api/v1/gestao/...`.
- Branches: `<tipo>/<numero-da-issue>-<slug-breve>`, com `tipo` em
  `feat|fix|chore|refactor|test` (ex.: `feat/1-paginacao-listagem-relatos`,
  `fix/14-expiracao-token-jwt`). O número é o da issue do GitHub que a
  branch resolve; o slug é só pra legibilidade, não precisa repetir o
  título inteiro da issue. O PR referencia a mesma issue (`Closes #N`), o
  que a vincula ao PR e a fecha no merge.

## Padrões de qualidade esperados (SOLID / DRY)

- Cada método público de um `Service` deve ter uma única responsabilidade
  de negócio; um método não deve orquestrar duas operações não
  relacionadas. Um `Service` agrupa métodos do mesmo agregado/recurso REST
  — se um novo método pertence a um agregado diferente do que já está na
  classe, ele vai para outro `Service`, nunca "encaixado" no existente.
- Validação de regra de negócio pertence ao `domain` (ex.:
  `Relato.atualizarStatus`) ou ao topo do use case, nunca ao controller.
- Bean Validation (`jakarta.validation`) cobre validação de formato/sintaxe
  nos DTOs de entrada; validação de regra de negócio (que depende de estado
  persistido) é sempre feita no `application`/`domain`.
- Regras duplicadas em mais de um lugar (ex.: validação de CPF, cálculo de
  transição de status) devem ser extraídas para um único ponto reutilizável
  — não copiadas.
- Logging de erro/depuração sempre via SLF4J (`@Slf4j`); nunca
  `System.out`/`System.err`/`printStackTrace`.
- Formatação no padrão google-java-format, verificada pelo Spotless, e
  regras básicas do PMD (`config/pmd/ruleset.xml`): sem imports, variáveis,
  campos ou métodos privados sem uso, sem blocos vazios e sempre com chaves
  em `if`/`for`/`while`. O CI roda `./mvnw verify`, que inclui as duas
  verificações e os testes.

## Tecnologias

Java 25, Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security),
PostgreSQL + Flyway, MapStruct, Lombok, JWT via `com.auth0:java-jwt`,
armazenamento de arquivos via S3-compatível (Cloudflare R2), documentação
via springdoc-openapi.
