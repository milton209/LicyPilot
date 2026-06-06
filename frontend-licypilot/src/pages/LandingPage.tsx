import { useNavigate } from 'react-router-dom';
import { BrainCircuit, ShieldCheck, Zap, ArrowRight } from 'lucide-react';

const LandingPage = () => {
  const navigate = useNavigate();

  return (
    <div className="bg-slate-900 min-h-screen text-white overflow-hidden">
      {/* Hero Section */}
      <section className="relative pt-20 pb-32 px-6">
        <div className="max-w-6xl mx-auto text-center relative z-10">
          <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-blue-500/10 border border-blue-500/20 text-blue-400 text-sm font-bold mb-8 animate-bounce">
            <BrainCircuit className="w-4 h-4" />
            IA Especialista em Licitações
          </div>
          <h1 className="text-5xl md:text-7xl font-extrabold mb-8 bg-gradient-to-r from-white via-blue-100 to-blue-400 bg-clip-text text-transparent">
            Licitações decodificadas pela Inteligência Artificial
          </h1>
          <p className="text-xl text-slate-400 max-w-3xl mx-auto mb-12 leading-relaxed">
            O LicyPilot transforma editais complexos em diagnósticos de viabilidade em segundos. 
            Cruze os dados da sua empresa com as exigências do governo e pare de perder tempo com editais furados.
          </p>
          <div className="flex flex-col md:flex-row items-center justify-center gap-6">
            <button 
              onClick={() => navigate('/dashboard')}
              className="px-8 py-4 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-2xl shadow-xl shadow-blue-500/20 transition-all flex items-center gap-2 text-lg group"
            >
              Acessar Oportunidades
              <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </button>
            <button className="px-8 py-4 bg-slate-800 hover:bg-slate-700 text-white font-bold rounded-2xl transition-all border border-slate-700">
              Ver Demonstração
            </button>
          </div>
        </div>
        
        {/* Background Glows */}
        <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-blue-600/20 rounded-full blur-[120px] -z-10" />
        <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-indigo-600/20 rounded-full blur-[120px] -z-10" />
      </section>

      {/* Features */}
      <section className="py-24 bg-slate-800/50 border-y border-slate-800 px-6">
        <div className="max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-12">
          <div className="space-y-4">
            <div className="w-12 h-12 bg-blue-500/20 rounded-xl flex items-center justify-center text-blue-400">
              <Zap className="w-6 h-6" />
            </div>
            <h3 className="text-xl font-bold">Extração Precisa</h3>
            <p className="text-slate-400">Leitura profunda de PDFs, inclusive escaneados, transformando o "juridiquês" em dados estruturados.</p>
          </div>
          <div className="space-y-4">
            <div className="w-12 h-12 bg-indigo-500/20 rounded-xl flex items-center justify-center text-indigo-400">
              <BrainCircuit className="w-6 h-6" />
            </div>
            <h3 className="text-xl font-bold">Cruzamento de Dados</h3>
            <p className="text-slate-400">Análise automática entre o capital social, CNAEs e histórico da sua empresa contra as regras do edital.</p>
          </div>
          <div className="space-y-4">
            <div className="w-12 h-12 bg-emerald-500/20 rounded-xl flex items-center justify-center text-emerald-400">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <h3 className="text-xl font-bold">Análise de Risco</h3>
            <p className="text-slate-400">Alertas em tempo real sobre multas abusivas, prazos impossíveis e cláusulas restritivas.</p>
          </div>
        </div>
      </section>

      {/* Footer Minimal */}
      <footer className="py-12 text-center text-slate-500 text-sm">
        <p>© 2026 LicyPilot. Todos os direitos reservados.</p>
      </footer>
    </div>
  );
};

export default LandingPage;
