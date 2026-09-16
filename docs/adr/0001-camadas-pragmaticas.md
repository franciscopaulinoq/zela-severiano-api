# ADR 0001 — Camadas pragmáticas em vez de Clean Architecture estrita

**Status**: Aceito (revisado em 2026-09-16 — ver "Revisão" no fim)
**Data**: 2026-09-16

## Contexto

O projeto seguia Clean Architecture em 4 camadas (`domain`, `application`,
`adapter`, `infrastructure`) de forma estrita: um `UseCase` por operação de
negócio, cada um numa classe própria com um único método `execute`.

Um agregado como `Relato` já existia em várias formas ao longo das
camadas: `RelatoEntity` (persistência), DTOs de entrada/saída em
`application.dto`, e `Request`/`Response` em `adapter.rest`. Isso por si só
é esperado de uma arquitetura em camadas — cada camada tem sua própria
forma de representar o dado, e é isso que permite mudar uma camada (ex.
trocar de JPA para outra persistência) sem afetar as demais. O problema
observado não era a existência de DTOs, e sim dois outros pontos:

1. **Uma classe por operação de negócio** (`RegistrarRelatoUseCase`,
   `ObterDetalheRelatoUseCase`, `ListarTodosRelatosUseCase`,
   `VisualizarMeusRelatosUseCase`, `AtualizarStatusRelatoUseCase` — 5
   classes só para o agregado `Relato`) multiplicava arquivos sem
   multiplicar responsabilidades reais: são operações do mesmo agregado,
   naturalmente coesas.
2. **Dois endpoints (`obterDetalhe` e `listarMeusRelatos` do
   `RelatoController`) devolviam o DTO de `application` direto como corpo
   HTTP**, sem passar por um `Response` de `adapter.rest` como os demais
   endpoints faziam — uma inconsistência de camada, não uma consequência
   de ter DTOs.

Esse custo estava prestes a ficar mais visível: as próximas 20 issues do
TCC (abertas no rastreador de Issues do GitHub) são a base do estudo de
revisão de código por IA — cada uma vira um PR pequeno (`< 400` linhas), e
cada nova
operação de negócio nascendo como uma classe `UseCase` própria (em vez de
um método num `Service` já existente) tornaria PRs simples mais espalhados
entre arquivos do que precisam ser.

## Decisão

Manter as 4 camadas, a regra de dependência do `domain`, **e os DTOs de
`application`** (eles desacoplam o contrato HTTP do modelo de domínio, que
é o objetivo real de tê-los — ver "Revisão" abaixo). Consolidar apenas o
que gerava arquivos sem gerar desacoplamento:

1. **Um `Service` por agregado/recurso REST, não um `UseCase` por
   operação.** `RelatoService` reúne os 5 casos de uso que antes eram 5
   classes. Cada método público continua sendo um caso de uso com
   responsabilidade única — o que muda é o agrupamento em arquivo, não a
   responsabilidade nem a lógica interna (o mapeamento `domain` → DTO que
   cada `UseCase` fazia no próprio `execute` virou um método privado
   `toXxxDTO` dentro do `Service` correspondente).
2. **Todo endpoint devolve um `Response` de `adapter.rest`, nunca um DTO
   de `application` direto.** Criados `RelatoDetalheResponse` e
   `RelatoListagemResponse` (espelhando exatamente os campos de
   `RelatoDetalheDTO`/`RelatoListagemDTO`) para fechar a inconsistência
   nos dois endpoints que a tinham.

## Alternativas consideradas

- **Manter uma classe `UseCase` por operação.** Rejeitada: nenhuma delas
  tinha mais de uma responsabilidade também no desenho por `Service` — o
  agrupamento por agregado não junta operações não relacionadas, só reduz
  arquivos que não precisavam estar separados.
- **Remover os DTOs de `application` e mapear `domain` direto para
  `Request`/`Response`.** Chegou a ser implementada nesta mesma sessão e
  depois revertida — ver "Revisão" abaixo.
