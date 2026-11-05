INSERT INTO "usuarios" ("id", "cpf", "senha_hash", "role")
VALUES ('6f2b9c1e-4a7d-4e2f-9b1a-3c5d7e9f0a12',
        '11144477735',
        '$2a$12$yG48DtlJmfemBdYb7Byuv./c35yRg/MUcOqGrcxZtrEZZ85MZeSm6',
        'GESTOR')
ON CONFLICT ("cpf") DO NOTHING;

INSERT INTO "perfis" ("id", "usuario_id", "nome_completo", "email")
VALUES ('8a4d2e6f-1b3c-4d5e-8f7a-9b0c1d2e3f45',
        '6f2b9c1e-4a7d-4e2f-9b1a-3c5d7e9f0a12',
        'Gestor Local',
        'gestor@zelaseveriano.local')
ON CONFLICT ("usuario_id") DO NOTHING;
