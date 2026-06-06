import { useState, useEffect } from 'react';
import { FileText, Calendar, Building2, ChevronRight, Loader2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
  const navigate = useNavigate();
  const [licitacoes, setLicitacoes] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [hasEmpresa, setHasEmpresa] = useState<boolean | null>(null);

  useEffect(() => {
    // 1. Verifica se existe empresa cadastrada
    fetch('/api/v1/empresas')
      .then(res => res.json())
      .then(data => {
        const existe = data && data.length > 0;
        setHasEmpresa(existe);
        
        // 2. Só carrega as licitações se houver empresa
        if (existe) {
          return fetch('/api/v1/licitacoes');
        } else {
          setIsLoading(false);
          return null;
        }
      })
      .then(res => {
        if (!res) return;
        if (!res.ok) throw new Error('Falha ao carregar licitações do backend.');
        return res.json();
      })
      .then(data => {
        if (Array.isArray(data)) {
          setLicitacoes(data);
        }
        setIsLoading(false);
      })
      .catch(err => {
        setError(err.message);
        setIsLoading(false);
      });
  }, []);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-[60vh]">
        <Loader2 className="w-10 h-10 animate-spin text-primary-600" />
      </div>
    );
  }

  if (hasEmpresa === false && licitacoes.length === 0) {
    return (
      <div className="max-w-2xl mx-auto mt-20 text-center space-y-8 bg-white dark:bg-slate-800 p-16 rounded-[2.5rem] border border-gray-100 dark:border-slate-700 shadow-2xl shadow-primary-500/5">
        <div className="w-24 h-24 bg-primary-50 dark:bg-primary-900/20 text-primary-600 dark:text-primary-400 rounded-3xl flex items-center justify-center mx-auto mb-8 shadow-inner rotate-3 hover:rotate-0 transition-transform">
          <Building2 className="w-12 h-12" />
        </div>
        <h2 className="text-4xl font-black text-teal-950 dark:text-teal-50 tracking-tight">Vamos configurar sua empresa</h2>
        <p className="text-gray-500 dark:text-slate-400 text-xl leading-relaxed">
          Para garantir diagnósticos de match precisos, o LicyPilot precisa conhecer o seu capital social e CNAEs.
        </p>
        <button 
          onClick={() => navigate('/perfil')}
          className="px-10 py-5 bg-primary-500 text-white font-black text-lg rounded-2xl shadow-xl shadow-primary-500/20 hover:bg-primary-600 transition-all flex items-center gap-3 mx-auto"
        >
          Configurar Perfil Agora
          <ChevronRight className="w-6 h-6" />
        </button>
      </div>
    );
  }

  return (
    <div className="w-full p-8 transition-colors duration-300">
      <header className="mb-12">
        <h2 className="text-4xl font-black text-teal-950 dark:text-teal-50 tracking-tight">Editais Disponíveis</h2>
        <p className="text-gray-600 dark:text-slate-300 mt-2 text-lg font-medium">Explore as oportunidades e peça um diagnóstico personalizado por IA.</p>
      </header>

      {error ? (
        <div className="p-6 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 rounded-2xl border border-red-100 dark:border-red-900/30 font-bold">
          {error} (O backend Java está rodando?)
        </div>
      ) : licitacoes.length === 0 ? (
        <div className="p-20 bg-white dark:bg-slate-800 text-gray-400 rounded-[3rem] border-2 border-dashed border-gray-100 dark:border-slate-700 text-center shadow-inner">
          <FileText className="w-20 h-20 mx-auto mb-8 opacity-20 text-primary-500" />
          <p className="font-black text-2xl text-gray-700 dark:text-slate-300">Nenhum edital ativo no momento.</p>
          <p className="text-lg mt-3 text-gray-500 dark:text-slate-500">Acesse a Área Admin para importar novos editais em PDF.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-10">
          {licitacoes.map((licitacao) => {
            if (!licitacao) return null;
            const status = licitacao.statusProcessamento || 'PENDENTE';
            
            return (
              <div 
                key={licitacao.id}
                className="bg-white dark:bg-slate-800 rounded-[2.5rem] border border-gray-100 dark:border-slate-700 shadow-sm hover:shadow-2xl hover:shadow-primary-500/10 transition-all p-10 flex flex-col h-full group hover:-translate-y-2"
              >
                <div className="flex justify-between items-start mb-8">
                  <div className="p-5 bg-primary-50 dark:bg-primary-900/20 rounded-2xl text-primary-600 dark:text-primary-400 group-hover:scale-110 transition-transform shadow-sm">
                    <FileText className="w-7 h-7" />
                  </div>
                  <span className={`text-[10px] font-black tracking-[0.2em] uppercase px-4 py-1.5 rounded-full ${
                    status === 'CONCLUIDO' ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400' :
                    status === 'PROCESSANDO' ? 'bg-primary-100 dark:bg-primary-900/30 text-primary-700 dark:text-primary-400' :
                    'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400'
                  }`}>
                    {status === 'CONCLUIDO' ? 'EDITAL PRONTO' : status}
                  </span>
                </div>

                <h3 className="text-2xl font-bold text-teal-950 dark:text-teal-50 mb-6 line-clamp-2 leading-tight group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors">
                  {licitacao?.titulo || licitacao?.nomeArquivo || 'Edital Sem Nome'}
                </h3>

                <div className="space-y-4 mt-auto pt-8 border-t border-gray-50 dark:border-slate-700/50">
                  <div className="flex items-center gap-3 text-sm text-gray-500 dark:text-slate-400 font-bold">
                    <div className="p-1.5 bg-gray-50 dark:bg-slate-900 rounded-lg"><Building2 className="w-4 h-4 text-primary-500" /></div>
                    <span className="truncate">{licitacao?.orgaoEmissor || 'Órgão não identificado'}</span>
                  </div>
                  <div className="flex items-center gap-3 text-sm text-gray-500 dark:text-slate-400 font-bold">
                    <div className="p-1.5 bg-gray-50 dark:bg-slate-900 rounded-lg"><Calendar className="w-4 h-4 text-primary-500" /></div>
                    <span>{new Date().toLocaleDateString()}</span>
                  </div>
                </div>

                <button 
                  disabled={status !== 'CONCLUIDO'}
                  onClick={() => navigate(`/licitacao/${licitacao.id}`)}
                  className={`mt-10 w-full py-5 font-black text-xs uppercase tracking-[0.2em] rounded-2xl transition-all flex items-center justify-center gap-3 group/btn ${
                    status === 'CONCLUIDO' 
                    ? 'bg-primary-500 hover:bg-primary-600 text-white shadow-xl shadow-primary-500/20' 
                    : 'bg-gray-100 dark:bg-slate-700 text-gray-400 cursor-not-allowed shadow-none'
                  }`}
                >
                  {status === 'CONCLUIDO' ? 'Analisar Match IA' : 'Processando...'}
                  {status === 'CONCLUIDO' && <ChevronRight className="w-5 h-5 group-hover/btn:translate-x-1 transition-transform" />}
                </button>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default Dashboard;