- **Reescrever para "package by feature" (pacote por módulo de negócio,
  ex. `relato/`, `perfil/`, `auth/`, cada um com seu controller, service,
  repository etc.).** Rejeitada por ser uma mudança maior que o necessário
  para o problema observado: exigiria mover todos os arquivos do projeto,
  não só os de `application`, e diluiria a regra de dependência de
  `domain` explícita por pacote raiz.

## Revisão (2026-09-16)

A primeira versão desta decisão também removia `application.dto` inteiro,
com `Service`s devolvendo o modelo de `domain` direto e os mappers REST
mapeando `domain` ⇄ `Request`/`Response`. Revertido a pedido do autor: os
DTOs de `application` são uma camada de desacoplamento legítima — sem
eles, qualquer mudança no modelo de `domain` (uma associação nova, um
campo interno de controle) vaza automaticamente para a superfície de
mapeamento REST, mesmo que não deva aparecer na API. Manter o DTO como
fronteira entre `application` e `adapter` é o desenho correto; o defeito
real estava só nos dois pontos listados em "Contexto", não na existência
da camada de DTO em si.

## Consequências

**Antes** (exemplo: adicionar um campo em `Relato` exposto na listagem do
cidadão): `RelatoEntity`, `RelatoMapper` (persistência), `Relato`
(domain), `RelatoListagemDTO`, `VisualizarMeusRelatosUseCase` (classe só
para montar esse DTO), `RelatoRestMapper`, `RelatoController` (devolvia o
DTO direto) — 7 arquivos.

**Depois**: `RelatoEntity`, `Relato` (domain), `RelatoListagemDTO` (o
método `toListagemDTO` já existe dentro de `RelatoService`, só ganha uma
linha), `RelatoListagemResponse`, `RelatoRestMapper` (MapStruct mapeia por
nome automaticamente se os campos baterem) — a contagem de arquivos não
muda tanto quanto na versão revertida, mas **o número de classes
"artificiais" cai**: não se cria mais uma classe `UseCase` nova por
operação, e o `Service` já existe para receber o método novo.

**O que não muda**: `domain` continua sem depender de Spring/JPA;
`infrastructure.persistence` não foi tocada; nenhuma regra de negócio foi
alterada; os DTOs de `application` continuam com os mesmos campos de
antes.

**O que não foi "corrigido" de propósito**: esta refatoração é
estritamente estrutural. Comportamentos e defeitos hoje reservados para
issues específicas do backlog do TCC foram preservados byte a byte nos
arquivos tocados, para que o diff dessas issues, quando codificadas,
continue existindo e sendo avaliável pelo pipeline de revisão por IA. Ao
contrário desta decisão arquitetural — que é pública por natureza — o
mapeamento exato de onde esses pontos ficam equivaleria a um gabarito de
defeitos ainda não corrigidos, então fica fora do repositório, em nota
local não versionada. Pelo mesmo motivo, a documentação de processo do
TCC (plano, backlog, gabarito de falhas, decisões de design do pipeline)
é mantida localmente pelo autor, não neste repositório público — só
`docs/public/regras_projeto.md` precisa estar aqui, por ser lido em tempo
real pelo workflow de revisão automática.

## Impacto na documentação e no pipeline do TCC

- `docs/public/regras_projeto.md` foi atualizado para descrever o novo
  agrupamento por `Service` e a regra de "todo endpoint devolve um
  `Response` próprio" — esse arquivo alimenta o prompt enviado aos modelos
  de IA (`scripts/pr-review/build_project_context.py`) a cada PR aberto,
  então precisa continuar público e commitado.
- `context/project_context.md` foi regenerado rodando
  `python build_project_context.py` (arquivo gerado, não editado à mão,
  não commitado — está em `.gitignore`).
- O backlog de issues não precisou mudar: as issues descrevem
  comportamento observável, nunca nomes de classe/arquivo, por desenho.
