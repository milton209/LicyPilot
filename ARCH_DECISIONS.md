# Decisões de Arquitetura - LicyPilot

Este documento registra as premissas de design e a visão de futuro para o motor de análise de licitações.

## 1. Estratégia de Análise Granular (Anti-Omissão)
- **Decisão:** O Master JSON é processado em blocos lógicos (Habilitação, Técnica, Financeiro, etc.) em vez de uma única chamada de IA.
- **Motivação:** LLMs perdem precisão em contextos longos. Blocos menores garantem que cada cláusula do edital seja comparada individualmente com os dados da empresa.
- **Evolução:** Permitir que cada bloco utilize modelos de IA diferentes (ex: um modelo especializado em jurídico para Habilitação).

## 2. Processamento On-Demand (Just-In-Time)
- **Decisão:** O diagnóstico detalhado via IA só é disparado quando o usuário solicita a visualização da análise.
- **Motivação:** Economia de recursos computacionais e tokens. Uma licitação pode ter centenas de empresas interessadas, mas apenas uma fração solicitará o diagnóstico profundo.
- **Evolução:** Implementar cache de diagnóstico por empresa/licitação e invalidação inteligente caso os dados da empresa sejam alterados.

## 3. Desacoplamento via Master JSON
- **Decisão:** O serviço de extração (Python) e o serviço de diagnóstico (Java) comunicam-se exclusivamente através do Master JSON estruturado.
- **Motivação:** Independência tecnológica. Podemos trocar o motor de extração ou o modelo de IA sem afetar a lógica de negócio do backend.

## 4. Reatividade e UX
- **Decisão:** A estrutura de serviços foi preparada para suportar execução paralela.
- **Evolução:** Implementar WebSockets/SSE para que o diagnóstico seja exibido em "streaming", onde o usuário vê cada bloco sendo validado em tempo real na tela.

## 5. Inteligência de Viabilidade Híbrida
- **Decisão:** Separar "Viabilidade Inicial" (Lógica Matemática/CNAE) de "Diagnóstico de Match" (IA).
- **Motivação:** Fornecer feedback instantâneo ao usuário (Selo de Viabilidade) enquanto o processamento pesado de IA ocorre em segundo plano ou sob demanda.
