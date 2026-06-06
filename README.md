# LicyPilot - Motor Inteligente de Análise de Licitações (MVP)

O **LicyPilot** transforma editais em PDF em dados estruturados para apoiar triagem e diagnóstico de viabilidade entre empresa e licitação, utilizando Inteligência Artificial (GPT-4o via GitHub Models).

## 🚀 Arquitetura e Estrutura

- `backend-java`: Orquestração, persistência e integração com IA (Spring Boot + Spring AI + GitHub Models).
- `ai-python`: Extração de texto/OCR de PDF (FastAPI + pdfplumber + Tesseract OCR).
- `frontend-licypilot`: Interface web moderna (React + TypeScript + Tailwind 4).
- `EditalLicitacaoTeste`: Pasta contendo PDFs para testes locais.

## ✨ Funcionalidades Principais

- **Extração Inteligente:** Converte PDFs complexos em um "Master JSON" estruturado.
- **Diagnóstico de Match:** IA analisa se a empresa é **Apta** ou se possui **Pendências** para o edital.
- **Streaming de Resposta (SSE):** O diagnóstico da IA é exibido em tempo real, com persistência de estado (você pode sair e voltar da página que a análise continua).
- **Perfil Corporativo Estruturado:** Cadastro de Acervo Técnico (Especialidades) e Documentos Regularizados.
- **Visualização Side-by-Side:** PDF original lado a lado com a análise estruturada.
- **Painel Admin:** Ferramentas de reset seletivo (Análises, Editais ou Tudo) para testes e demos.

## 🛠️ Stack Tecnológica

- **Java:** 17
- **Python:** 3.12
- **Node.js:** 18+
- **Banco:** PostgreSQL (padrão porta `4000`)
- **IA:** GPT-4o-mini (GitHub Models API)
- **OCR:** Tesseract OCR (necessário para PDFs escaneados)

---

## 💻 Guia de Instalação (Setup do Zero)

Siga estes passos para rodar o projeto em uma nova máquina após o clone.

### 1. Pré-requisitos
- **Java 17** (JDK) instalado.
- **Node.js 18+** e npm instalados.
- **Python 3.12** instalado.
- **PostgreSQL** rodando (padrão: porta `4000`).
- **Tesseract OCR** instalado e adicionado ao PATH do sistema.
- **Ollama** (Opcional - caso queira rodar localmente, altere o `application.properties`).

### 2. Configuração do Banco de Dados
1. Crie um banco de dados chamado `licypilot_db` no seu PostgreSQL.
2. Certifique-se de que o PostgreSQL está acessível na porta `4000` (ou ajuste no `application.properties` do Java).

### 3. Configuração de Variáveis de Ambiente
O backend Java necessita de um Token do GitHub para acessar a IA:
- No Windows: `setx GITHUB_TOKEN "seu_token_aqui"`
- No Linux/Mac: `export GITHUB_TOKEN="seu_token_aqui"`
*(Ou configure diretamente no `application.properties` para testes rápidos).*

### 4. Executando os Serviços

#### A) Extrator Python (`ai-python`)
```bash
cd ai-python
python -m venv venv
.\venv\Scripts\activate  # No Windows
# source venv/bin/activate # No Linux/Mac
pip install -r requirements.txt
python main.py
```
*O serviço subirá em `http://localhost:8000`*

#### B) Backend Java (`backend-java`)
```bash
cd backend-java
mvn clean install
mvn spring-boot:run
```
*Para carregar dados iniciais de teste (Empresa/Usuário), use:*
`mvn spring-boot:run "-Dspring-boot.run.arguments=--fase7"`
*O serviço subirá em `http://localhost:8081`*

#### C) Frontend React (`frontend-licypilot`)
```bash
cd frontend-licypilot
npm install
npm run dev
```
*Acesse em `http://localhost:5173`*

---

## 📊 Painel do Administrador
Acesse a rota `/admin` no navegador para gerenciar o ambiente:
- **Limpar Análises:** Apaga apenas os resultados da IA.
- **Resetar Editais:** Apaga editais e análises, mantendo seu Perfil de Empresa.
- **Reset Total:** Volta o sistema ao estado zero (apaga tudo).

## 📝 Observações Técnicas
- **CORS:** O backend está configurado para aceitar requisições de `http://localhost:5173`.
- **Master JSON:** O sistema valida a integridade da extração antes de liberar o Match.
- **Persistence:** Se você fechar a aba durante uma análise, ela continuará sendo processada no servidor. Ao voltar, o sistema retomará o progresso automaticamente.
