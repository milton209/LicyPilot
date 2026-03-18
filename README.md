# LicyPilot - Motor de Análise de Licitações (MVP)

O **LicyPilot** é uma plataforma inteligente projetada para transformar editais de licitação complexos (PDFs) em dados estruturados e acionáveis. Utilizando uma arquitetura híbrida de Microserviços e Inteligência Artificial, o sistema automatiza a triagem de oportunidades e o diagnóstico de conformidade (match) entre empresas e editais governamentais.

---

## 🚀 Visão Geral e Propósito

A aplicação resolve o problema da densidade e ambiguidade dos editais brasileiros. Através de um fluxo de processamento distribuído, o LicyPilot extrai cláusulas críticas, prazos e valores, permitindo que empresas identifiquem instantaneamente se possuem os requisitos necessários para vencer uma disputa.

---

## 🏗️ Arquitetura e Estratégias Core

O projeto fundamenta-se em **abordagens** de engenharia de software e IA de ponta:

### 1. Estratégia Anti-Omissão (Processamento Granular)
Diferente de abordagens tradicionais que enviam documentos inteiros para uma LLM, o LicyPilot utiliza a técnica de **Análise Gradual**. O documento é segmentado em blocos lógicos pelo serviço Python e processado sequencialmente pelo Java. Isso evita a perda de contexto e garante que nenhuma cláusula de habilitação ou risco seja ignorada.

### 2. Conceito de "Master JSON"
O **Master JSON** atua como o contrato de dados e a "fonte da verdade" da aplicação. Ele consolida todas as informações extraídas (identificação, prazos, habilitação técnica e jurídica) em um esquema estritamente tipado (Java Records), facilitando a integração entre o motor de IA e a lógica de negócio.

### 3. Inteligência de Viabilidade Híbrida
O sistema aplica duas camadas de filtragem:
- **Lógica Matemática (Fria):** Cruzamento instantâneo de CNAE, capital social e prazos via código Java puro.
- **Diagnóstico de Match (IA/Quente):** Análise semântica profunda via LLM para validar acervos técnicos e exigências qualitativas.

### 4. Processamento Just-In-Time (On-Demand)
Para otimização de recursos e redução de custos de tokens, o diagnóstico pesado de IA só é disparado quando o usuário solicita a análise detalhada, mantendo a escalabilidade do sistema.

### 5. Desacoplamento via JSONB
A utilização de **PostgreSQL com JSONB** permite que a aplicação armazene a variabilidade infinita dos editais sem a necessidade de migrações constantes de esquema, mantendo a performance de consulta.

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia | Papel |
| :--- | :--- | :--- |
| **Backend Principal** | Java 17 / Spring Boot 3.4 | Orquestração de serviços, persistência e lógica de negócio. |
| **Inteligência Artificial** | Spring AI / Ollama (Llama 3) | Processamento de linguagem natural e extração de dados. |
| **Serviço de Extração** | Python 3.12 / FastAPI | Limpeza de PDF, segmentação de texto e OCR (fallback). |
| **Banco de Dados** | PostgreSQL 16 | Armazenamento relacional e documentos JSONB. |
| **Processamento de PDF** | pdfplumber / Tesseract | Extração de texto e tabelas com alta precisão. |

---

## 📋 Fluxo de Execução (Pipeline)

1. **Ingestão:** Upload do edital PDF via API REST (Java).
2. **Segmentação (Python):** O Python limpa o ruído (cabeçalhos/rodapés) e divide o edital em blocos lógicos.
3. **Extração Gradual (Java + IA):** O Java orquestra chamadas ao LLM para cada bloco, populando o Master JSON.
4. **Merge & Validação:** A classe `MasterJsonMerger` consolida as extrações parciais em um registro único.
5. **Selo de Viabilidade:** O sistema gera um indicativo imediato de "Match" com base no perfil da empresa.

---

## 🛠️ Como Executar

### Pré-requisitos
- Banco de Dados PostgreSQL 16 (local)
- Java 17+
- Python 3.12+
- Ollama instalado localmente (modelo `llama3:8b-instruct`)

### Configuração Rápida
1. **Ollama:** `ollama run llama3:8b-instruct`
2. **Backend Java:** `cd backend-java && mvn spring-boot:run`
3. **AI Python:** `cd ai-python && uvicorn main:app --reload`

---

## 🗺️ Roadmap de Evolução
- [x] **Fase 7:** Implementação de OCR para PDFs escaneados (Imagens).
- [ ] **Fase 8:** Integração direta com APIs do PNCP (Portal Nacional de Contratações Públicas).
- [ ] **Fase 9:** Dashboard de análise de riscos contratuais e penalidades.

---

> **Nota:** Este projeto segue as diretrizes de desenvolvimento registradas em `ARCH_DECISIONS.md`. Qualquer alteração estrutural deve respeitar a estratégia de desacoplamento e análise granular.
