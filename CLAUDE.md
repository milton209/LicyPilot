PRD e Instruções: Projeto LicyPilot – Motor de Análise de Licitações (MVP)
0. Diretrizes para o Gemini cli (Você)
Papel: Atue como Arquiteto de Software Sênior e Especialista em Engenharia de Prompt.

Arquitetura: Monorepo. Pastas: /backend-java e /ai-python.

Estratégia Anti-Omissão e Dupla Análise Gradual (Crítico): > 1. Extração: O documento PDF deve ser processado em blocos pelo Python para que a IA foque em detalhes e monte o Master JSON sem omitir cláusulas.
2. Diagnóstico (Match): A comparação entre o perfil da Empresa e o Edital também deve ser feita em blocos (ex: primeiro compara Documentação, depois Acervo Técnico) para evitar resumos genéricos.
3. Dados Numéricos Puros: Obrigue a IA a extrair valores financeiros e prazos estritamente como números (Double/Int) no JSON para permitir cálculos matemáticos via Java.

Ciclo de Trabalho: Execute uma Fase de cada vez. Use o terminal para validar compilação (mvn) e execução de scripts.

Comunicação: Escreva logs claros. Se encontrar uma inconsistência no PDF, reporte-a.

1. Visão Geral
O LicyPilot transforma Editais em PDF em dados estruturados. O sistema utiliza Python para extração/limpeza de texto de alta precisão e Java (Spring AI) para orquestrar a inteligência, validar o esquema e persistir o "JSON Mestre".

2. Stack Tecnológica
Backend Principal: Java 17, Spring Boot 3.4+, Spring AI (Ollama).

Serviço de Extração: Python 3.12, FastAPI, pdfplumber (foco em manter referência de página).

Banco de Dados: PostgreSQL 16 com extensão para JSONB.

Motor LLM: Ollama (Local/Desenvolvimento: llama3:8b-instruct | Cloud/Produção: llama3:70b-instruct).

Justificativa do Arquiteto: Substituímos a família Qwen-Coder pela família Llama 3 Instruct. Modelos Coder são excelentes para gerar código, mas falham em interpretar ambiguidades e o "juridiquês" pesado dos editais brasileiros. O Llama 3 tem uma compreensão de leitura muito superior para textos complexos.

3. Modelo de Dados Relacional (PostgreSQL)
O banco deve suportar a separação entre o dado estático do edital e a análise dinâmica por usuário.

Entidade Licitacao: id (UUID), numeroEdital, orgaoEmissor, objeto, valorEstimado (Double), dataAbertura, statusProcessamento (Enum: PROCESSANDO, CONCLUIDO, ERRO_EXTRACO_PYTHON, TIMEOUT_IA), masterJson (usar @JdbcTypeCode(SqlTypes.JSON)), arquivoUrl.

Entidade Empresa: id (UUID), cnpj, razaoSocial, capitalSocial (Double), cnaes (List).

Entidade AnaliseUsuario (Tabela de Cruzamento/Match):

id (UUID)

licitacao_id (Relacionamento ManyToOne)

empresa_id (Relacionamento ManyToOne)

statusViabilidade (Enum: ALTA, BAIXA, INCOMPATIVEL, REVISAO_MANUAL) -> Representa o "Selo de Oportunidade".

diagnosticoJson (JSONB) -> Guarda o resultado detalhado da IA para não refazer consultas.

4. O JSON Mestre (Gabarito da IA)
Esta é a estrutura final que o Java deve montar após consolidar as análises graduais. A IA deve ser instruída a nunca inventar dados; se não encontrar, retornar null.

