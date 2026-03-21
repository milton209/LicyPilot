# LicyPilot - Motor Inteligente de Analise de Licitacoes (MVP)

O **LicyPilot** transforma editais em PDF em dados estruturados para apoiar triagem e diagnostico de viabilidade entre empresa e licitacao.

Arquitetura atual:
- `backend-java`: orquestracao, persistencia e integracao com IA (Spring Boot + Spring AI).
- `ai-python`: extracao de texto/OCR de PDF (FastAPI + pdfplumber + pytesseract).

## Estrutura

```text
testeClaudeCode/
  backend-java/
  ai-python/
  EditalLicitacaoTeste/
```

## Stack e Configuracoes Atuais

- **Java:** 17
- **Python:** 3.12
- **Banco:** PostgreSQL em `localhost:4000` (database `licypilot_db`)
- **IA local:** Ollama com modelo `llama3`
- **Backend Java:** porta `8081`
- **Extrator Python:** porta `8000`

Configuracoes principais em:
- `backend-java/src/main/resources/application.properties`
- `ai-python/requirements.txt`

## Pre-requisitos

Antes de rodar, garanta:
- Java 17 + Maven
- Python 3.12
- PostgreSQL ativo na porta `4000`
- Ollama instalado e modelo baixado:

```bash
ollama pull llama3
```

- Tesseract OCR instalado e no PATH (necessario para fallback OCR)

Validacoes rapidas:

```bash
java -version
mvn -v
python --version
tesseract --version
ollama list
```

## Como executar localmente (ordem recomendada)

> Importante: execute cada bloco no diretorio indicado.

### 1) Subir extrator Python

No diretorio `ai-python`:

```bash
python -m venv venv
.\venv\Scripts\activate
pip install -r requirements.txt
python main.py
```

Saida esperada: API FastAPI ativa em `http://localhost:8000`.

### 2) Subir backend Java (um perfil por vez)

No diretorio `backend-java`:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=teste
```

Perfis disponiveis:
- `teste`: fluxo mock para validar pipeline e calculo de match.
- `manual-match`: injeta JSON mestre (`JSONmaster.txt`) sem extracao de PDF.
- `real`: processa PDF real em `EditalLicitacaoTeste`.

Exemplos:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=manual-match
mvn spring-boot:run -Dspring-boot.run.profiles=real
```

## Criterios de sucesso por perfil

- **`teste`**: logs sem erro e persistencia de entidades de teste.
- **`manual-match`**: JSON mestre salvo no banco e analise inicial gerada.
- **`real`**: PDF lido, extracao por blocos concluida e `masterJson` persistido.

Se houver falha, validar:
- PostgreSQL ativo e acessivel.
- Ollama rodando com `llama3`.
- API Python em `http://localhost:8000`.

## Observacoes importantes

- Os runners usam caminhos relativos. Para evitar erro de arquivo nao encontrado, rode comandos Java a partir de `backend-java`.
- O fallback OCR depende do Tesseract corretamente instalado.
- O arquivo `JSONmaster.txt` pode estar ignorado no Git (`*.txt` no `.gitignore`).
- O perfil `real` pode executar ajustes de esquema em banco durante testes locais.

## Troubleshooting rapido

- **Erro ao conectar no banco:** confirme porta `4000` e credenciais no `application.properties`.
- **Erro de modelo IA:** rode `ollama list` e confirme `llama3`.
- **OCR nao funciona:** confirme `tesseract --version` e PATH do sistema.
- **Falha de import de Python:** recrie venv e rode `pip install -r requirements.txt`.

## Roadmap

- [ ] Conteinerizacao com Docker e Docker Compose
- [ ] Integracao com API do PNCP
- [ ] Migracoes com Flyway (substituir SQL manual em runtime)
