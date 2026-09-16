# Pipeline de revisão automática de PR por IA

Script de suporte ao TCC "Análise Comparativa de Modelos de IA Generativa na
Revisão de Código". Para cada PR, captura o diff + a issue vinculada + o
contexto do projeto, envia o mesmo prompt para GPT-4o, Claude e Gemini em
paralelo, e registra cada resposta em `results/pr_reviews.csv`.

As decisões de design (por que Python, por que GitHub Actions, por que o
contexto foi estendido além do diff, etc.) ficam registradas separadamente
pelo autor como parte da documentação do TCC.

## Uso local

```bash
cd scripts/pr-review
python -m venv .venv && source .venv/bin/activate  # ou .venv\Scripts\activate no Windows
pip install -r requirements.txt
cp .env.example .env  # preencher GITHUB_TOKEN e as API keys que forem usar

python build_project_context.py   # gera context/project_context.md (rodar de novo só se a estrutura do projeto mudar)
python review_pr.py --pr 3        # roda os 3 modelos e salva em results/pr_reviews.csv
python review_pr.py --pr 3 --post-comments  # além de salvar, publica comentários no PR
```

## Automação

`.github/workflows/pr-ai-review.yml` roda os mesmos dois comandos
automaticamente sempre que um PR é aberto (`pull_request: opened`), usando
Secrets do repositório para as chaves de API, e sobe o CSV resultante como
*artifact* do workflow (Actions → run → Artifacts).

## Estrutura

- `github_client.py` — chamadas à API REST do GitHub (diff, arquivos, issue, comentário).
- `build_project_context.py` / `context_builder.py` — montam o contexto estendido (não só o diff) enviado ao modelo.
- `prompt_builder.py` + `prompts/code_review_prompt.md` — o prompt padrão único.
- `clients/` — um adaptador fino por modelo, todos implementando `ModelClient.gerar(prompt, timeout)`.
- `review_pr.py` — orquestra tudo, com retry/backoff (`tenacity`) e chamadas assíncronas simultâneas aos 3 modelos.
- `storage.py` — grava cada resposta em CSV.
