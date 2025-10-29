ALTER TABLE compras ADD COLUMN comprovante_url VARCHAR(500);
ALTER TABLE compras ADD COLUMN data_upload_comprovante TIMESTAMP;
ALTER TABLE compras ADD COLUMN data_confirmacao TIMESTAMP;
ALTER TABLE compras ADD COLUMN observacao_vendedor VARCHAR(500);