JSON
{
  "identificacao_projeto": {
    "processo_licitatorio": "string",
    "numero_edital": "string",
    "orgao_emissor": "string",
    "modalidade": "ex: Concorrência Eletrônica",
    "criterio_julgamento": "ex: Menor Preço",
    "objeto_completo": "string"
  },
  "prazos_valores_e_pagamento": {
    "data_abertura_sessao": "ISO8601",
    "valor_estimado_total": 0.0,
    "prazo_execucao_global_dias": "number",
    "prazo_para_pagamento_dias": "number (MUITO IMPORTANTE PARA MPE)",
    "condicoes_pagamento_detalhes": "string"
  },
  "logistica_e_amostras": {
    "locais_de_entrega": "string (ex: Almoxarifado central ou múltiplas escolas?)",
    "prazo_entrega_dias": "number",
    "exige_amostra_fisica": true,
    "detalhes_amostra_ou_teste": "string"
  },
  "habilitacao_detalhada": [
    {
      "categoria": "Jurídica / Fiscal / Trabalhista / Econômica",
      "nome_documento": "ex: Certidão Negativa de Falência",
      "descricao_exigencia": "string",
      "obrigatorio": true,
      "pagina_referencia": 0,
      "trecho_original": "string (PROVA DE VIDA)"
    }
  ],
  "qualificacao_tecnica_especifica": [
    {
      "tipo_exigencia": "Atestado Técnico / Visita Técnica / Registro em Conselho",
      "descricao_detalhada": "string",
      "pagina_referencia": 0,
      "trecho_original": "string (PROVA DE VIDA)"
    }
  ],
  "regras_da_disputa": {
    "modo_disputa": "ex: Aberto / Aberto e Fechado",
    "beneficio_me_epp": "string (Vantagens para MPEs)",
    "criterios_desempate": "string"
  },
  "analise_de_risco_e_penalidades": [
    {
      "tipo_risco": "ex: Multa por Atraso / Rescisão",
      "descricao_e_impacto": "string",
      "pagina_referencia": 0,
      "trecho_original": "string (PROVA DE VIDA)"
    }
  ]
}

[INSTRUÇÃO PARA A CLI SOBRE O ESQUEMA DE DADOS]:
O Master JSON acima é a base estrutural. No código Java, as seções que contêm colchetes `[ ]` (como habilitacao_detalhada e analise_de_risco_e_penalidades) DEVEM ser implementadas como `List<T>` genéricas nos Records. 

NUNCA crie variáveis hardcoded para documentos específicos (ex: não crie booleanos como "exigeCnpj" ou "exigeBalanco"). O sistema deve estar preparado para receber N itens imprevisíveis dentro dessas listas para suportar a variação infinita dos editais brasileiros.

[INSTRUÇÃO PARA A CLI SOBRE A CRIAÇÃO DOS RECORDS JAVA]
Não crie classes aninhadas complexas. Utilize a estrutura de `Record` do Java 17+ para espelhar exatamente o JSON Mestre acima. 
Onde o JSON apresenta arrays `[ ]` (como em habilitacao_detalhada, qualificacao_tecnica_especifica e analise_de_risco), você DEVE mapear como `List<T>` genéricas no Record principal.

Exemplo do Padrão Esperado:
public record MasterJsonRecord(
    IdentificacaoProjetoRecord identificacao_projeto,
    PrazosPagamentoRecord prazos_valores_e_pagamento,
    LogisticaAmostrasRecord logistica_e_amostras,
    List<HabilitacaoRecord> habilitacao_detalhada, // Lista Genérica!
    List<QualificacaoTecnicaRecord> qualificacao_tecnica_especifica,
    RegrasDisputaRecord regras_da_disputa,
    List<RiscoRecord> analise_de_risco_e_penalidades
) {}

Nota Crítica: Para o `BeanOutputConverter` do Spring AI funcionar perfeitamente com o Ollama, certifique-se de adicionar anotações `@JsonProperty("nome_exato_do_json")` e `@JsonPropertyDescription("instruções para a IA")` nos campos dos Records caso ache necessário reforçar o formato.

5. Fases de Implementação Detalhadas

Fase 1: Infraestrutura e Backend Java (Base Spring AI)
Estrutura de Pastas: Criar /backend-java e /ai-python na raiz do monorepo.

Estrutura de Entrada de Dados (API Ready):

Controller: Criar LicitacaoController.java com um endpoint POST /api/v1/licitacoes/importar.

Funcionamento: Este endpoint deve aceitar um arquivo MultipartFile. O Java recebe esse arquivo, salva o nome no banco e passa o "caminho" ou os "bytes" para o LicitacaoService iniciar a análise.

Dependências Críticas (pom.xml):

spring-ai-ollama-spring-boot-starter: Para comunicação nativa com o Ollama.

