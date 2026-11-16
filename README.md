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
2. Copie o arquivo de exemplo de ambiente: `cp .env.example .env`.
3. Preencha as configurações do banco e do R2 e dois segredos JWT diferentes, com pelo menos 32 bytes cada.
4. Suba o banco de dados: `docker compose -f docker-compose-local.yml up -d`.
5. Execute a aplicação com o perfil de desenvolvimento: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`.

### Acesso como gestor (desenvolvimento local)
Com o perfil `dev` ativo, a aplicação cria um usuário gestor na inicialização,
para testar as rotas de `/api/v1/gestao/**` sem alterar o banco à mão:

- CPF: `11144477735`
- Senha: `gestor123`

Faça login em `POST /api/v1/auth/login` com esses dados e use o token
devolvido no cabeçalho `Authorization: Bearer <token>`. Essas credenciais
são só para uso local.

Testes e verificações de qualidade (formatação e PMD): `./mvnw verify`.
Para formatar o código automaticamente: `./mvnw spotless:apply`.

## 🏗 Arquitetura
O projeto usa uma variação pragmática da Clean Architecture: 4 camadas, com DTOs
como contrato HTTP desacoplado do modelo de domínio, mas agrupando casos
de uso por agregado em vez de uma classe por operação de negócio.
- **domain**: Entidades e regras de negócio puras.
- **application**: `Service`s agrupados por agregado/recurso REST (um por operação de negócio dentro do método, não da classe) e DTOs de entrada/saída usados diretamente pelos controllers.
- **adapter**: Controladores REST e tratamento global de exceções.
- **infrastructure**: Implementações de persistência, banco de dados e drivers.

Detalhes e convenções completas em [`docs/public/regras_projeto.md`](docs/public/regras_projeto.md).

<!-- omitir-do-contexto-de-revisao -->
## 🤖 Revisão automática de PR
Um workflow de disparo manual ([`scripts/pr-review/`](scripts/pr-review/README.md))
pede a revisão de PRs a modelos de IA e registra as respostas em CSV, sem
publicar nada nos PRs.
