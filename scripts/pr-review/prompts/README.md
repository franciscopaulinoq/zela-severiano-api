# Prompts

O prompt enviado a cada modelo é montado por `prompt_builder.py` como:

```
instrucoes_<nível>.md  +  formato_saida.md  +  ---  +  dados.md
```

- `instrucoes_p1.md` (básico), `instrucoes_p2.md` (intermediário) e
  `instrucoes_p3.md` (avançado) são a **única** parte que varia entre os
  níveis.
- `formato_saida.md` e `dados.md` são idênticos para todos os níveis e os
  três modelos.
- O checklist do P3 segue o guia de revisão de código do Google
  (*Engineering Practices — What to look for in a code review*) e as
  categorias do OWASP Top 10 (2021).