spring-boot-starter-data-jpa e postgresql: Persistência.

lombok: Redução de boilerplate.

Configuração de Banco de Dados:

Configurar application.properties para licypilot_db.

Habilitar logging de SQL para debugar o salvamento do JSONB.

Entidade Licitacao: * Implementar a classe Licitacao usando @JdbcTypeCode(SqlTypes.JSON) no campo masterJson.

Criar o LicitacaoRepository.

Fase 2: O Garimpeiro Python (Limpeza e Segmentação)
Endpoint de Extração Inteligente: O FastAPI deve ter o endpoint POST /extract.

Lógica Anti-Omissão: * O Python não deve apenas extrair o texto; ele deve limpar cabeçalhos e rodapés repetitivos que confundem a IA.

Segmentação: O Python deve retornar uma lista de objetos, onde cada objeto contém pagina_inicio, pagina_fim e o texto_limpo.

Exemplo de retorno: [{"secao": "Habilitação", "conteudo": "..."}, {"secao": "Preços", "conteudo": "..."}].

Requirements: Incluir pdfplumber para precisão em tabelas.

Fase 3: Orquestração no Java (O Coração da IA)
Cliente de Integração: Criar um PythonClient usando RestClient para chamar o FastAPI e obter os segmentos do PDF.

Processamento Assíncrono:
Como a análise da IA demora, o endpoint da API não deve travar a requisição.
Lógica: O Java recebe o arquivo, responde 202 Accepted ("Recebido! Estamos analisando") e inicia o processo em uma @Async task. O status do banco fica PROCESSANDO.

Serviço de Análise Gradual e Tipada (LicitacaoService):
[INSTRUÇÃO CRÍTICA PARA A CLI]: NÃO faça merge de strings JSON. A extração deve ser estritamente tipada usando `BeanOutputConverter` do Spring AI.

1. Modelagem: Crie os Java Records correspondentes à estrutura do "JSON Mestre" (ex: MasterJsonRecord, HabilitacaoRecord, RiscoRecord).

2. O Loop de Extração: O Java recebe a lista de blocos de texto do Python. O serviço deve iterar sobre CADA bloco sequencialmente (sem pular nenhum).

3. Prompt com OutputConverter: Para cada bloco, o ChatClient do Spring AI envia o texto do bloco + o prompt instruindo a extração, passando o `BeanOutputConverter(MasterJsonRecord.class)`.

4. Fusão Segura (O Merge em Java): Crie uma classe utilitária (ex: `MasterJsonMerger`) no Java. A cada iteração do loop, o Java recebe um objeto `MasterJsonRecord` parcial da IA e soma os valores não-nulos em um objeto `MasterJsonRecord` Acumulador.

   - Campos únicos (ex: numero_edital) são preenchidos na primeira vez que a IA encontrar e mantidos.
   - Campos de Lista (ex: habilitacao_detalhada) recebem um `.addAll()` a cada nova iteração que trouxer itens.

5. Finalização: Ao fim do loop, o `MasterJsonRecord` Acumulador é convertido para JSONB e salvo na tabela Licitacao. O status é atualizado para CONCLUIDO (ou TIMEOUT_IA em caso de falha).

Fase 4: Persistência e Pré-Filtro Lógico (A Lógica Fria)

Validação de Integridade: O Java valida se os campos obrigatórios do Master JSON (como datas e valores) estão preenchidos. Salva na tabela Licitacao.

Gatilho de Viabilidade: Imediatamente após salvar, o Java cruza via código (sem IA) o valorEstimado da licitação com o capitalSocial das empresas ativas.

Selo de Oportunidade: Cria registros na tabela AnaliseUsuario marcando o statusViabilidade inicial (ex: INCOMPATIVEL se o capital for muito baixo), poupando processamento futuro.

Fase 5: O Diagnóstico de Match (A Inteligência Quente)

Serviço Sob Demanda: Criar o DiagnosticoService. Este serviço é ativado quando o usuário quer ver os detalhes de uma licitação.

Verificação de Cache: O serviço olha se a tabela AnaliseUsuario já tem o diagnosticoJson preenchido. Se sim, retorna do banco.

