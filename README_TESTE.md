# 🧪 Guia Completo de Testes - Sistema de Rifas

## ✅ **Você já fez:**
1. ✅ Criou usuário
2. ✅ Criou rifa

## 🎯 **Próximos Testes**

---

## **1️⃣ LISTAR RIFAS ATIVAS (Público)**

**Objetivo:** Ver todas as rifas disponíveis para compra

```http
GET http://localhost:8080/api/v1/rifas
```

**Headers:**
```
(Nenhum header necessário - endpoint público)
```

**Response esperado:**
```json
{
  "content": [
    {
      "id": "uuid-da-rifa",
      "titulo": "iPhone 15 Pro",
      "imagemUrl": null,
      "precoPorNumero": 10.00,
      "quantidadeNumeros": 1000,
      "numerosDisponiveis": 1000,
      "numerosVendidos": 0,
      "percentualVendido": 0.00,
      "status": "ATIVA",
      "nomeVendedor": "João Silva",
      "dataCriacao": "2024-01-15T10:30:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20
}
```

---

## **2️⃣ VER DETALHES DA RIFA (Público)**

**Objetivo:** Ver informações completas da rifa

```http
GET http://localhost:8080/api/v1/rifas/{{rifaId}}
```

**Response esperado:**
```json
{
  "id": "uuid-da-rifa",
  "usuarioId": "uuid-do-vendedor",
  "nomeVendedor": "João Silva",
  "titulo": "iPhone 15 Pro",
  "descricao": "256GB Azul Titânio",
  "quantidadeNumeros": 1000,
  "precoPorNumero": 10.00,
  "valorTotal": 10000.00,
  "status": "ATIVA",
  "numerosDisponiveis": 1000,
  "numerosReservados": 0,
  "numerosVendidos": 0,
  "percentualVendido": 0.00,
  "valorArrecadado": 0.00,
  "totalCompras": 0
}
```

---

## **3️⃣ VER NÚMEROS DISPONÍVEIS (Público)**

**Objetivo:** Ver quais números estão disponíveis para compra

```http
GET http://localhost:8080/api/v1/rifas/{{rifaId}}/numeros/disponiveis
```

**Response esperado:**
```json
{
  "total": 1000,
  "disponiveis": 1000,
  "vendidos": 0,
  "reservados": 0,
  "numerosDisponiveis": [1, 2, 3, 4, 5, ..., 1000]
}
```

---

## **4️⃣ CRIAR USUÁRIO CLIENTE (Para comprar)**

**Objetivo:** Criar um cliente para testar compra

```http
POST http://localhost:8080/api/v1/auth/register
Content-Type: application/json
```

**Body:**
```json
{
  "nome": "Maria Cliente",
  "email": "maria@email.com",
  "cpf": "98765432100",
  "telefone": "11888888888",
  "senha": "senha123",
  "role": "CLIENTE"
}
```

**Response:**
```json
{
  "id": "uuid-do-cliente",
  "nome": "Maria Cliente",
  "email": "maria@email.com",
  "cpf": "98765432100",
  "role": "CLIENTE",
  "ativo": true
}
```

---

## **5️⃣ LOGIN DO CLIENTE**

```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json
```

**Body:**
```json
{
  "email": "maria@email.com",
  "senha": "senha123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tipo": "Bearer",
  "usuario": {
    "id": "uuid-do-cliente",
    "nome": "Maria Cliente",
    "role": "CLIENTE"
  }
}
```

**⚠️ IMPORTANTE:** Copie o `token` e salve em uma variável do Postman!

---

## **6️⃣ RESERVAR NÚMEROS (Cliente)**

**Objetivo:** Cliente reserva números da rifa

```http
POST http://localhost:8080/api/v1/compras/reservar
Authorization: Bearer {{token_cliente}}
Content-Type: application/json
```

**Body - Opção 1 (Sistema escolhe):**
```json
{
  "rifaId": "{{rifaId}}",
  "quantidade": 5
}
```

**Body - Opção 2 (Cliente escolhe números específicos):**
```json
{
  "rifaId": "{{rifaId}}",
  "quantidade": 5,
  "numeros": [7, 13, 21, 42, 99]
}
```

