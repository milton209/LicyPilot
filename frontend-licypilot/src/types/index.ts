export type StatusViabilidade = 'ALTA' | 'BAIXA' | 'INCOMPATIVEL' | 'REVISAO_MANUAL';

export interface Licitacao {
  id: string;
  numeroEdital: string;
  orgaoEmissor: string;
  objeto: string;
  valorEstimado: number;
  dataAbertura: string;
  statusProcessamento: 'PROCESSANDO' | 'CONCLUIDO' | 'ERRO_EXTRACAO_PYTHON' | 'TIMEOUT_IA';
  titulo: string;
  arquivoUrl: string;
}

export interface AnaliseUsuario {
  id: string;
  licitacao: Licitacao;
  empresa: any; // Pode ser detalhado depois
  statusViabilidade: StatusViabilidade;
  diagnosticoJson: any;
}
