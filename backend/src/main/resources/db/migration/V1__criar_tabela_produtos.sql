CREATE TABLE produtos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    codigo_barras VARCHAR(50) NOT NULL UNIQUE,
    fornecedor VARCHAR(150) NOT NULL,
    marca VARCHAR(100) NOT NULL,
    data_validade DATE,
    preco_varejo NUMERIC(12, 2) NOT NULL,
    preco_atacado NUMERIC(12, 2) NOT NULL,
    preco_compra TEXT NOT NULL,
    preco_venda NUMERIC(12, 2) NOT NULL,
    margem TEXT NOT NULL,
    markup NUMERIC(12, 4) NOT NULL,
    quantidade INTEGER NOT NULL DEFAULT 0,
    quantidade_minima INTEGER NOT NULL DEFAULT 0,
    criado_em TIMESTAMP NOT NULL,
    atualizado_em TIMESTAMP NOT NULL
);

CREATE INDEX idx_produtos_nome ON produtos (nome);
CREATE INDEX idx_produtos_fornecedor ON produtos (fornecedor);
