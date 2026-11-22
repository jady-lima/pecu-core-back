# AgroCore

Framework em Spring Boot para gestão de rebanhos, evoluído do PecuCore. Um **núcleo** comum (animais, lotes, pesagens, sanidade, custos, alertas e análise com IA) é especializado por **aplicações**, uma por espécie. Hoje existem duas: **bovinos** de corte (PecuCore) e **equinos** (EquiCore).

## Como avaliar

Requisito: **Java 21**. Não é preciso PostgreSQL nem `.env`: os testes usam banco H2 em memória e uma configuração própria (`src/test/resources/application.properties`).

```bash
./mvnw clean test        # Linux/macOS
mvnw.cmd clean test      # Windows
```

Resultado esperado: `Tests run: 220, Failures: 0, Errors: 0, Skipped: 0`.

Os testes estão organizados por requisito: cada classe tem um `@DisplayName`, e cada grupo `@Nested` começa com "Requisito: ...". Para ver a árvore de requisitos, rode os testes pela IDE (no IntelliJ: botão direito em `src/test/java` → *Run Tests*).

Algumas mensagens de erro no console **são esperadas**:
- `APPLICATION FAILED TO START`, no `AplicacaoSemImplementacaoTest`: o teste sobe, de propósito, uma aplicação sem implementações.
- Pilhas de `IllegalStateException: detalhe interno` e `IA fora do ar`, no `GlobalExceptionHandlerTest`: são os erros simulados que o teste confere.

### Mapa dos testes

| Grupo | Testes | Classes | O que provam |
|---|---|---|---|
| Framework de ponta a ponta | 12 | `AplicacaoBovinosAtivaTest`, `AplicacaoEquinosAtivaTest`, `AplicacaoSemImplementacaoTest`, `SystemApplicationTests` | O sistema inteiro sobe com bovinos ou com equinos, escolhidos pela propriedade `agrocore.aplicacao`; uma aplicação sem implementações é barrada na inicialização |
| Arquitetura e convenções | 16 | `ArquiteturaPacotesTest`, `ContratosPontosFlexiveisTest`, `ContratoApiTest`, `ConvencaoNomesTest`, `DocumentacaoApiTest` | O núcleo não conhece as aplicações, e as aplicações não se conhecem; todo ponto flexível tem contrato, e toda aplicação o cumpre; a API só devolve DTOs e está documentada; os nomes seguem a convenção do grupo |
| Contratos dos pontos flexíveis | 46 | `PontosFlexiveisBovinosTest`, `PontosFlexiveisEquinosTest` (herdam de `core/extensao/**/*Contrato`) | Cada implementação respeita o que o núcleo supõe dela |
| Regras do núcleo | 95 | `core/service/**`, `MetaTest` | Desempenho e meta em faixa, ranking, carência e restrições sanitárias, lote e metas por fase, animal, vacinas, cálculos estatísticos |
| Regras dos equinos | 18 | `equinos/service/**` | Metas padrão com fonte, fase pela idade, ranking por proximidade da faixa, exames exigidos na venda |
| Infraestrutura | 33 | `core/config/**`, `core/exception/**`, `core/ia/**` | Segurança, formato padrão de erro, configuração e integração com a IA (sem acessar a rede) |

## Arquitetura

```
src/main/java/com/pecucore/system/
├── core/           núcleo (frozen spots): entidades, serviços, controllers, alertas, IA
│   └── extensao/   pontos flexíveis (hot spots) e mecanismo de ativação da aplicação
├── bovinos/        aplicação bovinos de corte
└── equinos/        aplicação equinos
```

### Pontos flexíveis

São as interfaces anotadas com `@PontoFlexivel`. A aplicação ativa precisa ter **exatamente uma** implementação de cada uma; se não tiver, o `ValidadorPontosFlexiveis` impede o sistema de subir.

| Ponto flexível | O que a aplicação define | Bovinos | Equinos |
|---|---|---|---|
| `IndicadorDesempenho` | Como medir o desempenho, a escala de condição corporal e o critério do ranking | GMD (kg/dia); escala de 1 a 5; maior ganho = melhor | Variação de peso (kg/dia); escala de 1 a 9 (Henneke, 1983); dentro da faixa da meta = melhor |
| `PoliticaAgrupamento` | Fases do lote, meta padrão por fase, sugestão de fase pela idade e saídas do rebanho | Cria, recria, engorda, corte, reprodução, quarentena, desmame; sem meta padrão; saídas: venda, óbito e abate | Potro, jovem, adulto, reprodução, observação; metas do NRC (2007); saídas: venda e óbito |
| `PoliticaRestricaoSanitaria` | Fim da carência, saídas bloqueadas na carência e restrições próprias | Venda e abate bloqueados na carência | Venda bloqueada na carência e quando falta vacina ou exame exigido em dia |
| `DescricaoAplicacao` | Nome do sistema e espécie (Swagger e prompt da IA) | PecuCore, bovinos de corte | EquiCore, equinos |