**Response esperado:**
```json
{
  "compraId": "uuid-da-compra",
  "rifaId": "uuid-da-rifa",
  "tituloRifa": "iPhone 15 Pro",
  "quantidadeNumeros": 5,
  "numeros": [7, 13, 21, 42, 99],
  "valorTotal": 50.00,
  "dataExpiracao": "2024-01-15T10:45:00",
  "minutosParaExpirar": 15,
  "pagamento": null
}
```

**✅ SUCESSO! Os números foram RESERVADOS!**

---

## **7️⃣ GERAR PIX PARA PAGAMENTO**

**Objetivo:** Gerar QR Code PIX para pagar a compra

```http
POST http://localhost:8080/api/v1/compras/{{compraId}}/pagamento/pix
Authorization: Bearer {{token_cliente}}
```

**Response esperado:**
```json
{
  "id": "uuid-do-pagamento",
  "compraId": "uuid-da-compra",
  "gateway": "PICPAY",
  "referenceId": "uuid-da-compra",
  "qrCode": "iVBORw0KGgo...", // Base64 da imagem
  "qrCodePayload": "00020126580014br.gov.bcb.pix...", // PIX copia e cola
  "valor": 50.00,
  "status": "AGUARDANDO",
  "dataExpiracao": "2024-01-15T10:45:00"
}
```

**✅ SUCESSO! PIX gerado!**

---

## **8️⃣ SIMULAR WEBHOOK DO PICPAY (Aprovar Pagamento)**

**Objetivo:** Simular que o cliente pagou via PIX

```http
POST http://localhost:8080/api/v1/webhook/picpay
x-seller-token: {{PICPAY_SELLER_TOKEN}}
Content-Type: application/json
```

**⚠️ IMPORTANTE:** O `x-seller-token` deve ser o mesmo do `application.yml`

**Body:**
```json
{
  "referenceId": "{{compraId}}",
  "status": "paid",
  "authorizationId": "PICPAY-12345"
}
```

**Response:**
```
200 OK
```

**✅ SUCESSO! Pagamento confirmado!**

---

## **9️⃣ VERIFICAR COMPRA CONFIRMADA**

```http
GET http://localhost:8080/api/v1/compras/{{compraId}}
Authorization: Bearer {{token_cliente}}
```

**Response esperado:**
```json
{
  "id": "uuid-da-compra",
  "rifaId": "uuid-da-rifa",
  "tituloRifa": "iPhone 15 Pro",
  "compradorId": "uuid-do-cliente",
  "nomeComprador": "Maria Cliente",
  "status": "PAGO",  // ✅ Mudou de PENDENTE para PAGO
  "valorTotal": 50.00,
  "quantidadeNumeros": 5,
  "numeros": [7, 13, 21, 42, 99]
}
```

---

## **🔟 VER MINHAS COMPRAS (Cliente)**

```http
GET http://localhost:8080/api/v1/compras/minhas
Authorization: Bearer {{token_cliente}}
```

**Response:**
```json
{
  "content": [
    {
      "id": "uuid-da-compra",
      "tituloRifa": "iPhone 15 Pro",
      "quantidadeNumeros": 5,
      "valorTotal": 50.00,
      "status": "PAGO",
      "dataCriacao": "2024-01-15 10:30"
    }
  ]
}
```

---

## **1️⃣1️⃣ VERIFICAR NÚMEROS DA RIFA NOVAMENTE**

```http
GET http://localhost:8080/api/v1/rifas/{{rifaId}}/numeros/disponiveis
```

**Response esperado:**
```json
{
  "total": 1000,
  "disponiveis": 995,  // ✅ Diminuiu de 1000 para 995
  "vendidos": 5,       // ✅ Aumentou de 0 para 5
  "reservados": 0,
  "numerosDisponiveis": [1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 14, ...]
  // Note que 7, 13, 21, 42, 99 não aparecem mais!
}
```

---

## **1️⃣2️⃣ VENDEDOR VER VENDAS DA SUA RIFA**

```http
GET http://localhost:8080/api/v1/compras/rifa/{{rifaId}}
Authorization: Bearer {{token_vendedor}}
```

**Response:**
```json
{
  "content": [
    {
      "id": "uuid-da-compra",
      "compradorId": "uuid-do-cliente",
      "nomeComprador": "Maria Cliente",
      "status": "PAGO",
      "valorTotal": 50.00,
      "quantidadeNumeros": 5,
      "numeros": [7, 13, 21, 42, 99]
    }
  ]
}
```

