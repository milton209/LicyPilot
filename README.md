# LicyPilot - Motor Inteligente de Análise de Licitações (MVP) 🚀

O **LicyPilot** é uma solução de alta performance que transforma editais de licitação complexos (em PDF) em dados estruturados para apoiar triagem e diagnóstico de viabilidade técnica entre empresas e certames públicos. Utilizando Inteligência Artificial de última geração e uma arquitetura robusta de micro-serviços, o sistema oferece uma análise profunda e automatizada de editais.

Este projeto foi desenvolvido com foco em **escalabilidade**, **UX moderna** e **integração avançada com LLMs**, sendo uma excelente demonstração de competência técnica em engenharia de software fullstack.

---

## 🚀 Arquitetura e Estrutura do Projeto

O sistema é dividido em três componentes principais que trabalham em harmonia:

- **`backend-java`**: Orquestração central, persistência de dados e integração inteligente com IA utilizando Spring AI.
- **`ai-python`**: Micro-serviço especializado em extração de dados brutos e OCR de alta precisão.
- **`frontend-licypilot`**: Interface web reativa e moderna, focada em visualização de dados e experiência do usuário.

---

## 🛠️ Stack Tecnológica de Elite

### **Backend (Orquestração e IA)**
*   **Java 17 + Spring Boot 3.4.3:** Base sólida e performática para a lógica de negócio empresarial.
*   **Spring AI (v1.0.0-M6):** Framework de ponta para integração nativa com modelos de IA (GPT-4o via GitHub Models).
*   **Spring Data JPA:** Abstração de persistência robusta para comunicação com PostgreSQL.
*   **SSE (Server-Sent Events):** Streaming de dados em tempo real para exibir o diagnóstico da IA conforme ele é gerado.
*   **Maven:** Gestão profissional de dependências e ciclo de vida de build.

### **AI Python (Extração e OCR)**
*   **Python 3.12 + FastAPI:** API de alta velocidade para processamento assíncrono de documentos pesados.
*   **pdfplumber:** Ferramenta cirúrgica para extração de texto e tabelas de documentos PDF.
*   **Tesseract OCR + pytesseract:** Fallback inteligente com visão computacional para leitura de documentos escaneados.
*   **Pydantic:** Garantia de integridade de dados na comunicação entre serviços.

### **Frontend (Interface do Usuário)**
*   **React 19:** A versão mais recente da biblioteca líder de mercado, focada em performance e hooks modernos.
*   **Vite 8:** Ferramenta de build de última geração para desenvolvimento instantâneo.
*   **Tailwind CSS 4:** Estilização baseada em utilitários para um design responsivo e visual "Premium".
*   **TypeScript:** Segurança de tipos e maior manutenibilidade do código da interface.
*   **Lucide React & Framer Motion:** Ícones modernos e animações fluidas para uma UX superior.

---

## ✨ Funcionalidades Principais

*   **Extração Inteligente:** Converte PDFs complexos em um "Master JSON" estruturado, validando a integridade dos dados.
*   **Diagnóstico de Match (IA GPT-4o):** Análise automática se a empresa está **Apta** ou possui **Pendências** baseada no edital.
*   **Streaming de Resposta (SSE):** O diagnóstico da IA é exibido em tempo real, com persistência automática (pode sair e voltar que a análise continua).
*   **Perfil Corporativo Estruturado:** Cadastro detalhado de Acervo Técnico (Especialidades) e Documentos.
*   **Visualização Side-by-Side:** O PDF original e a análise estruturada são exibidos lado a lado para fácil conferência.
*   **Painel Admin Estratégico:** Ferramentas de reset seletivo (Análises, Editais ou Tudo) para testes rápidos e demonstrações.

---

## 💻 Guia de Instalação e Execução

### 1. Pré-requisitos
*   **Java 17 (JDK)** instalado.
*   **Node.js 18+** e npm instalados.
*   **Python 3.12** instalado.
*   **PostgreSQL** rodando (padrão: porta `4000`).
*   **Tesseract OCR** instalado e adicionado ao PATH do sistema.

### 2. Configuração de Variáveis de Ambiente
O backend exige as seguintes variáveis para segurança e integração:

**Windows:**
```powershell
setx GITHUB_TOKEN "seu_token_aqui"
setx DB_PASSWORD "senha_do_seu_postgres"
```
*(Reinicie o terminal após configurar).*

**Linux/Mac:**
```bash
export GITHUB_TOKEN="seu_token_aqui"
export DB_PASSWORD="senha_do_seu_postgres"
```

### 3. Executando os Serviços

#### **A) Extrator Python (`ai-python`)**
```bash
cd ai-python
python -m venv venv
.\venv\Scripts\activate  # Windows
source venv/bin/activate # Linux/Mac
pip install -r requirements.txt
python main.py
```
*Serviço disponível em: `http://localhost:8000`*

#### **B) Backend Java (`backend-java`)**
```bash
cd backend-java
mvn clean install
mvn spring-boot:run
```
*Para carregar dados iniciais de teste, use: `mvn spring-boot:run "-Dspring-boot.run.arguments=--fase7"`*

#### **C) Frontend React (`frontend-licypilot`)**
```bash
cd frontend-licypilot
npm install
npm run dev
```
*Acesse em: `http://localhost:5173`*

---

## 📊 Painel do Administrador
Acesse a rota `/admin` no navegador para gerenciar o ambiente de testes:
- **Limpar Análises:** Apaga apenas os resultados gerados pela IA.
- **Resetar Editais:** Remove editais e análises, preservando o perfil da empresa.
- **Reset Total:** Restaura o sistema ao estado zero.

## 📝 Observações Técnicas
- **CORS:** Backend configurado para aceitar requisições do frontend em `localhost:5173`.
- **Master JSON:** Validação rigorosa de integridade antes da liberação do Match pela IA.
- **Persistência:** Processamento assíncrono no servidor garante que análises não sejam perdidas se a aba for fechada.

---
*Este projeto demonstra competência em engenharia de software fullstack, integração avançada de IA e design de sistemas modernos e escaláveis.*
