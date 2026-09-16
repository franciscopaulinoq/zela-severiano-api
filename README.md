# Zela Severiano API

API REST para registro de demandas na infraestrutura urbana de Doutor Severiano.

## 🚀 Tecnologias
- Java 25
- Spring Boot 4.1.0
- PostgreSQL
- Maven

## 🛠 Pré-requisitos
- Docker & Docker Compose
- JDK 25

## ⚙️ Como executar
1. Clone o repositório.
2. Copie o arquivo de exemplo de ambiente: `cp .env.example .env`
3. Suba o banco de dados: `docker-compose up -d`
4. Execute a aplicação: `./mvnw spring-boot:run`

## 🏗 Arquitetura
O projeto usa uma variação pragmática da Clean Architecture (ver
[ADR 0001](docs/adr/0001-camadas-pragmaticas.md)): 4 camadas, com DTOs
para desacoplar o contrato HTTP do modelo de domínio, mas agrupando casos
de uso por agregado em vez de uma classe por operação de negócio.
- **domain**: Entidades e regras de negócio puras, sem depender de Spring/JPA.
- **application**: `Service`s agrupados por agregado/recurso REST (um por operação de negócio dentro do método, não da classe) e DTOs de entrada/saída.
- **adapter**: Controladores, request/response REST e mappers (MapStruct) entre DTOs e o contrato HTTP.
- **infrastructure**: Implementações de persistência, banco de dados e drivers.

Detalhes e convenções completas em [`docs/public/regras_projeto.md`](docs/public/regras_projeto.md).

## 🎓 TCC — Revisão de código por IA
Este repositório também é o objeto de estudo de um TCC comparando GPT-4o,
Claude e Gemini como revisores automáticos de código: a cada Pull Request
aberto, um workflow ([`scripts/pr-review/`](scripts/pr-review/README.md))
envia o diff, a issue vinculada e o contexto do projeto
(`docs/public/regras_projeto.md`) para os três modelos e registra as
respostas para análise comparativa. O restante da documentação do TCC
(plano, backlog de issues, decisões de design, dados de classificação) é
mantido pelo autor fora deste repositório público.