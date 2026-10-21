# PecuCore

API em Spring Boot do PecuCore.

## Requisitos

- Java 21
- PostgreSQL

## Como executar

1. Crie o banco no PostgreSQL (ex.: `PecuCore`).

2. Copie o `.env.example` para `.env` e preencha as variáveis (banco, `JWT_SECRET`, credenciais do admin e, se quiser usar IA, `GEMINI_API_KEY`):

   ```bash
   cp .env.example .env
   ```

3. Rode a aplicação:

   ```bash
   ./mvnw spring-boot:run
   ```

   No Windows: `mvnw.cmd spring-boot:run`

A API sobe em `http://localhost:8080` e a documentação Swagger fica em `http://localhost:8080/swagger-ui.html`.
