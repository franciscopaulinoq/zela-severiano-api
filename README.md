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
O projeto segue os princípios da **Clean Architecture**, dividindo as responsabilidades em:
- **domain**: Entidades e regras de negócio puras.
- **application**: Casos de uso e DTOs.
- **adapter**: Controladores e mappers de interface.
- **infrastructure**: Implementações de persistência, banco de dados e drivers.

## 🎓 TCC — Revisão de código por IA
Este repositório também é o objeto de estudo de um TCC comparando GPT-4o,
Claude e Gemini como revisores automáticos de código. Documentação completa
(plano, requisitos, modelo de dados, decisões de design) em
[`docs/tcc/`](docs/tcc/README.md); pipeline de revisão automática em
[`scripts/pr-review/`](scripts/pr-review/README.md).