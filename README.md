# FAQ Inteligente

Sistema de FAQ com busca semântica usando Spring WebFlux, MongoDB, Qdrant e Ollama.

## Sobre

Este projeto implementa uma API de FAQ onde usuários podem buscar respostas usando linguagem natural. A busca semântica permite encontrar FAQs relevantes mesmo quando as palavras não coincidem exatamente.

**Exemplo:**
- FAQ cadastrada: "Como redefinir credenciais de acesso?"
- Busca do usuário: "esqueci minha senha"
- O sistema encontra a FAQ correta porque entende que os significados são similares.

## Stack

| Componente | Tecnologia |
|------------|------------|
| API | Spring Boot 3.2 + WebFlux |
| Banco de dados | MongoDB |
| Banco vetorial | Qdrant |
| Embeddings | Ollama (nomic-embed-text) |
| Autenticação | JWT |
| Senha | BCrypt |

## Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│                    Docker Compose                       │
│                                                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │              API Spring WebFlux                  │   │
│  │                   (:8080)                        │   │
│  └──────────────────────────────────────────────────┘   │
│           │              │              │               │
│           ▼              ▼              ▼               │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐         │
│  │  MongoDB   │  │   Qdrant   │  │   Ollama   │         │
│  │  (:27017)  │  │  (:6333)   │  │  (:11434)  │         │
│  └────────────┘  └────────────┘  └────────────┘         │
└─────────────────────────────────────────────────────────┘
```

## Pré-requisitos

- Docker e Docker Compose
- Java 17+ (para desenvolvimento local)
- Maven (para desenvolvimento local)

## Como executar

### Com Docker (recomendado)

```bash
# Subir todos os serviços
docker-compose up -d

# Baixar o modelo de embedding (necessário apenas na primeira vez)
docker exec -it java-spring-web-flux-with-security-ollama-1 ollama pull nomic-embed-text

# Verificar logs da API
docker-compose logs -f api
```

### Desenvolvimento local

```bash
# Subir apenas as dependências
docker-compose up -d mongodb qdrant ollama

# Baixar o modelo de embedding
docker exec -it java-spring-web-flux-with-security-ollama-1 ollama pull nomic-embed-text

# Executar a aplicação
./mvnw spring-boot:run
```

## Endpoints

### Autenticação

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/auth/register` | Criar usuário |
| POST | `/api/auth/login` | Login (retorna JWT) |

### FAQ

| Método | Endpoint | Acesso | Descrição |
|--------|----------|--------|-----------|
| GET | `/api/faqs` | Público | Listar todas FAQs |
| GET | `/api/faqs/{id}` | Público | Buscar FAQ por ID |
| POST | `/api/faqs` | Admin | Criar FAQ |
| PUT | `/api/faqs/{id}` | Admin | Atualizar FAQ |
| DELETE | `/api/faqs/{id}` | Admin | Remover FAQ |
| POST | `/api/faqs/search` | Público | Busca semântica |

## Exemplos de uso

### Registrar usuário admin

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "role": "ROLE_ADMIN"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

Resposta:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "admin"
}
```

### Criar FAQ (requer token de admin)

```bash
curl -X POST http://localhost:8080/api/faqs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d '{
    "pergunta": "Como redefinir minha senha?",
    "resposta": "Acesse Configurações > Segurança > Redefinir senha. Você receberá um email com instruções.",
    "categoria": "conta",
    "tags": ["senha", "segurança", "acesso"]
  }'
```

### Busca semântica

```bash
curl -X POST http://localhost:8080/api/faqs/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "esqueci meu password",
    "limit": 5
  }'
```

Resposta:
```json
[
  {
    "faq": {
      "id": "...",
      "pergunta": "Como redefinir minha senha?",
      "resposta": "Acesse Configurações > Segurança...",
      "categoria": "conta",
      "tags": ["senha", "segurança", "acesso"]
    },
    "score": 0.89
  }
]
```

## Estrutura do projeto

```
src/main/java/br/com/example/
├── config/          # Configurações (Security, MongoDB, etc)
├── controller/      # Endpoints REST
├── dto/             # Request/Response objects
├── exception/       # Exceções e handler global
├── model/           # Entidades (User, Faq, Role)
├── repository/      # Acesso a dados MongoDB
├── security/        # JWT e autenticação
└── service/         # Lógica de negócio
```

## Configuração

Variáveis de ambiente:

| Variável | Default | Descrição |
|----------|---------|-----------|
| `MONGODB_URI` | `mongodb://localhost:27017/faq-db` | URI do MongoDB |
| `OLLAMA_URL` | `http://localhost:11434` | URL do Ollama |
| `QDRANT_URL` | `http://localhost:6333` | URL do Qdrant |
| `JWT_SECRET` | (interno) | Secret para assinar tokens JWT |
| `JWT_EXPIRATION` | `28800` | Tempo de expiração do token (segundos) |

## Como funciona a busca semântica

1. **Cadastro de FAQ**: Quando uma FAQ é criada, a pergunta é enviada ao Ollama que gera um vetor (embedding) de 768 dimensões representando o significado semântico. Esse vetor é salvo no Qdrant.

2. **Busca**: Quando o usuário faz uma busca, a query também é transformada em vetor. O Qdrant encontra os vetores mais similares usando distância de cosseno.

3. **Resultado**: As FAQs mais relevantes são retornadas com um score de similaridade (0 a 1).

## Tecnologias utilizadas

- **Spring Boot 3.2** - Framework base
- **Spring WebFlux** - Programação reativa
- **Spring Security** - Autenticação e autorização
- **MongoDB** - Banco de dados NoSQL
- **Qdrant** - Banco de dados vetorial
- **Ollama** - Servidor de modelos de IA local
- **nomic-embed-text** - Modelo de embedding
- **JJWT** - Geração e validação de tokens JWT
- **Lombok** - Redução de boilerplate
