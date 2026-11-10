-- =============================================================================
-- Dados iniciais para o quiz funcionar logo na primeira execução.
-- =============================================================================

-- Usuário padrão (id 1), que faz o papel de "usuário logado", como no projeto original.
-- Senha: Sinalize123 (hash BCrypt)
INSERT INTO usuarios (id, nome, email, senha)
VALUES (1, 'Administrador', 'admin@sinalize.com.br',
        '$2a$10$MukaCES.fEl2PCi0nNYySeMWXCXSIOnT/099z3Frgj2qLXk5hUOFu');

-- Como o id foi informado explicitamente, a sequência da identidade precisa ser ajustada.
SELECT setval(pg_get_serial_sequence('usuarios', 'id'), (SELECT max(id) FROM usuarios));

INSERT INTO categorias (nome, descricao, criado_por) VALUES
    ('Saudações', 'Cumprimentos e expressões de cortesia do dia a dia.', 1),
    ('Família',   'Membros da família e parentes.', 1),
    ('Cores',     'Nomes das cores mais comuns.', 1),
    ('Animais',   'Animais domésticos e selvagens.', 1),
    ('Alimentos', 'Comidas e bebidas do cotidiano.', 1),
    ('Lugares',   'Locais e ambientes frequentados no dia a dia.', 1);

-- Palavras de cada categoria. "Laranja" aparece em Cores e em Alimentos,
-- o que exercita a associação N:N.
WITH dados (palavra, categoria) AS (
    VALUES
        ('Oi', 'Saudações'), ('Tchau', 'Saudações'), ('Obrigado', 'Saudações'),
        ('Bom dia', 'Saudações'), ('Boa noite', 'Saudações'), ('Desculpa', 'Saudações'),
        ('Por favor', 'Saudações'),

        ('Mãe', 'Família'), ('Pai', 'Família'), ('Irmão', 'Família'), ('Irmã', 'Família'),
        ('Avô', 'Família'), ('Avó', 'Família'), ('Filho', 'Família'), ('Tio', 'Família'),

        ('Azul', 'Cores'), ('Vermelho', 'Cores'), ('Verde', 'Cores'), ('Amarelo', 'Cores'),
        ('Branco', 'Cores'), ('Preto', 'Cores'), ('Rosa', 'Cores'), ('Laranja', 'Cores'),

        ('Cachorro', 'Animais'), ('Gato', 'Animais'), ('Cavalo', 'Animais'), ('Peixe', 'Animais'),
        ('Pássaro', 'Animais'), ('Vaca', 'Animais'), ('Macaco', 'Animais'),

        ('Arroz', 'Alimentos'), ('Feijão', 'Alimentos'), ('Pão', 'Alimentos'), ('Leite', 'Alimentos'),
        ('Café', 'Alimentos'), ('Maçã', 'Alimentos'), ('Banana', 'Alimentos'), ('Água', 'Alimentos'),
        ('Laranja', 'Alimentos'),

        ('Casa', 'Lugares'), ('Escola', 'Lugares'), ('Hospital', 'Lugares'), ('Igreja', 'Lugares'),
        ('Praia', 'Lugares'), ('Cidade', 'Lugares')
),
novas_palavras AS (
    INSERT INTO palavras (palavra, criado_por)
    SELECT DISTINCT palavra, 1 FROM dados
    RETURNING id, palavra
)
INSERT INTO palavras_categorias (palavra_id, categoria_id)
SELECT np.id, c.id
FROM dados d
JOIN novas_palavras np ON np.palavra = d.palavra
JOIN categorias c ON c.nome = d.categoria;
