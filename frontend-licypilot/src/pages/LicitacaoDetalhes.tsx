import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  ArrowLeft, 
  FileText, 
  BrainCircuit, 
  CheckCircle2, 
  AlertTriangle,
  ChevronDown,
  ChevronUp,
  Loader2
} from 'lucide-react';

const LicitacaoDetalhes = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [licitacao, setLicitacao] = useState<any>(null);
  const [empresa, setEmpresa] = useState<any>(null);
  const [analiseId, setAnaliseId] = useState<string | null>(null);
  const [isAnalisando, setIsAnalisando] = useState(false);
  const [vereditoText, setVereditoText] = useState('');
  const [showFullReport, setShowFullReport] = useState(false);
  const [openSection, setOpenSection] = useState<string | null>(null);
  const [blocosTecnicos, setBlocosTecnicos] = useState<any[]>([]);
  const [viewMode, setViewMode] = useState<'pdf' | 'data'>('pdf');

  useEffect(() => {
    console.log("Iniciando carregamento de detalhes para ID:", id);
    // 1. Carrega o Edital
    fetch(`/api/v1/licitacoes`)
      .then(res => res.json())
      .then(data => {
        if (Array.isArray(data)) {
          const found = data.find((l: any) => l.id === id);
          if (!found) {
            console.warn("Edital não encontrado no banco.");
            navigate('/dashboard');
            return;
          }
          console.log("Edital carregado:", found.titulo || found.nomeArquivo);
          setLicitacao(found);
        }
      })
      .catch(err => console.error("Erro ao buscar editais:", err));

    // 2. Carrega a Empresa
    fetch('/api/v1/empresas')
      .then(res => res.json())
      .then(data => {
        if (Array.isArray(data) && data.length > 0) {
          const currentEmpresa = data[0];
          setEmpresa(currentEmpresa);
          console.log("Empresa ativa:", currentEmpresa.razaoSocial);

          // 3. Verifica se já existe uma análise
          fetch(`/api/v1/analises/licitacao/${id}`)
            .then(res => res.json())
            .then(analises => {
              if (Array.isArray(analises)) {
                const myAnalise = analises.find((a: any) => a.empresa && a.empresa.id === currentEmpresa.id);
                if (myAnalise) {
                  console.log("Análise anterior encontrada:", myAnalise.id, "Status:", myAnalise.statusProcessamento);
                  setAnaliseId(myAnalise.id);
                  
                  if (myAnalise.statusProcessamento === 'PROCESSANDO') {
                    console.log("Análise ainda em curso. Retomando conexão...");
                    iniciarAnalise(myAnalise.id); // Re-conecta ao stream
                    return;
                  }

                  if (myAnalise.diagnosticoJson) {
                    setVereditoText(myAnalise.diagnosticoJson.veredito_do_especialista || '');
                    const areas = myAnalise.diagnosticoJson.detalhes_por_area;
                    if (areas) {
                      const mapBlocks = Object.keys(areas).map(key => ({
                          id: key,
                          title: key.toUpperCase().replace('_', ' '),
                          status: areas[key].atende === 'SIM' ? 'OK' : 'ALERTA',
                          content: areas[key].veredito_especialista || 'Área sem pendências.',
                          pendencias: areas[key].pendencias || [],
                          justificativa: areas[key].justificativa || ''
                      }));
                      setBlocosTecnicos(mapBlocks);
                      setShowFullReport(true);
                    }
                  }
                }
              }
            })
            .catch(err => console.error("Erro ao buscar análises:", err));
        }
      })
      .catch(err => console.error("Erro ao buscar empresa:", err));
  }, [id, navigate]);

  const iniciarAnalise = async () => {
    if (!empresa) {
      alert("Você precisa cadastrar sua empresa no Perfil antes de solicitar um diagnóstico.");
      navigate('/perfil');
      return;
    }

    console.log("Iniciando nova análise de Match...");
    setIsAnalisando(true);
    setVereditoText('');
    setShowFullReport(false);
    setBlocosTecnicos([]);

    try {
      let currentAnaliseId = analiseId;

      if (!currentAnaliseId) {
        console.log("Criando nova ponte de análise no backend...");
        const resCreate = await fetch(`/api/v1/analises/iniciar?licitacaoId=${id}&empresaId=${empresa.id}`, { method: 'POST' });
        if (!resCreate.ok) throw new Error("Falha ao criar análise");
        const newAnalise = await resCreate.json();
        currentAnaliseId = newAnalise.id;
        setAnaliseId(newAnalise.id);
      }

      console.log("Abrindo stream de diagnóstico para ID:", currentAnaliseId);
      const response = await fetch(`/api/v1/analises/${currentAnaliseId}/diagnostico/stream`);
      
      if (!response.body) throw new Error("ReadableStream not supported");
      
      const reader = response.body.getReader();
      const decoder = new TextDecoder("utf-8");
      let partialChunk = '';
      
      while (true) {
        const { value, done } = await reader.read();
        if (done) break;
        
        const text = decoder.decode(value, { stream: true });
        const lines = (partialChunk + text).split('\n');
        partialChunk = lines.pop() || '';

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.replace('data:', '');
            // Mantemos o texto EXATAMENTE como veio, preservando espaços e quebras de linha
            if (data.includes('[STATUS:ANALISANDO_BLOCOS]')) {
              setVereditoText("Consultando cláusulas do edital e comparando com seu perfil...\n");
            } else if (data.includes('[STATUS:GERANDO_VEREDITO]')) {
              setVereditoText(""); 
            } else if (data) {
              setVereditoText((prev) => prev + data);
            }
          } else if (line.length > 0) {
            setVereditoText((prev) => prev + line);
          }
        }
      }
      
      console.log("Stream concluído. Buscando JSON final...");
      const res = await fetch(`/api/v1/analises/${currentAnaliseId}`);
      const updated = await res.json();
      
      if (updated && updated.diagnosticoJson && updated.diagnosticoJson.detalhes_por_area) {
         const areas = updated.diagnosticoJson.detalhes_por_area;
         const mapBlocks = Object.keys(areas).map(key => ({
            id: key,
            title: key.toUpperCase().replace('_', ' '),
            status: areas[key].atende === 'SIM' ? 'OK' : 'ALERTA',
            content: areas[key].veredito_especialista || 'Área sem pendências.',
            pendencias: areas[key].pendencias || [],
            justificativa: areas[key].justificativa || ''
         }));
         setBlocosTecnicos(mapBlocks);
         console.log("Relatório técnico carregado.");
      }
      
      setIsAnalisando(false);
      setShowFullReport(true);
      
    } catch (error) {
      console.error("Falha no processo de análise:", error);
      setVereditoText("Erro ao conectar com a Inteligência Artificial. Verifique se o backend está ativo.");
      setIsAnalisando(false);
    }
  };

  if (!licitacao) {
    return (
      <div className="flex flex-col items-center justify-center h-screen w-full text-primary-600 bg-gray-50 dark:bg-slate-900 transition-colors">
        <Loader2 className="w-12 h-12 animate-spin mb-4" />
        <p className="text-xl font-bold animate-pulse">Carregando detalhes do edital...</p>
      </div>
    );
  }

  return (
    <div className="h-screen flex flex-col transition-colors duration-300">
      <header className="p-6 bg-white dark:bg-slate-900 border-b border-gray-100 dark:border-slate-800 flex items-center justify-between shadow-sm z-10">
        <div className="flex items-center gap-4">
          <button 
            onClick={() => navigate(-1)}
            className="p-2 hover:bg-gray-100 dark:hover:bg-slate-800 rounded-full transition-colors"
          >
            <ArrowLeft className="w-6 h-6 text-gray-500 dark:text-slate-400" />
          </button>
          <div>
            <h2 className="text-xl font-bold line-clamp-1 max-w-xl text-teal-950 dark:text-teal-50">
              {licitacao.titulo || licitacao.nomeArquivo || 'Detalhes da Licitação'}
            </h2>
            <div className="flex items-center gap-4 mt-1">
              <p className="text-xs font-bold text-gray-500 dark:text-slate-400">
                {empresa ? `🏢 ${empresa.razaoSocial}` : '⚠️ Perfil incompleto'}
              </p>
              <div className="flex bg-gray-100 dark:bg-slate-800 p-1 rounded-lg">
                <button 
                  onClick={() => setViewMode('pdf')}
                  className={`px-3 py-1 text-[10px] font-black uppercase rounded-md transition-all ${viewMode === 'pdf' ? 'bg-white dark:bg-slate-700 text-primary-600 shadow-sm' : 'text-gray-400'}`}
                >
                  PDF Original
                </button>
                <button 
                  onClick={() => setViewMode('data')}
                  className={`px-3 py-1 text-[10px] font-black uppercase rounded-md transition-all ${viewMode === 'data' ? 'bg-white dark:bg-slate-700 text-primary-600 shadow-sm' : 'text-gray-400'}`}
                >
                  Dados Extraídos
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <div className="flex items-center gap-3">
          <button 
            onClick={async () => {
              if(confirm('Deseja apagar este edital permanentemente?')) {
                await fetch(`/api/v1/licitacoes/${id}`, { method: 'DELETE' });
                navigate('/dashboard');
              }
            }}
            className="p-3 text-red-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-xl transition-all"
            title="Apagar Edital"
          >
            <AlertTriangle className="w-5 h-5" />
          </button>

          {!isAnalisando && (
            <button 
              onClick={iniciarAnalise}
              className={`px-6 py-3 font-bold rounded-xl shadow-lg transition-all flex items-center gap-2 ${
                empresa 
                ? 'bg-primary-500 text-white shadow-primary-500/20 hover:bg-primary-600' 
                : 'bg-gray-200 dark:bg-slate-800 text-gray-500 cursor-not-allowed shadow-none'
              }`}
            >
              <BrainCircuit className="w-5 h-5" />
              {vereditoText ? 'Refazer Diagnóstico' : 'Gerar Diagnóstico IA'}
            </button>
          )}
        </div>
      </header>

      <div className="flex-1 flex overflow-hidden p-6 gap-6">
        {/* Lado Esquerdo: PDF ou Dados Estruturados */}
        <div className="w-[55%] flex flex-col bg-white dark:bg-slate-800 rounded-3xl border border-gray-100 dark:border-slate-700 shadow-sm overflow-hidden">
          {viewMode === 'pdf' ? (
            <div className="flex-1 relative">
              <iframe 
                src={`/api/v1/licitacoes/${id}/pdf#toolbar=0`} 
                className="w-full h-full border-none"
                title="Visualizador de PDF"
              />
              <div className="absolute top-4 right-4 p-2 bg-white/80 dark:bg-slate-800/80 backdrop-blur rounded-lg border border-gray-100 dark:border-slate-700 text-[10px] font-black text-primary-600 uppercase">
                Visualização do Edital Original
              </div>
            </div>
          ) : (
            <div className="flex-1 overflow-y-auto p-8 custom-scrollbar">
              <div className="flex items-center gap-2 mb-8 text-gray-400 dark:text-slate-500 font-black uppercase text-[10px] tracking-[0.2em]">
                <FileText className="w-4 h-4" />
                Estrutura de Dados do Edital
              </div>
              
              {licitacao.masterJson ? (
                <div className="space-y-12">
                  <section>
                    <h3 className="text-xl font-black text-teal-950 dark:text-teal-50 mb-6 flex items-center gap-3">
                      <span className="w-1.5 h-6 bg-primary-500 rounded-full"></span>
                      Identificação do Projeto
                    </h3>
                    <div className="grid grid-cols-2 gap-8 text-sm">
                      <div><p className="text-gray-400 dark:text-slate-500 font-black uppercase text-[10px] mb-1">Número do Edital</p><p className="font-bold">{licitacao.masterJson?.identificacao_projeto?.numero_edital || 'N/A'}</p></div>
                      <div><p className="text-gray-400 dark:text-slate-500 font-black uppercase text-[10px] mb-1">Órgão Emissor</p><p className="font-bold">{licitacao.masterJson?.identificacao_projeto?.orgao_emissor || 'N/A'}</p></div>
                      <div className="col-span-2 bg-primary-50/50 dark:bg-primary-900/10 p-5 rounded-2xl border border-primary-100/50 dark:border-primary-800/20">
                        <p className="text-primary-600 dark:text-primary-400 font-black uppercase text-[10px] mb-2 tracking-widest">Objeto Completo</p>
                        <p className="font-medium text-gray-600 dark:text-slate-300 leading-relaxed text-base italic">"{licitacao.masterJson?.identificacao_projeto?.objeto_completo || licitacao.objeto || 'Não informado'}"</p>
                      </div>
                    </div>
                  </section>

                  <section>
                    <h3 className="text-xl font-black text-teal-950 dark:text-teal-50 mb-6 flex items-center gap-3">
                      <span className="w-1.5 h-6 bg-primary-400 rounded-full"></span>
                      Habilitação Requerida
                    </h3>
                    <div className="space-y-4">
                      {Array.isArray(licitacao.masterJson?.habilitacao_detalhada) && licitacao.masterJson.habilitacao_detalhada.length > 0 ? (
                        licitacao.masterJson.habilitacao_detalhada.map((h: any, i: number) => (
                          <div key={i} className="p-5 bg-gray-50 dark:bg-slate-900/50 rounded-2xl border border-gray-100 dark:border-slate-700 flex gap-4 items-start hover:border-primary-200 dark:hover:border-primary-800 transition-colors group">
                            <div className="mt-1 w-6 h-6 rounded-lg bg-white dark:bg-slate-800 text-primary-500 flex items-center justify-center text-xs font-black shadow-sm group-hover:scale-110 transition-transform">
                              {i+1}
                            </div>
                            <div>
                              <p className="font-bold text-gray-800 dark:text-slate-200">{h.nome_documento || 'Documento'}</p>
                              <p className="text-sm text-gray-500 dark:text-slate-400 mt-1 leading-relaxed">{h.descricao_exigencia}</p>
                            </div>
                          </div>
                        ))
                      ) : (
                        <p className="text-sm text-gray-400 italic">Nenhum dado de habilitação extraído.</p>
                      )}
                    </div>
                  </section>

                  <section>
                    <h3 className="text-xl font-black text-teal-950 dark:text-teal-50 mb-6 flex items-center gap-3">
                      <span className="w-1.5 h-6 bg-primary-600 rounded-full"></span>
                      Prazos e Valores
                    </h3>
                    <div className="grid grid-cols-2 gap-6 text-sm">
                      <div className="p-6 bg-gradient-to-br from-primary-50 to-white dark:from-primary-900/20 dark:to-slate-800 rounded-3xl border border-primary-100 dark:border-primary-800 shadow-sm group">
                        <p className="text-primary-700 dark:text-primary-300 font-black uppercase text-[10px] mb-1 tracking-wider">Valor Estimado</p>
                        <p className="text-3xl font-black text-primary-600 dark:text-primary-400 group-hover:scale-[1.02] transition-transform">
                          {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(licitacao.masterJson?.prazos_valores_e_pagamento?.valor_estimado_total || 0)}
                        </p>
                      </div>
                      <div className="p-6 bg-gradient-to-br from-cyan-50 to-white dark:from-cyan-900/20 dark:to-slate-800 rounded-3xl border border-cyan-100 dark:border-cyan-800 shadow-sm">
                        <p className="text-cyan-700 dark:text-cyan-300 font-black uppercase text-[10px] mb-1 tracking-wider">Prazo Global</p>
                        <p className="text-3xl font-black text-cyan-600 dark:text-cyan-400">
                          {licitacao.masterJson?.prazos_valores_e_pagamento?.prazo_execucao_global_dias || '?'} <span className="text-lg">dias</span>
                        </p>
                      </div>
                    </div>
                  </section>
                </div>
              ) : (
                <div className="flex flex-col items-center justify-center h-full text-gray-400 animate-pulse">
                  <Loader2 className="w-12 h-12 animate-spin mb-4" />
                  <p className="font-bold text-lg">Processando Master JSON...</p>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Lado Direito: Diagnóstico IA */}
        <div className="w-[45%] flex flex-col gap-6 overflow-y-auto pr-2 custom-scrollbar min-w-[400px]">
          
          {/* Veredito Geral (Streaming) */}
          <div className="bg-white dark:bg-slate-800 rounded-3xl border border-gray-200 dark:border-slate-700 shadow-lg p-8 relative overflow-hidden flex flex-col w-full min-h-[350px] max-h-[600px]">
            <div className="flex items-center gap-2 mb-8 text-primary-600 dark:text-primary-400 font-black bg-primary-50 dark:bg-primary-900/30 w-fit px-4 py-1.5 rounded-full text-[10px] tracking-widest uppercase shrink-0">
              <BrainCircuit className="w-4 h-4" />
              <span>Diagnóstico de Viabilidade</span>
            </div>
            
            <div className="w-full text-gray-700 dark:text-slate-200 leading-relaxed text-lg italic whitespace-pre-wrap break-words font-medium overflow-y-auto custom-scrollbar pr-2 flex-1">
              {vereditoText === '' && !isAnalisando ? (
                <div className="flex flex-col items-center justify-center py-12 text-center space-y-6">
                  <div className="w-20 h-20 bg-gray-50 dark:bg-slate-900 rounded-full flex items-center justify-center border-4 border-white dark:border-slate-800 shadow-inner">
                    <BrainCircuit className="w-10 h-10 text-gray-200 dark:text-slate-700" />
                  </div>
                  <p className="text-gray-400 dark:text-slate-500 text-sm max-w-[280px] leading-relaxed">
                    Nossa IA cruzará os dados deste edital com o capital e CNAEs da sua empresa.
                  </p>
                </div>
              ) : (
                <div className="block w-full animate-fade-in">
                  {vereditoText}
                  {isAnalisando && <span className="inline-block w-2 h-5 bg-primary-500 ml-1 animate-pulse align-middle rounded-full" />}
                </div>
              )}
            </div>

            {isAnalisando && (
              <div className="mt-6 pt-6 flex items-center gap-3 text-[10px] text-primary-500 font-black uppercase tracking-widest border-t border-gray-50 dark:border-slate-700 shrink-0">
                <Loader2 className="w-4 h-4 animate-spin" />
                <span>Analista Virtual em ação...</span>
              </div>
            )}
          </div>

          {/* Blocos Técnicos */}
          <AnimatePresence>
            {showFullReport && blocosTecnicos.length > 0 && (
              <motion.div 
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                className="space-y-4 pb-8"
              >
                <div className="flex items-center justify-between ml-2">
                   <h4 className="text-[10px] font-black text-gray-400 dark:text-slate-500 uppercase tracking-[0.2em]">Parecer Técnico Detalhado</h4>
                </div>
                {blocosTecnicos.map((bloco) => (
                  <div key={bloco.id} className={`bg-white dark:bg-slate-800 rounded-2xl border ${bloco.status === 'OK' ? 'border-green-100 dark:border-green-900/30' : 'border-amber-100 dark:border-amber-900/30'} shadow-sm overflow-hidden transition-all group`}>
                    <button 
                      onClick={() => setOpenSection(openSection === bloco.id ? null : bloco.id)}
                      className={`w-full px-6 py-5 flex items-center justify-between hover:bg-gray-50 dark:hover:bg-slate-700/50 transition-colors text-left ${openSection === bloco.id ? 'bg-gray-50/50 dark:bg-slate-700/30' : ''}`}
                    >
                      <div className="flex items-center gap-4">
                        {bloco.status === 'OK' ? (
                          <div className="p-2 bg-green-50 dark:bg-green-900/20 rounded-xl group-hover:scale-110 transition-transform"><CheckCircle2 className="w-5 h-5 text-green-500" /></div>
                        ) : (
                          <div className="p-2 bg-amber-50 dark:bg-amber-900/20 rounded-xl group-hover:scale-110 transition-transform"><AlertTriangle className="w-5 h-5 text-amber-500" /></div>
                        )}
                        <span className="font-bold text-gray-700 dark:text-slate-200">{bloco.title}</span>
                      </div>
                      <div className="flex items-center gap-3">
                         <span className={`text-[10px] font-black px-2 py-0.5 rounded-md ${bloco.status === 'OK' ? 'bg-green-100 dark:bg-green-900/40 text-green-700 dark:text-green-400' : 'bg-amber-100 dark:bg-amber-900/40 text-amber-700 dark:text-amber-400'}`}>
                           {bloco.status === 'OK' ? 'APTO' : 'PENDENTE'}
                         </span>
                         {openSection === bloco.id ? <ChevronUp className="w-4 h-4 text-gray-400" /> : <ChevronDown className="w-4 h-4 text-gray-400" />}
                      </div>
                    </button>
                    {openSection === bloco.id && (
                      <div className="px-6 pb-6 text-sm space-y-5 animate-fade-in">
                        <div className="pt-2">
                           <p className="text-[10px] font-black text-gray-400 dark:text-slate-500 uppercase mb-2 tracking-widest">Resumo do Especialista</p>
                           <p className="text-gray-600 dark:text-slate-300 leading-relaxed bg-gray-50 dark:bg-slate-900/50 p-4 rounded-xl border border-gray-100 dark:border-slate-700 font-medium">{bloco.content}</p>
                        </div>
                        
                        {bloco.pendencias && bloco.pendencias.length > 0 && (
                          <div className="p-4 bg-red-50 dark:bg-red-900/10 rounded-xl border border-red-100 dark:border-red-900/20">
                             <p className="text-[10px] font-black text-red-500 dark:text-red-400 uppercase mb-3 tracking-widest flex items-center gap-1.5">
                               <AlertTriangle className="w-3 h-3" /> Pendências a Corrigir
                             </p>
                             <ul className="space-y-2">
                               {bloco.pendencias.map((p:string, idx:number) => (
                                 <li key={idx} className="flex gap-2 text-red-700 dark:text-red-300 font-bold text-xs bg-white dark:bg-slate-800 p-2.5 rounded-lg shadow-sm border border-red-50 dark:border-red-900/30">
                                   <span className="text-red-400">•</span> {p}
                                 </li>
                               ))}
                             </ul>
                          </div>
                        )}

                        {bloco.justificativa && (
                           <div className="border-t border-gray-50 dark:border-slate-700 pt-4">
                              <p className="text-[10px] font-black text-gray-400 dark:text-slate-500 uppercase mb-2 tracking-widest italic">Base Legal / Justificativa</p>
                              <p className="text-xs text-gray-500 dark:text-slate-400 italic leading-relaxed">{bloco.justificativa}</p>
                           </div>
                        )}
                      </div>
                    )}
                  </div>
                ))}
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </div>
    </div>
  );
};

export default LicitacaoDetalhes;