Análise Gradual da IA: Se não tiver, o Java busca o perfil da Empresa, pega o Master JSON e envia para a IA em blocos:

Prompt 1: "Compare a documentação do usuário X com a seção de habilitação jurídica do Edital Y. Retorne o status (OK, ALERTA, PENDENCIA) para cada exigência."

Prompt 2: "Faça o mesmo para a capacitação técnica."

Persistência do Diagnóstico: Salva o JSON final resultante na coluna diagnosticoJson da tabela AnaliseUsuario para acesso instantâneo futuro.

Fase 6: Interface de Teste (CLI)

Criar um CommandLineRunner simples no Java para testar o fluxo localmente.

Ex 1: java -jar app.jar --extrair "C:/editais/edital.pdf"

Ex 2: java -jar app.jar --diagnostico <id_licitacao> <id_empresa>

fase 7. Requisitos Não Funcionais, Defesas e Preocupações Críticas
[INSTRUÇÃO PARA A CLI]: A arquitetura DEVE prever e mitigar os seguintes gargalos e riscos inerentes ao processamento de LLMs e PDFs governamentais:

A. Defesa contra PDFs "Imagem" (Estratégia de Fallback com OCR)
Problema: Muitos editais (especialmente de prefeituras menores) são documentos impressos e escaneados, sem texto selecionável nativamente. O `pdfplumber` tradicional retornará vazio, mas o LicyPilot NÃO pode recusar a análise.
Solução (Worker Python): O script FastAPI deve implementar uma camada de OCR (Optical Character Recognition) como rota de fallback.
Lógica Obrigatória para a CLI:

1. Tentativa Rápida: O sistema tenta extrair o texto da página usando `pdfplumber`.

2. Validação de Conteúdo: Se o texto extraído da página for vazio ou muito curto (ex: menor que 50 caracteres), o sistema identifica que é um PDF escaneado (imagem).

3. Acionamento do OCR: O Python utiliza a biblioteca `pdf2image` para converter a página do PDF em imagem, e em seguida aplica o `pytesseract` (Tesseract OCR) ou `EasyOCR` para ler as palavras da imagem.

4. Concatenação e Retorno: O texto extraído via OCR é injetado no bloco de texto normal e devolvido para o Java.
Nota de Performance: Como o OCR consome mais CPU e tempo, isso reforça a obrigatoriedade absoluta do Java tratar a requisição de forma assíncrona (`@Async`), mantendo o status no banco como `PROCESSANDO` para não gerar Timeout na tela do usuário.

B. Defesa contra Rate Limits da LLM e Timeout
Problema: Enviar 20 blocos de texto simultaneamente para a API do LLM (Together AI/Groq) resultará em erro 429 (Too Many Requests).
Solução (Java Backend): O `LicitacaoService` deve usar processamento sequencial no loop de blocos e implementar resiliência (Spring Retry) com Exponential Backoff. Se a IA falhar ou der timeout, o Java deve tentar novamente antes de abortar a missão.

C. Otimização de Banco de Dados e Cache Único
Problema: O processo de extração custa dinheiro e tempo. Se 5 usuários da plataforma pedirem a análise do MESMO edital, o sistema não pode extrair o PDF 5 vezes.
Solução (Java Backend): Antes de iniciar a Fase 3, o Controller deve verificar se o `numeroEdital` (ou hash do arquivo) já existe na tabela `Licitacao` com status CONCLUIDO. Se sim, ignora a extração e aproveita o `MasterJson` existente. Adicionar Índices (Indexes) no PostgreSQL para as colunas `licitacao_id`, `empresa_id` e `status_viabilidade` para garantir consultas ultra-rápidas.

D. Fatiamento de Contexto no Match (Fase 5)
Problema: Se o Java enviar todos os documentos da Empresa de uma vez, o prompt ultrapassa a janela de contexto e a IA "alucina" validações incorretas.
Solução (Java Backend): No `DiagnosticoService`, o cruzamento Edital vs. Empresa DEVE ser fatiado por domínio. Mande apenas os "Dados Fiscais da Empresa" para validar a "Habilitação Fiscal do Edital", garantindo que a IA sempre trabalhe com prompts curtos e focados. Exigir sempre que a IA retorne o `trecho_original` que embasou sua decisão.