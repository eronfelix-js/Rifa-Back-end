CREATE TABLE IF NOT EXISTS usuario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    cpf VARCHAR(14),
    telefone VARCHAR(20),
    senha VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    chave_pix VARCHAR(100),
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS rifas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuario(id),
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    imagem_url VARCHAR(500),
    quantidade_numeros INTEGER NOT NULL,
    preco_por_numero DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVA',
    tipo VARCHAR(20) NOT NULL DEFAULT 'GRATUITA',
    repassar_taxa_cliente BOOLEAN NOT NULL DEFAULT TRUE,
    sorteio_automatico BOOLEAN NOT NULL DEFAULT TRUE,
    sortear_ao_vender_tudo BOOLEAN NOT NULL DEFAULT TRUE,
    data_inicio TIMESTAMP NOT NULL,
    data_limite TIMESTAMP,
    data_sorteio TIMESTAMP,
    numero_vencedor INTEGER,
    comprador_vencedor_id UUID,
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS compras (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rifa_id UUID NOT NULL REFERENCES rifas(id),
    comprador_id UUID NOT NULL REFERENCES usuario(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    valor_total DECIMAL(10,2) NOT NULL,
    quantidade_numeros INTEGER NOT NULL,
    data_expiracao TIMESTAMP NOT NULL,
    comprovante_url VARCHAR(500),
    data_upload_comprovante TIMESTAMP,
    data_confirmacao TIMESTAMP,
    observacao_vendedor VARCHAR(500),
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS numeros (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rifa_id UUID NOT NULL REFERENCES rifas(id),
    compra_id UUID REFERENCES compras(id),
    numero INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL',
    data_reserva TIMESTAMP,
    data_venda TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pagamentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    compra_id UUID NOT NULL UNIQUE REFERENCES compras(id),
    gateway VARCHAR(20) NOT NULL DEFAULT 'PICPAY',
    reference_id VARCHAR(100) NOT NULL UNIQUE,
    authorization_id VARCHAR(100),
    qr_code TEXT,
    qr_code_payload TEXT,
    valor DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AGUARDANDO',
    data_expiracao TIMESTAMP,
    data_pagamento TIMESTAMP,
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS sorteios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rifa_id UUID NOT NULL UNIQUE REFERENCES rifas(id),
    numero_sorteado INTEGER NOT NULL,
    comprador_vencedor_id UUID NOT NULL REFERENCES usuario(id),
    metodo VARCHAR(20) NOT NULL,
    hash_verificacao VARCHAR(64) NOT NULL,
    data_sorteio TIMESTAMP NOT NULL,
    observacoes TEXT
);