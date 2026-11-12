# Revisão de PR por IA

Para cada PR, monta o contexto (diff, arquivos alterados completos, issue vinculada e
contexto do projeto) e pede a revisão aos modelos configurados (OpenAI, Anthropic e
Google), em cada nível de instrução pedido (P1 básico, P2 intermediário, P3 avançado).
As respostas vão para `results/pr_reviews.csv` (uma linha por revisão) e
`results/comentarios.csv` (uma linha por comentário). Nada é publicado no PR.

## Uso local

```bash
cd scripts/pr-review
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env  # preencher GITHUB_TOKEN e as chaves de API

python build_project_context.py   # gera context/project_context.md
python review_pr.py --pr 30 --experimento E1 --niveis-prompt P1,P2,P3
```

## No GitHub Actions

`.github/workflows/pr-ai-review.yml` roda só por disparo manual (Actions → *Run
workflow*), com a lista de PRs, o experimento (`PILOTO` ou `E1`) e os provedores. Cada
PR vira um job: o código é o do commit do PR, os scripts vêm do `main` e os CSVs sobem
como artifact.

## Configuração

Modelos em `config.py` (`gpt-6.1-sol`, `claude-sonnet-5-5`, `gemini-3.8-flash`),
temperatura padrão de cada provedor, o mesmo nível de raciocínio nos três
(`PR_REVIEW_REASONING_EFFORT`, padrão `medium`) e o mesmo limite de saída
(`PR_REVIEW_MAX_OUTPUT_TOKENS`, padrão 16000).

## Arquivos

- `review_pr.py`: ponto de entrada; revisões em paralelo, com nova tentativa só para erros técnicos.
- `config.py`: configuração lida do ambiente.
- `github_client.py`: chamadas à API do GitHub.
- `context_builder.py`: contexto do PR.
- `build_project_context.py`: contexto do projeto (README, regras e pacotes), sem as seções sobre este pipeline.
- `prompt_builder.py` e `prompts/`: montagem do prompt (ver `prompts/README.md`).
- `clients/`: um cliente por provedor, com a interface `ClienteModelo.gerar(prompt, timeout)`.
- `parser_resposta.py`: extrai os comentários do JSON devolvido.
- `storage.py`: grava os CSVs.
