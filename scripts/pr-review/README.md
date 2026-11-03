# Pipeline de revisão automática de PR por IA

Para cada PR, captura o diff, a issue vinculada e o
contexto do projeto e pede a revisão a três modelos (OpenAI, Anthropic e
Google) em cada nível de instrução pedido (P1 básico, P2 intermediário, P3
avançado). Cada resposta é registrada em `results/pr_reviews.csv` e cada
comentário extraído dela em `results/comentarios.csv`. Nada é publicado no
PR.

## Uso local

```bash
cd scripts/pr-review
python -m venv .venv && source .venv/bin/activate  # ou .venv\Scripts\activate no Windows
pip install -r requirements.txt
cp .env.example .env  # preencher GITHUB_TOKEN e as API keys

python build_project_context.py   # gera context/project_context.md
python review_pr.py --pr 30 --experimento E1 --niveis-prompt P1,P2,P3
```

## Automação

`.github/workflows/pr-ai-review.yml` roda só por disparo manual
(Actions → "Revisão automática de PR por IA" → *Run workflow*), informando a
lista de PRs, o experimento (`PILOTO` ou `E1`) e os modelos (por padrão,
os três). Cada PR vira um job:
o checkout é feito no commit do próprio PR (para o contexto do projeto ser o
daquele momento), os scripts vêm do `main` e os CSVs sobem como *artifact*.

## Configuração de inferência

Modelos fixos em `config.py` (`gpt-6.1-sol`, `claude-sonnet-5-5`,
`gemini-3.8-flash`), temperatura padrão de cada provedor (nenhum cliente
envia `temperature`), raciocínio no mesmo nível nominal nos três
(`PR_REVIEW_REASONING_EFFORT`, padrão `medium`) e o mesmo teto de saída
(`PR_REVIEW_MAX_OUTPUT_TOKENS`, padrão 16000).

## Estrutura

- `github_client.py` — chamadas à API REST do GitHub (PR, diff, arquivos, conteúdo, issue).
- `build_project_context.py` / `context_builder.py` — montam o contexto estendido (não só o diff), sem as partes da documentação que descrevem o próprio pipeline.
- `prompt_builder.py` + `prompts/` — instruções por nível + formato de saída + dados (ver `prompts/README.md`); degradação da issue por regra fixa.
- `clients/` — um adaptador fino por modelo, todos implementando `ModelClient.gerar(prompt, timeout)`.
- `parser_resposta.py` — extrai a lista de comentários do JSON devolvido (resposta fora do formato = `json_valido = N`).
- `review_pr.py` — orquestra tudo, com retry/backoff (`tenacity`) só para erros técnicos e chamadas assíncronas simultâneas.
- `storage.py` — grava revisões e comentários em CSV.
