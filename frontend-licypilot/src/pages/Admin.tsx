import { useState } from 'react';
import { Upload, FilePlus, AlertCircle, CheckCircle2, Trash2, RefreshCcw, Loader2 } from 'lucide-react';

const Admin = () => {
  const [titulo, setTitulo] = useState('');
  const [orgao, setOrgao] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);
  const [isResetting, setIsResetting] = useState(false);

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!titulo || !file) return;

    setIsUploading(true);
    
    const formData = new FormData();
    formData.append('arquivo', file);
    formData.append('titulo', titulo);
    formData.append('orgao', orgao);
    formData.append('maxPages', '20'); // Limite para o protótipo aumentado para 20

    try {
      const response = await fetch('/api/v1/licitacoes/importar', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) throw new Error('Falha no upload para o backend Java.');

      setIsUploading(false);
      setShowSuccess(true);
      setTitulo('');
      setOrgao('');
      setFile(null);
      setTimeout(() => setShowSuccess(false), 3000);
    } catch (error) {
      console.error(error);
      alert('Erro ao enviar edital para o backend Java.');
      setIsUploading(false);
    }
  };

  const resetarSistema = async (tipo: 'licitacoes' | 'analises' | 'completo') => {
    let confirmMsg = '';
    if (tipo === 'analises') confirmMsg = 'Apagar todos os diagnósticos IA? (Mantém os editais)';
    if (tipo === 'licitacoes') confirmMsg = 'Apagar editais e diagnósticos? (Mantém sua EMPRESA e PERFIL)';
    if (tipo === 'completo') confirmMsg = 'ATENÇÃO: Apagar TUDO? (Empresas, Usuários e Editais). O sistema voltará ao zero.';

    if (!confirm(confirmMsg)) return;
    
    setIsResetting(true);
    let endpoint = '';
    if (tipo === 'analises') endpoint = '/api/v1/analises/reset';
    if (tipo === 'licitacoes') endpoint = '/api/v1/licitacoes/reset';
    if (tipo === 'completo') endpoint = '/api/v1/empresas/reset';
    
    try {
      const response = await fetch(endpoint, { method: 'DELETE' });
      if (response.ok) {
        alert(`${tipo.charAt(0).toUpperCase() + tipo.slice(1)} resetadas com sucesso!`);
        if (tipo === 'completo') window.location.href = '/';
      }
    } catch (error) {
      alert('Erro ao resetar dados.');
    } finally {
      setIsResetting(false);
    }
  };

  return (
    <div className="w-full p-8 space-y-10 transition-colors duration-300">
      <header>
        <h2 className="text-4xl font-black text-teal-950 dark:text-teal-50 tracking-tight">Painel do Administrador</h2>
        <p className="text-gray-500 dark:text-slate-400 mt-2 text-lg font-medium">Gerencie editais e controle o ambiente de demonstração.</p>
      </header>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-10">
        {/* Lado Esquerdo: Upload */}
        <div className="bg-white dark:bg-slate-800 rounded-[2.5rem] border border-gray-100 dark:border-slate-700 shadow-sm p-10">
          <h3 className="text-xl font-bold text-teal-950 dark:text-teal-50 mb-8 flex items-center gap-3">
            <div className="p-2 bg-primary-50 dark:bg-primary-900/20 rounded-xl text-primary-600 dark:text-primary-400">
              <Upload className="w-6 h-6" />
            </div>
            Importar Novo Edital
          </h3>

          <form onSubmit={handleUpload} className="space-y-8">
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-black text-gray-400 dark:text-slate-500 uppercase tracking-widest mb-2 ml-1">Título do Edital</label>
                <input 
                  type="text" 
                  required
                  value={titulo}
                  onChange={(e) => setTitulo(e.target.value)}
                  placeholder="Ex: Reforma da Escola Municipal"
                  className="w-full px-6 py-4 rounded-2xl bg-gray-50 dark:bg-slate-900 border border-gray-100 dark:border-slate-700 focus:ring-4 focus:ring-primary-500/10 focus:border-primary-500 outline-none transition-all font-medium"
                />
              </div>
              <div>
                <label className="block text-sm font-black text-gray-400 dark:text-slate-500 uppercase tracking-widest mb-2 ml-1">Órgão Emissor</label>
                <input 
                  type="text" 
                  required
                  value={orgao}
                  onChange={(e) => setOrgao(e.target.value)}
                  placeholder="Ex: Prefeitura de São Paulo"
                  className="w-full px-6 py-4 rounded-2xl bg-gray-50 dark:bg-slate-900 border border-gray-100 dark:border-slate-700 focus:ring-4 focus:ring-primary-500/10 focus:border-primary-500 outline-none transition-all font-medium"
                />
              </div>
            </div>

            <div className="relative group">
              <input 
                type="file" 
                accept=".pdf"
                onChange={(e) => setFile(e.target.files?.[0] || null)}
                className="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10"
              />
              <div className={`p-12 border-2 border-dashed rounded-[2rem] flex flex-col items-center justify-center transition-all ${
                file ? 'border-primary-500 bg-primary-50/50 dark:bg-primary-900/10' : 'border-gray-200 dark:border-slate-700 group-hover:border-primary-400 bg-gray-50 dark:bg-slate-900/50'
              }`}>
                {file ? (
                  <>
                    <div className="w-16 h-16 bg-white dark:bg-slate-800 rounded-2xl flex items-center justify-center shadow-lg mb-4 text-primary-500">
                      <FilePlus className="w-8 h-8" />
                    </div>
                    <span className="text-primary-700 dark:text-primary-300 font-bold text-center break-all px-4">{file.name}</span>
                  </>
                ) : (
                  <>
                    <div className="w-16 h-16 bg-white dark:bg-slate-800 rounded-2xl flex items-center justify-center shadow-sm mb-4 text-gray-300 dark:text-slate-700">
                      <Upload className="w-8 h-8" />
                    </div>
                    <span className="text-gray-500 dark:text-slate-400 font-bold">Arraste o PDF do edital aqui</span>
                    <span className="text-gray-400 dark:text-slate-600 text-xs mt-2 uppercase tracking-tighter">PDF até 20 páginas no demo</span>
                  </>
                )}
              </div>
            </div>

            <button 
              type="submit"
              disabled={!file || isUploading}
              className={`w-full py-5 font-black text-xs uppercase tracking-[0.2em] rounded-2xl shadow-xl transition-all flex items-center justify-center gap-3 ${
                !file || isUploading 
                ? 'bg-gray-100 dark:bg-slate-700 text-gray-400 cursor-not-allowed' 
                : 'bg-primary-500 text-white shadow-primary-500/20 hover:bg-primary-600'
              }`}
            >
              {isUploading ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Processando Extração...
                </>
              ) : (
                <>
                  <FilePlus className="w-5 h-5" />
                  Cadastrar e Analisar
                </>
              )}
            </button>
          </form>

          {showSuccess && (
            <div className="mt-6 p-5 bg-green-50 dark:bg-green-900/20 text-green-700 dark:text-green-400 rounded-2xl border border-green-100 dark:border-green-900/30 flex items-center gap-3 animate-fade-in font-bold">
              <CheckCircle2 className="w-6 h-6" />
              Edital importado e estruturado com sucesso!
            </div>
          )}
        </div>

        {/* Lado Direito: Demo Tools */}
        <div className="bg-white dark:bg-slate-800 rounded-[2.5rem] border border-gray-100 dark:border-slate-700 shadow-sm p-10 flex flex-col">
          <h3 className="text-xl font-bold text-teal-950 dark:text-teal-50 mb-8 flex items-center gap-3">
            <div className="p-2 bg-amber-50 dark:bg-amber-900/20 rounded-xl text-amber-600 dark:text-amber-400">
              <RefreshCcw className="w-6 h-6" />
            </div>
            Gestão do Ambiente Demo
          </h3>
          
          <div className="space-y-4 flex-1">
            <div className="p-6 border border-amber-100 dark:border-amber-900/20 bg-amber-50/30 dark:bg-amber-900/10 rounded-[2rem] group hover:border-amber-300 transition-colors">
              <div className="flex items-center gap-3 mb-2">
                <Trash2 className="w-4 h-4 text-amber-600" />
                <h4 className="font-black text-amber-800 dark:text-amber-500 uppercase text-[10px] tracking-widest">Limpar Análises</h4>
              </div>
              <p className="text-[11px] text-amber-700 dark:text-amber-600/80 mb-4 font-medium leading-relaxed">
                Apaga apenas os resultados da IA.
              </p>
              <button 
                onClick={() => resetarSistema('analises')}
                disabled={isResetting}
                className="w-full py-3 bg-amber-600 text-white text-[9px] font-black uppercase tracking-[0.2em] rounded-xl hover:bg-amber-700 transition-all flex items-center justify-center gap-2"
              >
                Resetar Diagnósticos
              </button>
            </div>

            <div className="p-6 border border-orange-100 dark:border-orange-900/20 bg-orange-50/30 dark:bg-orange-900/10 rounded-[2rem] group hover:border-orange-300 transition-colors">
              <div className="flex items-center gap-3 mb-2">
                <Trash2 className="w-4 h-4 text-orange-600" />
                <h4 className="font-black text-orange-800 dark:text-orange-500 uppercase text-[10px] tracking-widest">Resetar Editais</h4>
              </div>
              <p className="text-[11px] text-orange-700 dark:text-orange-600/80 mb-4 font-medium leading-relaxed">
                Apaga editais e análises (Mantém seu Perfil).
              </p>
              <button 
                onClick={() => resetarSistema('licitacoes')}
                disabled={isResetting}
                className="w-full py-3 bg-orange-600 text-white text-[9px] font-black uppercase tracking-[0.2em] rounded-xl hover:bg-orange-700 transition-all flex items-center justify-center gap-2"
              >
                Limpar Editais e Matchs
              </button>
            </div>

            <div className="p-6 border border-red-100 dark:border-red-900/20 bg-red-50/30 dark:bg-red-900/10 rounded-[2rem] group hover:border-red-300 transition-colors">
              <div className="flex items-center gap-3 mb-2">
                <Trash2 className="w-4 h-4 text-red-600" />
                <h4 className="font-black text-red-800 dark:text-red-500 uppercase text-[10px] tracking-widest">Reset Total</h4>
              </div>
              <p className="text-[11px] text-red-700 dark:text-red-600/80 mb-4 font-medium leading-relaxed">
                Zera o sistema (Empresas, Usuários e Tudo).
              </p>
              <button 
                onClick={() => resetarSistema('completo')}
                disabled={isResetting}
                className="w-full py-3 bg-red-600 text-white text-[9px] font-black uppercase tracking-[0.2em] rounded-xl hover:bg-red-700 transition-all flex items-center justify-center gap-2"
              >
                Apagar TUDO do Banco
              </button>
            </div>
          </div>

          <div className="mt-8 p-6 bg-gray-50 dark:bg-slate-900/50 rounded-2xl flex items-center gap-4 border border-gray-100 dark:border-slate-800">
            <AlertCircle className="w-10 h-10 text-primary-500 opacity-50" />
            <p className="text-xs text-gray-500 dark:text-slate-500 font-bold leading-relaxed italic">
              Este painel é de uso exclusivo para apresentações comerciais e testes internos do LicyPilot.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Admin;
