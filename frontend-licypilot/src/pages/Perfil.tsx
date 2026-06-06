import { useState, useEffect } from 'react';
import { Save, Building, User, Tag as TagIcon, CheckCircle2, Loader2, FileText, Trash2, Plus } from 'lucide-react';

const Perfil = () => {
  const [empresa, setEmpresa] = useState<any>({
    razaoSocial: '',
    cnpj: '',
    capitalSocial: 0,
    cnaes: [],
    documentosRegulares: [],
    experienciasTecnicas: []
  });
  const [usuario, setUsuario] = useState<any>({
    nome: '',
    email: ''
  });
  const [isLoading, setIsLoading] = useState(true);
  const [showSuccess, setShowSuccess] = useState(false);

  const [newTag, setNewTag] = useState('');
  const [newDoc, setNewDoc] = useState('');
  
  // Estados para nova experiência estruturada
  const [expSkill, setExpSkill] = useState('');
  const [expDetail, setExpDetail] = useState('');

  const preDefinedTags = [
    "Desenvolvimento de Software", 
    "Fornecimento de TI", 
    "Obras", 
    "Serviços de Limpeza", 
    "Consultoria", 
    "Segurança",
    "Mobiliário",
    "Licenciamento de Software"
  ];

  const preDefinedDocs = [
    "Certidão Negativa de Débitos Federais",
    "Certidão Negativa de Débitos Estaduais",
    "Certidão Negativa de Débitos Municipais",
    "CRF do FGTS",
    "Certidão Negativa de Débitos Trabalhistas",
    "Certidão Negativa de Efeitos de Falência",
    "Certidão de Registro de Pessoa Jurídica",
    "Certidão de Registro na entidade profissional competente",
    "Atestado(s) de Capacidade Técnica",
    "Declaração de Vistoria",
    "Comprovação de patrimônio líquido",
    "Balanço patrimonial",
    "Visto do Conselho de Santa Catarina",
    "Certidão de Regularidade Fiscal",
    "Certidão Simplificada",
    "Declaração de Enquadramento",
    "Declaração de Contratos",
    "Documentação de regularidade fiscal",
    "Procuração",
    "Estatuto ou Contrato Social",
    "Declaração de cumprimento",
    "Declaração de conformidade trabalhista",
    "Declaração de habilitação",
    "Garantia da Proposta",
    "Seguro-Garantia",
    "Certidão Conjunta",
    "CNEP",
    "Consulta CNEP",
    "Declaração de reserva de cargos",
    "Ato constitutivo, Estatuto ou Contrato Social"
  ];

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [resEmpresas, resUsuarios] = await Promise.all([
          fetch('/api/v1/empresas'),
          fetch('/api/v1/usuarios')
        ]);
        
        if (resEmpresas.ok) {
          const empresas = await resEmpresas.json();
          if (Array.isArray(empresas) && empresas.length > 0) {
            const emp = empresas[0];
            // Garante que os arrays existam
            setEmpresa({
              ...emp,
              cnaes: emp.cnaes || [],
              documentosRegulares: emp.documentosRegulares || [],
              experienciasTecnicas: emp.experienciasTecnicas || []
            });
          }
        }
        
        if (resUsuarios.ok) {
          const usuarios = await resUsuarios.json();
          if (Array.isArray(usuarios) && usuarios.length > 0) setUsuario(usuarios[0]);
        }
      } catch (err) {
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, []);

  const handleAddTag = (tag: string) => {
    if (!tag) return;
    const currentTags = empresa.cnaes || [];
    if (!currentTags.includes(tag)) {
      setEmpresa({ ...empresa, cnaes: [...currentTags, tag] });
    }
    setNewTag('');
  };

  const handleRemoveTag = (index: number) => {
    setEmpresa({
      ...empresa,
      cnaes: empresa.cnaes.filter((_: any, i: number) => i !== index)
    });
  };

  const handleAddDoc = (doc: string) => {
    if (!doc) return;
    const currentDocs = empresa.documentosRegulares || [];
    if (!currentDocs.includes(doc)) {
      setEmpresa({ ...empresa, documentosRegulares: [...currentDocs, doc] });
    }
    setNewDoc('');
  };

  const handleRemoveDoc = (index: number) => {
    setEmpresa({
      ...empresa,
      documentosRegulares: empresa.documentosRegulares.filter((_: any, i: number) => i !== index)
    });
  };

  const handleAddExperiencia = () => {
    if (!expSkill || !expDetail) return;
    const novaExp = { especialidade: expSkill, detalheExperiencia: expDetail };
    setEmpresa({
      ...empresa,
      experienciasTecnicas: [...(empresa.experienciasTecnicas || []), novaExp]
    });
    setExpSkill('');
    setExpDetail('');
  };

  const handleRemoveExperiencia = (index: number) => {
    setEmpresa({
      ...empresa,
      experienciasTecnicas: empresa.experienciasTecnicas.filter((_: any, i: number) => i !== index)
    });
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      // 1. Salva Usuário
      const userMethod = !usuario.id ? 'POST' : 'PUT';
      const userUrl = !usuario.id ? '/api/v1/usuarios' : `/api/v1/usuarios/${usuario.id}`;
      const resUser = await fetch(userUrl, {
        method: userMethod,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(usuario),
      });
      if (resUser.ok) {
        const savedUser = await resUser.json();
        setUsuario(savedUser);
      }

      // 2. Salva Empresa
      const empMethod = !empresa.id ? 'POST' : 'PUT';
      const empUrl = !empresa.id ? '/api/v1/empresas' : `/api/v1/empresas/${empresa.id}`;
      const resEmp = await fetch(empUrl, {
        method: empMethod,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(empresa),
      });

      if (resEmp.ok) {
        const savedEmp = await resEmp.json();
        setEmpresa(savedEmp);
        setShowSuccess(true);
        window.dispatchEvent(new Event('profileUpdated'));
        setTimeout(() => setShowSuccess(false), 3000);
      }
    } catch (error) {
      console.error(error);
      alert('Falha na comunicação com o servidor.');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading && !empresa.id && !usuario.id) {
    return (
      <div className="flex justify-center items-center h-[60vh]">
        <Loader2 className="w-10 h-10 animate-spin text-primary-600" />
      </div>
    );
  }

  return (
    <div className="w-full max-w-5xl mx-auto p-8 space-y-10 transition-colors duration-300">
      <header>
        <h2 className="text-4xl font-black text-teal-950 dark:text-teal-50 tracking-tight">Perfil Corporativo</h2>
        <p className="text-gray-500 dark:text-slate-400 mt-2 text-lg font-medium">Configure os dados da sua organização para diagnósticos de viabilidade precisos.</p>
      </header>

      <form onSubmit={handleSave} className="space-y-10">
        {/* Seção Usuário */}
        <div className="bg-white dark:bg-slate-800 rounded-[2.5rem] border border-gray-100 dark:border-slate-700 shadow-sm p-10 space-y-8">
          <h3 className="text-xl font-bold text-teal-950 dark:text-teal-50 flex items-center gap-3">
            <div className="p-2 bg-primary-50 dark:bg-primary-900/20 rounded-xl text-primary-600 dark:text-primary-400">
              <User className="w-6 h-6" />
            </div>
            Gestor da Conta
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
            <div>
              <label className="block text-sm font-black text-gray-400 dark:text-slate-500 uppercase tracking-widest mb-2 ml-1">Nome Completo</label>
              <input 
                type="text" 
                required
                value={usuario.nome}
                onChange={(e) => setUsuario({...usuario, nome: e.target.value})}
                className="w-full px-6 py-4 rounded-2xl bg-gray-50 dark:bg-slate-900 border border-gray-100 dark:border-slate-700 focus:ring-4 focus:ring-primary-500/10 focus:border-primary-500 outline-none transition-all font-medium text-teal-950 dark:text-teal-50"
                placeholder="Ex: Milton Santos"
              />
            </div>
            <div>
              <label className="block text-sm font-black text-gray-400 dark:text-slate-500 uppercase tracking-widest mb-2 ml-1">E-mail Corporativo</label>
              <input 
                type="email" 
                required
                value={usuario.email}
                onChange={(e) => setUsuario({...usuario, email: e.target.value})}
                className="w-full px-6 py-4 rounded-2xl bg-gray-50 dark:bg-slate-900 border border-gray-100 dark:border-slate-700 focus:ring-4 focus:ring-primary-500/10 focus:border-primary-500 outline-none transition-all font-medium text-teal-950 dark:text-teal-50"
                placeholder="milton@empresa.com"
              />
            </div>
          </div>
        </div>

        {/* Seção Empresa */}
        <div className="bg-white dark:bg-slate-800 rounded-[2.5rem] border border-gray-100 dark:border-slate-700 shadow-sm p-10 space-y-8">
          <h3 className="text-xl font-bold text-teal-950 dark:text-teal-50 flex items-center gap-3">
            <div className="p-2 bg-primary-50 dark:bg-primary-900/20 rounded-xl text-primary-600 dark:text-primary-400">
              <Building className="w-6 h-6" />
            </div>
            Dados da Organização
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
            <div className="md:col-span-2">
              <label className="block text-sm font-black text-gray-400 dark:text-slate-500 uppercase tracking-widest mb-2 ml-1">Razão Social</label>
              <input 
                type="text" 
                required
                value={empresa.razaoSocial}
                onChange={(e) => setEmpresa({...empresa, razaoSocial: e.target.value})}
                className="w-full px-6 py-4 rounded-2xl bg-gray-50 dark:bg-slate-900 border border-gray-100 dark:border-slate-700 focus:ring-4 focus:ring-primary-500/10 focus:border-primary-500 outline-none transition-all font-medium text-teal-950 dark:text-teal-50"
              />
            </div>
            <div>
              <label className="block text-sm font-black text-gray-400 dark:text-slate-500 uppercase tracking-widest mb-2 ml-1">CNPJ</label>
              <input 
                type="text" 
                required
                value={empresa.cnpj}
                onChange={(e) => setEmpresa({...empresa, cnpj: e.target.value})}
                className="w-full px-6 py-4 rounded-2xl bg-gray-50 dark:bg-slate-900 border border-gray-100 dark:border-slate-700 focus:ring-4 focus:ring-primary-500/10 focus:border-primary-500 outline-none transition-all font-medium text-teal-950 dark:text-teal-50"
              />
            </div>
            <div>
              <label className="block text-sm font-black text-gray-400 dark:text-slate-500 uppercase tracking-widest mb-2 ml-1">Porte</label>
              <select 
                value={empresa.porte || ''}
                onChange={(e) => setEmpresa({...empresa, porte: e.target.value})}
                className="w-full px-6 py-4 rounded-2xl bg-gray-50 dark:bg-slate-900 border border-gray-100 dark:border-slate-700 focus:ring-4 focus:ring-primary-500/10 focus:border-primary-500 outline-none transition-all font-medium appearance-none text-teal-950 dark:text-teal-50"
              >
                <option value="">Selecione...</option>
                <option value="ME">Microempresa (ME)</option>
                <option value="EPP">Empresa de Pequeno Porte (EPP)</option>
                <option value="DEMAIS">Grande Porte / Demais</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-black text-gray-400 dark:text-slate-500 uppercase tracking-widest mb-2 ml-1">Capital Social (R$)</label>
              <input 
                type="number" 
                value={empresa.capitalSocial}
                onChange={(e) => setEmpresa({...empresa, capitalSocial: parseFloat(e.target.value) || 0})}
                className="w-full px-6 py-4 rounded-2xl bg-gray-50 dark:bg-slate-900 border border-gray-100 dark:border-slate-700 focus:ring-4 focus:ring-primary-500/10 focus:border-primary-500 outline-none transition-all font-medium text-teal-950 dark:text-teal-50"
              />
            </div>
          </div>

          {/* SEÇÃO: ESPECIALIDADES (TAGS) */}
          <div className="pt-8 border-t border-gray-50 dark:border-slate-700/50">
            <h4 className="text-sm font-black text-gray-400 dark:text-slate-500 uppercase tracking-widest mb-6 flex items-center gap-2">
              <TagIcon className="w-4 h-4 text-primary-500" /> Especialidades da Empresa
            </h4>
            
            <div className="flex flex-wrap gap-3 mb-8">
              {(empresa.cnaes || []).length === 0 && <p className="text-sm text-gray-400 italic bg-gray-50 dark:bg-slate-900/50 p-4 rounded-xl border border-dashed border-gray-200 dark:border-slate-700 w-full text-center">Nenhuma especialidade selecionada.</p>}
              {(empresa.cnaes || []).map((tag: string, index: number) => (
                <span key={index} className="group flex items-center gap-3 px-5 py-2.5 bg-primary-50 dark:bg-primary-900/20 text-primary-700 dark:text-primary-300 rounded-2xl text-sm font-bold border border-primary-100 dark:border-primary-800 shadow-sm animate-fade-in hover:scale-105 transition-transform">
                  {tag}
                  <button type="button" onClick={() => handleRemoveTag(index)} className="hover:text-red-500 transition-colors">×</button>
                </span>
              ))}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-10">
              <div className="space-y-3">
                <p className="text-[10px] font-black text-gray-400 uppercase ml-1">Adicionar Especialidade</p>
                <div className="flex gap-2">
                  <input 
                    type="text"
                    value={newTag}
                    onChange={(e) => setNewTag(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), handleAddTag(newTag))}
                    placeholder="Ex: Consultoria em TI"
                    className="flex-1 px-6 py-3 rounded-xl bg-gray-50 dark:bg-slate-900 border border-gray-100 dark:border-slate-700 focus:ring-2 focus:ring-primary-500 outline-none text-sm transition-all text-teal-950 dark:text-teal-50"
                  />
                  <button type="button" onClick={() => handleAddTag(newTag)} className="px-6 py-3 bg-gray-800 dark:bg-slate-700 text-white rounded-xl text-sm font-black hover:bg-gray-700 transition-all shadow-lg">OK</button>
                </div>
              </div>
              <div className="space-y-3">
                <p className="text-[10px] font-black text-gray-400 uppercase ml-1">Sugestões</p>
                <div className="flex flex-wrap gap-2">
                  {preDefinedTags.filter(t => !(empresa.cnaes || []).includes(t)).map((tag) => (
                    <button key={tag} type="button" onClick={() => handleAddTag(tag)} className="px-4 py-2 bg-white dark:bg-slate-800 text-gray-600 dark:text-slate-400 rounded-xl text-xs font-bold hover:bg-primary-500 hover:text-white transition-all border border-gray-100 dark:border-slate-700 shadow-sm">+ {tag}</button>
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* SEÇÃO: ACERVO TÉCNICO ESTRUTURADO */}
          <div className="pt-8 border-t border-gray-50 dark:border-slate-700/50">
            <h4 className="text-sm font-black text-gray-400 dark:text-slate-500 uppercase tracking-widest mb-6 flex items-center gap-2">
              <FileText className="w-4 h-4 text-amber-500" /> Acervo Técnico (Experiência por Área)
            </h4>
            
            <div className="space-y-4 mb-8">
              {(empresa.experienciasTecnicas || []).length === 0 && <p className="text-sm text-gray-400 italic bg-gray-50 dark:bg-slate-900/50 p-4 rounded-xl border border-dashed border-gray-200 dark:border-slate-700 w-full text-center">Descreva sua experiência por área (ex: "Obras Civis" -&gt; "Construção de 5 escolas").</p>}
              {(empresa.experienciasTecnicas || []).map((exp: any, index: number) => (
                <div key={index} className="flex items-start justify-between p-5 bg-amber-50/30 dark:bg-amber-900/10 border border-amber-100 dark:border-amber-900/30 rounded-2xl animate-fade-in group">
                  <div>
                    <span className="text-xs font-black uppercase text-amber-600 dark:text-amber-400 tracking-wider">{exp.especialidade}</span>
                    <p className="text-teal-950 dark:text-teal-50 font-medium mt-1">{exp.detalheExperiencia}</p>
                  </div>
                  <button type="button" onClick={() => handleRemoveExperiencia(index)} className="p-2 text-gray-400 hover:text-red-500 transition-colors opacity-0 group-hover:opacity-100">
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              ))}
            </div>

            <div className="bg-gray-50 dark:bg-slate-900/50 p-6 rounded-[2rem] border border-gray-100 dark:border-slate-700 space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="md:col-span-1">
                  <label className="text-[10px] font-black text-gray-400 uppercase ml-1 block mb-2">Especialidade / Área</label>
                  <input 
                    type="text"
                    value={expSkill}
                    onChange={(e) => setExpSkill(e.target.value)}
                    placeholder="Ex: Estruturas Metálicas"
                    className="w-full px-5 py-3 rounded-xl bg-white dark:bg-slate-800 border border-gray-200 dark:border-slate-700 focus:ring-2 focus:ring-amber-500 outline-none text-sm transition-all"
                  />
                </div>
                <div className="md:col-span-2">
                  <label className="text-[10px] font-black text-gray-400 uppercase ml-1 block mb-2">Detalhamento da Experiência (Contratos, m², quantidades...)</label>
                  <div className="flex gap-2">
                    <input 
                      type="text"
                      value={expDetail}
                      onChange={(e) => setExpDetail(e.target.value)}
                      placeholder="Ex: Execução de 10.000m² de cobertura em galpões industriais."
                      className="flex-1 px-5 py-3 rounded-xl bg-white dark:bg-slate-800 border border-gray-200 dark:border-slate-700 focus:ring-2 focus:ring-amber-500 outline-none text-sm transition-all"
                    />
                    <button type="button" onClick={handleAddExperiencia} className="p-3 bg-amber-500 text-white rounded-xl hover:bg-amber-600 transition-all shadow-lg shadow-amber-500/20">
                      <Plus className="w-6 h-6" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* SEÇÃO: DOCUMENTOS REGULARIZADOS */}
          <div className="pt-8 border-t border-gray-50 dark:border-slate-700/50">
            <h4 className="text-sm font-black text-gray-400 dark:text-slate-500 uppercase tracking-widest mb-6 flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 text-emerald-500" /> Documentos já Regularizados
            </h4>
            
            <div className="flex flex-wrap gap-3 mb-8">
              {(empresa.documentosRegulares || []).length === 0 && <p className="text-sm text-gray-400 italic bg-gray-50 dark:bg-slate-900/50 p-4 rounded-xl border border-dashed border-gray-200 dark:border-slate-700 w-full text-center">Informe seus documentos regulares.</p>}
              {(empresa.documentosRegulares || []).map((doc: string, index: number) => (
                <span key={index} className="group flex items-center gap-3 px-5 py-2.5 bg-emerald-50 dark:bg-emerald-900/20 text-emerald-700 dark:text-emerald-300 rounded-2xl text-sm font-bold border border-emerald-100 dark:border-emerald-800 shadow-sm animate-fade-in hover:scale-105 transition-transform">
                  {doc}
                  <button type="button" onClick={() => handleRemoveDoc(index)} className="hover:text-red-500 transition-colors">×</button>
                </span>
              ))}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-10">
              <div className="space-y-3">
                <p className="text-[10px] font-black text-gray-400 uppercase ml-1">Adicionar Documento</p>
                <div className="flex gap-2">
                  <input 
                    type="text"
                    value={newDoc}
                    onChange={(e) => setNewDoc(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), handleAddDoc(newDoc))}
                    placeholder="Ex: Certidão Negativa Municipal"
                    className="flex-1 px-6 py-3 rounded-xl bg-gray-50 dark:bg-slate-900 border border-gray-100 dark:border-slate-700 focus:ring-2 focus:ring-primary-500 outline-none text-sm transition-all text-teal-950 dark:text-teal-50"
                  />
                  <button type="button" onClick={() => handleAddDoc(newDoc)} className="px-6 py-3 bg-gray-800 dark:bg-slate-700 text-white rounded-xl text-sm font-black hover:bg-gray-700 transition-all shadow-lg">OK</button>
                </div>
              </div>
              <div className="space-y-3">
                <p className="text-[10px] font-black text-gray-400 uppercase ml-1">Documentos Comuns</p>
                <div className="flex flex-wrap gap-2">
                  {preDefinedDocs.filter(d => !(empresa.documentosRegulares || []).includes(d)).map((doc) => (
                    <button key={doc} type="button" onClick={() => handleAddDoc(doc)} className="px-4 py-2 bg-white dark:bg-slate-800 text-gray-600 dark:text-slate-400 rounded-xl text-xs font-bold hover:bg-emerald-500 hover:text-white transition-all border border-gray-100 dark:border-slate-700 shadow-sm">+ {doc}</button>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-6 pt-4">
          <button 
            type="submit"
            disabled={isLoading}
            className="px-16 py-5 bg-primary-500 text-white font-black text-lg rounded-[2rem] shadow-2xl shadow-primary-500/30 hover:bg-primary-600 transition-all flex items-center gap-3 disabled:bg-gray-300 dark:disabled:bg-slate-700"
          >
            {isLoading ? <Loader2 className="w-6 h-6 animate-spin" /> : <Save className="w-6 h-6" />}
            Atualizar Perfil Corporativo
          </button>
          {showSuccess && (
            <div className="flex items-center gap-3 text-green-600 dark:text-green-400 font-black animate-fade-in text-lg">
              <CheckCircle2 className="w-6 h-6" />
              Dados atualizados!
            </div>
          )}
        </div>
      </form>
    </div>
  );
};

export default Perfil;
