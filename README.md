# SGC - Sistema de Gestão Comercial (Mobile)

Componente mobile do ecossistema SGC, desenvolvido em Kotlin nativo para Android. O aplicativo atua como frente de caixa (PDV) e terminal de controle de estoque integrado em tempo real ao Firebase Firestore, contando com automação por inteligência artificial para entrada de mercadorias.

## Funcionalidades

- **Controle de Estoque:**
  - Cadastro e edição de produtos (nome, preço, quantidade em estoque).
  - Entrada rápida de mercadorias via Código de Barras (EAN) ou ID do documento.
  - Cópia rápida de identificadores direto pelo catálogo.
  - Geração e visualização de etiquetas com código de barras.

- **Automação de Entrada por Nota Fiscal:**
  - Importação de fotos e documentos de notas fiscais.
  - Extração automática de itens, quantidades e preços utilizando o modelo Gemini (Google AI / Firebase).
  - Atualização automática e atômica de catálogo sem duplicação de itens.

- **Frente de Caixa (PDV):**
  - Leitura contínua de códigos de barras via câmera.
  - Gerenciamento de carrinho de compras.
  - Pagamento via Pix com chave dinâmica.
  - Baixa de estoque com transações atômicas seguras no Firestore (validação de concorrência e integridade).
  - Histórico detalhado de vendas finalizadas.

## Tecnologias Utilizadas

- **Linguagem:** Kotlin
- **Plataforma:** Android SDK
- **Interface:** View Binding, Material Design 3 e RecyclerView
- **Banco de Dados:** Cloud Firestore (NoSQL em tempo real com suporte a transações ACID)
- **Inteligência Artificial:** Firebase AI SDK (Google Gemini Flash)
- **Controle de Versão:** Git com fluxo GitFlow
