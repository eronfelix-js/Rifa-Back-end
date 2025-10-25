# 🎲 Sistema de Rifas Online

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![License](https://img.shields.io/badge/license-MIT-green)

Sistema completo de gerenciamento de rifas online com autenticação JWT, pagamentos PIX e upload de imagens.

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Instalação](#instalação)
- [Configuração](#configuração)
- [Endpoints da API](#endpoints-da-api)
- [Documentação](#documentação)
- [Testes](#testes)
- [Deploy](#deploy)
- [Contribuindo](#contribuindo)
- [Licença](#licença)

## 🎯 Sobre o Projeto

Sistema web para criação e gerenciamento de rifas online, permitindo que vendedores criem rifas, compradores reservem números e o sistema realize sorteios automáticos. Inclui integração com gateway de pagamento PIX e armazenamento de imagens na nuvem.

### ✨ Diferenciais

- 🔐 **Autenticação JWT** com controle de permissões (ADMIN, VENDEDOR, CLIENTE)
- 💳 **Pagamento PIX** via PicPay com QR Code automático
- 📸 **Upload de imagens** integrado com Cloudinary
- ⏰ **Expiração automática** de reservas após 15 minutos
- 🎰 **Sorteio automático** quando todos os números são vendidos
- 📊 **Dashboard** com estatísticas em tempo real
- 🔒 **Transações seguras** com isolamento de banco de dados

## 🚀 Funcionalidades

### 👤 Autenticação e Usuários
- [x] Registro de usuários (Cliente/Vendedor)
- [x] Login com JWT
- [x] Controle de permissões por roles
- [x] Upload de avatar

### 🎲 Gestão de Rifas
- [x] Criar rifa com imagem
- [x] Listar rifas ativas
- [x] Buscar rifa por ID
- [x] Cancelar rifa (sem números vendidos)
- [x] Listar minhas rifas
- [x] Atualizar imagem da rifa
- [x] Estatísticas detalhadas

### 🎟️ Números e Reservas
- [x] Listar números disponíveis
- [x] Reservar números específicos ou aleatórios
- [x] Expiração automática de reservas (15 min)
- [x] Job scheduler para limpeza de reservas
- [x] Máximo de 100 números por compra

### 💰 Pagamentos
- [x] Geração de PIX (QR Code + Copia e Cola)
- [x] Webhook para confirmação de pagamento
- [x] Confirmação automática de compra
- [x] Liberação de números após expiração

### 🎰 Sorteios
- [x] Sorteio automático ao vender todos os números
- [x] Sorteio manual pelo vendedor
- [x] Hash de verificação para transparência
- [x] Notificação de vencedor

## 🛠️ Tecnologias

### Backend
- **Java 17**
- **Spring Boot 3.x**
  - Spring Security (JWT)
  - Spring Data JPA
  - Spring Validation
  - Spring Scheduling
- **PostgreSQL 15**
- **Hibernate** (ORM)
- **Lombok** (Redução de boilerplate)
- **ModelMapper** (Conversão de DTOs)

### Integrações
- **Cloudinary** (Armazenamento de imagens)
- **PicPay API** (Pagamentos PIX)

### Ferramentas
- **Maven** (Gerenciamento de dependências)
- **Git** (Controle de versão)
- **Postman** (Testes de API)

## 🏗️ Arquitetura
```
src/main/java/dev/Felix/rifa_system/
├── Config/              # Configurações (Security, CORS, etc)
├── Controller/          # Endpoints REST
├── Entity/              # Entidades JPA
├── Enum/                # Enumerações
├── Exceptions/          # Tratamento de exceções
├── Mapper/              # DTOs e conversões
├── Repository/          # Camada de dados
├── Security/            # JWT e filtros
└── Service/             # Lógica de negócio
```

### Padrão de Camadas
```
Controller → Service → Repository → Database
     ↓          ↓
   DTOs     Business Logic
```

## 📦 Instalação

### Pré-requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL 15+
- Conta Cloudinary (gratuita)
- Conta PicPay Seller (opcional para testes)

### Clone o repositório
```bash
git clone https://github.com/seu-usuario/rifa-system.git
cd rifa-system
```

### Configure o banco de dados
```sql
CREATE DATABASE rifa_system;
```

### Configure as variáveis de ambiente

Crie um arquivo `application.properties` em `src/main/resources/`:
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/rifa_system
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
app.jwt.secret=SUA_CHAVE_SECRETA_MINIMO_256_BITS
app.jwt.expiration=86400000

# Cloudinary
cloudinary.cloud-name=seu_cloud_name
cloudinary.api-key=sua_api_key
cloudinary.api-secret=seu_api_secret

# PicPay
picpay.token=seu_token
picpay.seller-token=seu_seller_token
picpay.url=https://appws.picpay.com/ecommerce/public

# Upload Config
app.upload.max-size=10485760
app.upload.allowed-types=image/jpeg,image/png,image/webp

# Reserva Config
app.reserva.tempo-expiracao-minutos=15
app.reserva.max-numeros-por-compra=100
```

### Execute a aplicação
```bash
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

## ⚙️ Configuração

### Criar primeiro usuário ADMIN
```sql
INSERT INTO usuario (id, nome, email, cpf, senha, role, ativo, data_criacao, data_atualizacao)
VALUES (
    gen_random_uuid(),
    'Admin',
    'admin@rifas.com',
    '12345678901',
    '$2a$10$...',  -- Senha criptografada (use BCrypt)
    'ADMIN',
    true,
    NOW(),
    NOW()
);
```

## 📡 Endpoints da API

### 🔐 Autenticação

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/v1/auth/registro` | Registrar novo usuário |
| POST | `/api/v1/auth/login` | Fazer login |

### 🎲 Rifas

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| POST | `/api/v1/rifas` | Criar rifa (com imagem) | VENDEDOR |
| GET | `/api/v1/rifas` | Listar rifas ativas | Público |
| GET | `/api/v1/rifas/{id}` | Buscar rifa por ID | Público |
| GET | `/api/v1/rifas/minhas` | Minhas rifas | VENDEDOR |
| DELETE | `/api/v1/rifas/{id}` | Cancelar rifa | VENDEDOR |
| PUT | `/api/v1/rifas/{id}/imagem` | Atualizar imagem | VENDEDOR |
| DELETE | `/api/v1/rifas/{id}/imagem` | Remover imagem | VENDEDOR |

### 🎟️ Números

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| GET | `/api/v1/rifas/{id}/numeros/disponiveis` | Números disponíveis | Público |
| GET | `/api/v1/rifas/{id}/estatisticas` | Estatísticas da rifa | Público |
| POST | `/api/v1/compras/reservar` | Reservar números | CLIENTE |

### 💰 Compras e Pagamentos

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| POST | `/api/v1/compras/reservar` | Reservar números | CLIENTE |
| GET | `/api/v1/compras/minhas` | Minhas compras | CLIENTE |
| POST | `/api/v1/pagamentos/pix` | Gerar PIX | CLIENTE |
| POST | `/api/v1/pagamentos/webhook` | Webhook PicPay | Sistema |

### 📸 Imagens

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| POST | `/api/v1/imagens/rifa/{rifaId}` | Upload imagem rifa | VENDEDOR |
| POST | `/api/v1/imagens/avatar` | Upload avatar | USER |
| DELETE | `/api/v1/imagens/{publicId}` | Deletar imagem | USER |

### 🎰 Sorteios

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| POST | `/api/v1/sorteios/{rifaId}/sortear` | Sortear rifa | VENDEDOR |
| GET | `/api/v1/sorteios/rifa/{rifaId}` | Ver resultado | Público |

## 📚 Documentação

### Exemplos de Requisições

#### Registrar Usuário
```bash
curl -X POST http://localhost:8080/api/v1/auth/registro \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "email": "joao@email.com",
    "cpf": "12345678901",
    "telefone": "91999999999",
    "senha": "senha123",
    "role": "VENDEDOR"
  }'
```

#### Criar Rifa (com imagem)
```bash
curl -X POST http://localhost:8080/api/v1/rifas \
  -H "Authorization: Bearer SEU_TOKEN" \
  -F 'rifa={"titulo":"iPhone 15","descricao":"Sorteio","quantidadeNumeros":100,"precoPorNumero":25.50};type=application/json' \
  -F 'imagem=@foto.jpg'
```

#### Reservar Números
```bash
curl -X POST http://localhost:8080/api/v1/compras/reservar \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "rifaId": "uuid-da-rifa",
    "quantidade": 5,
    "numeros": [1, 2, 3, 4, 5]
  }'
```

## 🧪 Testes
```bash
# Executar todos os testes
mvn test

# Executar testes com coverage
mvn test jacoco:report
```

## 🚀 Deploy

### Heroku
```bash
# Login no Heroku
heroku login

# Criar app
heroku create nome-do-app

# Adicionar PostgreSQL
heroku addons:create heroku-postgresql:mini

# Configurar variáveis
heroku config:set JWT_SECRET=sua_chave
heroku config:set CLOUDINARY_URL=sua_url

# Deploy
git push heroku main
```

### Railway

1. Conecte seu repositório GitHub
2. Configure as variáveis de ambiente
3. Deploy automático a cada push

## 📊 Modelo de Dados
```mermaid
erDiagram
    USUARIO ||--o{ RIFA : cria
    RIFA ||--o{ NUMERO : possui
    USUARIO ||--o{ COMPRA : faz
    COMPRA ||--o{ NUMERO : reserva
    COMPRA ||--|| PAGAMENTO : tem
    RIFA ||--o| SORTEIO : resultado
    
    USUARIO {
        uuid id PK
        string nome
        string email UK
        string cpf UK
        string senha
        enum role
        boolean ativo
    }
    
    RIFA {
        uuid id PK
        uuid usuario_id FK
        string titulo
        text descricao
        string imagem_url
        int quantidade_numeros
        decimal preco_por_numero
        enum status
        timestamp data_sorteio
    }
    
    NUMERO {
        uuid id PK
        uuid rifa_id FK
        int numero
        enum status
        uuid compra_id FK
    }
    
    COMPRA {
        uuid id PK
        uuid rifa_id FK
        uuid comprador_id FK
        enum status
        decimal valor_total
        timestamp data_expiracao
    }
    
    PAGAMENTO {
        uuid id PK
        uuid compra_id FK UK
        string reference_id
        string qr_code
        enum status
    }
    
    SORTEIO {
        uuid id PK
        uuid rifa_id FK UK
        int numero_sorteado
        uuid vencedor_id FK
        string hash_verificacao
    }
```

## 🔒 Segurança

- ✅ Senhas criptografadas com BCrypt
- ✅ JWT com expiração configurável
- ✅ CORS configurado
- ✅ Validação de dados com Bean Validation
- ✅ Transações com isolamento SERIALIZABLE
- ✅ Rate limiting (configurável)
- ✅ HTTPS obrigatório em produção

## 🐛 Troubleshooting

### Erro: "Content-Type 'a' is not supported"

**Problema:** Postman configurando Content-Type errado no multipart.

**Solução:** No Postman, ao adicionar o campo `rifa`, clique nos 3 pontinhos e selecione Content-Type: `application/json`

### Erro: "Rifa já existe ativa"

**Problema:** Usuário tentando criar segunda rifa ativa.

**Solução:** Finalize ou cancele a rifa anterior antes de criar nova.

### Reserva expirou

**Problema:** Pagamento não confirmado em 15 minutos.

**Solução:** Reserve novamente os números desejados.

## 📈 Roadmap

- [ ] Sistema de notificações por email
- [ ] SMS para vencedores
- [ ] Relatórios em PDF
- [ ] Dashboard administrativo
- [ ] App mobile (React Native)
- [ ] Sistema de afiliados
- [ ] Sorteios agendados
- [ ] Integração com outros gateways (Mercado Pago, Stripe)

## 👥 Contribuindo

Contribuições são bem-vindas! Por favor:

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/NovaFuncionalidade`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/NovaFuncionalidade`)
5. Abra um Pull Request

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.


⭐ Se este projeto te ajudou, deixe uma estrela!

Made with ❤️ and ☕ in Belém, PA