Ganchos opcionais do núcleo: tipos de alerta (`ProvedorTiposAlerta`) e verificadores de alerta (`VerificadorAlerta`).

### Meta de desempenho de um lote

A meta é uma faixa (mínimo, máximo ou os dois). Ela vem da primeira destas fontes que existir:
1. meta própria do lote (`PUT /lotes/{id}/meta-desempenho`);
2. meta cadastrada para a fase (`/metas-desempenho`, só administrador);
3. meta padrão da aplicação (`PoliticaAgrupamento.getMetaDaFase`).

## Escolher a aplicação

A aplicação é escolhida pela variável `AGROCORE_APLICACAO` no `.env` (propriedade `agrocore.aplicacao`): `bovinos` ou `equinos`. `ovinos` está previsto, mas ainda não tem implementação; com ele, o sistema se recusa a subir.

Cada aplicação deve usar o **próprio banco**: as tabelas criadas por uma não aceitam as fases da outra.

## Criar uma nova aplicação

1. Criar o pacote `com.pecucore.system.<especie>` e o valor correspondente em `AgroCoreProperties.Aplicacao`.
2. Criar a classe de configuração com `@Configuration`, `@AplicacaoAgroCore(Aplicacao.<ESPECIE>)` e `@ComponentScan` do pacote (modelo: `EquinosConfig`), e incluí-la no `@Import` do `SystemApplication`.
3. Implementar os 4 pontos flexíveis.
4. Criar `src/test/java/com/pecucore/system/<especie>/PontosFlexiveis<Especie>Test.java`, com um `@Nested` estendendo cada `*Contrato`.

Os testes de arquitetura cobram os passos 2 e 4 e impedem que a nova aplicação use código de outra.

## Fontes dos valores dos equinos

- **Escala de condição corporal:** Henneke, D. R. et al. (1983). *Relationship between condition score, physical measurements and body fat percentage in mares*. Equine Veterinary Journal, 15(4), 371–372.
- **Ganho de peso de potros e jovens:** National Research Council (2007). *Nutrient Requirements of Horses*, 6ª ed., tabela de requisitos de cavalos em crescimento, reproduzida no MSD Veterinary Manual. Os valores são para cavalos de cerca de 500 kg adultos.
- **Faixa do adulto (±0,1 kg/dia):** **critério do projeto** (manutenção = peso estável), não é valor da literatura.
- **Exames para trânsito de equídeos (AIE, mormo):** Programa Nacional de Sanidade dos Equídeos (MAPA), IN MAPA nº 6/2018 alterada pela Portaria MAPA nº 593/2023, e normas estaduais. As exigências variam de estado para estado; por isso o sistema não fixa quais exames são obrigatórios: o usuário marca a vacina ou o exame como "exigido para trânsito".

Os valores padrão podem ser substituídos pelas metas cadastradas por fase e pela meta própria do lote.

## Limitações conhecidas

- O sistema não registra o **resultado** dos exames (positivo ou negativo). Os exames são cadastrados como vacinas com validade, e só exames negativos devem ser lançados. Uma entidade própria para exames fica como trabalho futuro.
- As metas padrão dos equinos supõem porte adulto de cerca de 500 kg.
- Na meta cadastrada por fase, o mínimo é obrigatório, por compatibilidade com a tabela já existente.
- O front-end ainda não acompanha todas as mudanças da API.

## Como executar

Requisitos: Java 21 e PostgreSQL.

1. Crie o banco no PostgreSQL (ex.: `PecuCore`).

2. Copie o `.env.example` para `.env` e preencha as variáveis (banco, `JWT_SECRET`, credenciais do admin, `AGROCORE_APLICACAO` e, se quiser usar IA, `GEMINI_API_KEY`):

   ```bash
   cp .env.example .env
   ```

3. Rode a aplicação:

   ```bash
   ./mvnw spring-boot:run
   ```

   No Windows: `mvnw.cmd spring-boot:run`

A API sobe em `http://localhost:8080`, e a documentação Swagger fica em `http://localhost:8080/swagger-ui.html`.