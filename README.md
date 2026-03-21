# 🚀 LicyPilot - Motor Inteligente de Análise de Licitações (MVP)

O **LicyPilot** é uma plataforma projetada para transformar editais de licitação complexos (PDFs) em dados estruturados. Utilizando uma arquitetura de Microsserviços e Inteligência Artificial, o sistema automatiza a triagem de oportunidades e emite um diagnóstico de viabilidade entre empresas e contratos.

---

## 🏗️ Estrutura do Projeto

O ecossistema é dividido em dois grandes blocos:
1.  **Backend (Java):** Orquestração de negócio, persistência e interface com IA (Spring AI).
2.  **Extrator (Python):** Processamento de PDFs e OCR (FastAPI + Pytesseract).

### Configurações Atuais
*   **Banco de Dados:** PostgreSQL rodando na porta **4000**.
*   **Inteligência Artificial:** Ollama utilizando o modelo **`llama3`**.
*   **Comunicação:** O Backend Java consome o Extrator Python na porta `8000`.

---

## ⚙️ Como Executar o Projeto Localmente

### 1. Pré-requisitos
*   **Java 17** e **Maven** instalados.
*   **Python 3.12** (ambiente virtual recomendado).
*   **Tesseract OCR** instalado no Sistema Operacional (necessário para o fallback de imagens).
*   **Ollama** instalado e com o modelo baixado: `ollama pull llama3`.
*   **PostgreSQL** ativo na porta **4000**.

### 2. Iniciando o Extrator Python
```bash
cd ai-python
# Crie o venv se não houver
python -m venv venv
./venv/Scripts/activate
# Instale as dependências manualmente (FastAPI, uvicorn, pdfplumber, pytesseract, pdf2image)
python main.py
```

### 3. Iniciando o Backend Java
O projeto utiliza perfis do Spring para executar fluxos automatizados via `CommandLineRunner`.

*   **Perfil `teste` (Mock):** Usa dados em memória para validar o cálculo de match e a IA rapidamente.
    ```bash
    mvn spring-boot:run -Dspring-boot.run.profiles=teste
    ```
*   **Perfil `manual-match` (JSON):** Ignora o PDF e injeta o arquivo `JSONmaster.txt` diretamente no banco.
    ```bash
    mvn spring-boot:run -Dspring-boot.run.profiles=manual-match
    ```
*   **Perfil `real` (Completo):** Processa o arquivo físico `EDITAL20263.pdf` localizado na pasta `EditalLicitaçãoTeste`.
    ```bash
    mvn spring-boot:run -Dspring-boot.run.profiles=real
    ```

---

## ⚠️ Observações de Desenvolvimento
*   **Caminhos de Arquivo:** Os runners utilizam caminhos relativos (ex: `..\\EditalLicitaçãoTeste\\`). Certifique-se de executar o comando de dentro da pasta `backend-java`.
*   **Banco de Dados:** O perfil `real` executa comandos de alteração de tabela (`ALTER TABLE`) para garantir compatibilidade com o formato de arquivo.

---

## 🗺️ Roadmap e Futuro
*   [ ] **Fase 10:** Conteinerização total com Docker e Docker Compose (Orquestração de Banco, Python e Java em um clique).
*   [ ] **Fase 11:** Integração com API do PNCP.
*   [ ] **Fase 12:** Implementação de Migrations (Flyway) para substituir comandos SQL manuais no código Java.

Desenvolvido com foco na resiliência de dados e em práticas de Clean Code.
