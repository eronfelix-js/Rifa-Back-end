# Documentação da API - Sistema de Rifas

## Autenticação (`/api/v1/auth`)
- `POST /register` — Cadastro de novo usuário
- `POST /login` — Login de usuário
- `GET /me` — Buscar dados do usuário logado

## Compras (`/api/v1/compras`)
- `POST /reservar` — Reservar números para uma rifa (cliente)
- `POST /{compraId}/pagamento/pix` — Gerar pagamento PIX para uma compra
- `GET /{id}` — Buscar compra por ID (acesso restrito ao comprador ou dono da rifa)
- `GET /minhas` — Listar minhas compras (cliente)

## Rifas (`/api/v1/rifas`)
- `POST /` — Criar nova rifa (vendedor)
- `GET /` — Listar rifas ativas (público)
- `GET /{id}` — Buscar rifa por ID (público)
- `GET /minhas` — Listar rifas do usuário logado (vendedor)
- `DELETE /{id}` — Cancelar rifa (vendedor)
- `GET /{id}/numeros/disponiveis` — Listar números disponíveis de uma rifa (público)
- `GET /{id}/estatisticas` — Obter estatísticas da rifa (público)

## Sorteios (`/api/v1/sorteios`)
- `POST /rifa/{rifaId}/sortear` — Realizar sorteio manual de uma rifa (vendedor)
- `GET /rifa/{rifaId}` — Buscar resultado do sorteio de uma rifa

## Usuários (`/api/v1/usuarios`)
- `GET /{id}` — Buscar usuário por ID
- `PUT /perfil` — Atualizar perfil do usuário logado
- `PUT /senha` — Alterar senha do usuário logado

---

Todos os endpoints utilizam JSON para requisições e respostas. Para detalhes dos dados enviados e recebidos, consulte os DTOs correspondentes no código-fonte.
