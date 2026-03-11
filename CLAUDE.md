PRD e Instruções: Projeto LicyPilot – Motor de Análise de Licitações (MVP)
0. Diretrizes para o Gemini cli (Você)
Papel: Atue como Arquiteto de Software Sênior e Especialista em Engenharia de Prompt.
Arquitetura: Monorepo. Pastas: /backend-java e /ai-python.

Estratégia Anti-Omissão e Dupla Análise Gradual (Crítico):
1. Extração: O documento PDF deve ser processado em blocos pelo Python para que a IA foque em detalhes e monte o Master JSON sem omitir cláusulas.
2. Diagnóstico (Match): A comparação entre o perfil da Empresa e o Edital também deve ser feita em blocos (ex: primeiro compara Documentação, depois Acervo Técnico) para evitar resumos genéricos.
3. Dados Numéricos Puros: Obrigue a IA a extrair valores financeiros e prazos estritamente como números (Double/Int) no JSON para permitir cálculos matemáticos via Java.

1. Visão Geral
O LicyPilot transforma Editais em PDF em dados estruturados. O sistema utiliza Python para extração/limpeza de texto de alta precisão e Java (Spring AI) para orquestrar a inteligência, validar o esquema e persistir o "JSON Mestre".

2. Stack Tecnológica
Backend Principal: Java 17, Spring Boot 3.4+, Spring AI (Ollama).
Serviço de Extração: Python 3.12, FastAPI, pdfplumber (foco em manter referência de página).
Banco de Dados: PostgreSQL 16 com extensão para JSONB.
Motor LLM: Ollama (Local/Desenvolvimento: llama3:8b-instruct).

3. Modelo de Dados Relacional (PostgreSQL)
Entidade Licitacao: id (UUID), numeroEdital, orgaoEmissor, objeto, valorEstimado (Double), dataAbertura, statusProcessamento, masterJson (JSONB), arquivoUrl.
Entidade Empresa: id (UUID), cnpj, razaoSocial, capitalSocial (Double), cnaes (List).
Entidade AnaliseUsuario (Match): id (UUID), licitacao_id, empresa_id, statusViabilidade, diagnosticoJson (JSONB).

4. O JSON Mestre (Gabarito da IA)
Estrutura que o Java monta após consolidar as análises: Identificação, Prazos/Valores, Logística/Amostras, Habilitação Detalhada (Lista), Qualificação Técnica (Lista), Regras da Disputa e Análise de Risco (Lista).

5. Fases de Implementação

Fase 1: Infraestrutura e Backend Java (Base Spring AI)
Fase 2: O Garimpeiro Python (Limpeza e Segmentação + OCR Fallback)
Fase 3: Orquestração no Java (O Coração da IA - Loop de Extração Assíncrono)
Fase 4: Persistência e Pré-Filtro Lógico (Gatilho de Viabilidade e CNAE Match)
Fase 5: O Diagnóstico de Match (A Inteligência Quente - Comparação Detalhada Edital vs Empresa)
Fase 6: Interface de Teste (CLI CommandLineRunner)
Fase 7: Requisitos Não Funcionais e Defesas (OCR, Resiliência a Timeout, Cache de Extração, Fatiamento de Contexto)

--------------------------------------------------------------------------------
6. Fases Futuras e Escalabilidade (Planejamento Pós-MVP)

Fase 8: Integração com APIs de Licitações
Implementar integradores automáticos (ex: PNCP ou APIs privadas) para que o sistema busque novos editais automaticamente sem depender de upload manual, servindo como um "Radar de Oportunidades".

Fase 9: Estratégia Cloud e APIs de Alta Performance
Quando o processamento local (Ollama) atingir o limite de hardware para grandes volumes:
- Transição para APIs Cloud (Groq, Together AI ou OpenAI) via application.properties.
- Implementação de Interceptadores de Tokens para controle de custos por análise.

Fase 10: Inteligência de Imagens e Análise Humana
Aprimorar o sistema para que, ao detectar tabelas complexas ou plantas baixas que o OCR não processe com 100% de confiança:
- Marcar `analise_humana_obrigatoria = true`.
- Gerar alerta específico indicando página e seção para conferência visual do usuário.
- O sistema nunca omitirá anexos visuais, mesmo que não consiga interpretá-los.