---

## **1️⃣3️⃣ SORTEAR MANUALMENTE (Vendedor)**

**Objetivo:** Vendedor realiza sorteio antes de vender tudo

```http
POST http://localhost:8080/api/v1/sorteios/rifa/{{rifaId}}/sortear
Authorization: Bearer {{token_vendedor}}
```

**Response esperado:**
```json
{
  "id": "uuid-do-sorteio",
  "rifaId": "uuid-da-rifa",
  "tituloRifa": "iPhone 15 Pro",
  "numeroSorteado": 13,  // ✅ Um dos números vendidos
  "compradorVencedorId": "uuid-do-cliente",
  "nomeVencedor": "Maria Cliente",
  "emailVencedor": "maria@email.com",
  "metodo": "MANUAL",
  "hashVerificacao": "a1b2c3d4...",
  "dataSorteio": "2024-01-15T11:00:00"
}
```

**✅ SUCESSO! Sorteio realizado!**

---

## **1️⃣4️⃣ VER RESULTADO DO SORTEIO (Público)**

```http
GET http://localhost:8080/api/v1/sorteios/rifa/{{rifaId}}
```

**Response:**
```json
{
  "id": "uuid-do-sorteio",
  "rifaId": "uuid-da-rifa",
  "numeroSorteado": 13,
  "nomeVencedor": "Maria Cliente",
  "metodo": "MANUAL",
  "hashVerificacao": "a1b2c3d4...",
  "dataSorteio": "2024-01-15T11:00:00"
}
```

---

## **1️⃣5️⃣ TESTAR RESERVA EXPIRADA**

**Objetivo:** Reservar números e NÃO pagar (deixar expirar)

### **Passo 1: Reservar números**
```http
POST http://localhost:8080/api/v1/compras/reservar
Authorization: Bearer {{token_cliente}}

{
  "rifaId": "{{rifaId}}",
  "quantidade": 3,
  "numeros": [100, 200, 300]
}
```

### **Passo 2: Aguardar 15 minutos OU aguardar o scheduler rodar (1min)**

### **Passo 3: Verificar que números voltaram**
```http
GET http://localhost:8080/api/v1/rifas/{{rifaId}}/numeros/disponiveis
```

**✅ Os números 100, 200, 300 devem voltar para disponíveis!**

---

## **📊 Checklist de Testes Completo**

### **Autenticação:**
- [ ] Registrar vendedor
- [ ] Login vendedor
- [ ] Registrar cliente
- [ ] Login cliente
- [ ] Ver perfil (GET /api/v1/auth/me)

### **Rifas:**
- [ ] Criar rifa (vendedor)
- [ ] Listar rifas ativas (público)
- [ ] Ver detalhes da rifa (público)
- [ ] Ver números disponíveis (público)
- [ ] Ver estatísticas (público)
- [ ] Cancelar rifa (vendedor)

### **Compras:**
- [ ] Reservar números (cliente)
- [ ] Gerar PIX (cliente)
- [ ] Webhook aprovar pagamento
- [ ] Ver minhas compras (cliente)
- [ ] Ver vendas da rifa (vendedor)
- [ ] Testar expiração de reserva

### **Sorteio:**
- [ ] Sortear manualmente (vendedor)
- [ ] Ver resultado (público)

---

## **🔧 Configurar Variáveis no Postman**

Crie estas variáveis de ambiente:

```
base_url = http://localhost:8080
token_vendedor = (colado após login)
token_cliente = (colado após login)
rifaId = (colado após criar rifa)
compraId = (colado após reservar)
PICPAY_SELLER_TOKEN = seu-seller-token-aqui
```

---

## **✅ Está tudo funcionando se:**

1. ✅ Conseguiu criar usuário e fazer login
2. ✅ Vendedor consegue criar rifa
3. ✅ Público consegue ver rifas
4. ✅ Cliente consegue reservar números
5. ✅ Números ficam RESERVADOS por 15min
6. ✅ Webhook confirma pagamento
7. ✅ Números mudam para VENDIDO
8. ✅ Sorteio funciona
9. ✅ Reservas expiradas são liberadas

**Algum endpoint deu erro? Me diga qual e eu te ajudo!** 🚀