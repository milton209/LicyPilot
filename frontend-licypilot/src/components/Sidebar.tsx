import { useState, useEffect } from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, User, ShieldCheck, Sun, Moon } from 'lucide-react';

interface SidebarProps {
  isDarkMode: boolean;
  toggleTheme: () => void;
}

const Sidebar = ({ isDarkMode, toggleTheme }: SidebarProps) => {
  const [userData, setUserData] = useState({ nome: 'Usuário', empresa: 'Empresa' });

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const [resUser, resEmp] = await Promise.all([
          fetch('/api/v1/usuarios'),
          fetch('/api/v1/empresas')
        ]);
        if (resUser.ok && resEmp.ok) {
          const users = await resUser.json();
          const emps = await resEmp.json();
          if (users.length > 0 || emps.length > 0) {
            setUserData({
              nome: users[0]?.nome || 'Usuário',
              empresa: emps[0]?.razaoSocial || 'Empresa'
            });
          }
        }
      } catch (err) {
        console.error("Erro ao carregar perfil na barra lateral", err);
      }
    };
    fetchProfile();
    
    // Escuta evento customizado para atualizar quando o perfil for salvo
    window.addEventListener('profileUpdated', fetchProfile);
    return () => window.removeEventListener('profileUpdated', fetchProfile);
  }, []);

  const navItems = [
    { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/perfil', icon: User, label: 'Perfil Empresa' },
    { to: '/admin', icon: ShieldCheck, label: 'Área Admin' },
  ];

  return (
    <aside 
      className={`w-64 border-r flex flex-col transition-colors duration-300 sticky top-0 h-screen flex-shrink-0 z-50 ${
        isDarkMode ? 'bg-slate-900 border-slate-800 text-slate-100' : 'bg-white border-gray-200 text-gray-900'
      }`}
    >
      <div className="p-6 flex items-center justify-between">
        <h1 className="text-2xl font-black text-primary-500 flex items-center gap-2 italic tracking-tighter">
          <ShieldCheck className="w-8 h-8" />
          LicyPilot
        </h1>
      </div>
      
      <nav className="flex-1 px-4 py-4 space-y-2">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `flex items-center gap-3 px-4 py-3 rounded-xl transition-all font-bold ${
                isActive 
                  ? 'bg-primary-500 text-white shadow-lg shadow-primary-500/20 scale-[1.02]' 
                  : isDarkMode 
                    ? 'text-slate-400 hover:bg-slate-800 hover:text-white' 
                    : 'text-gray-500 hover:bg-gray-100 hover:text-gray-900'
              }`
            }
          >
            <item.icon className="w-5 h-5" />
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="p-4 border-t border-gray-100 dark:border-slate-800 space-y-4">
        <button 
          onClick={toggleTheme}
          className={`w-full flex items-center justify-center gap-2 p-3 rounded-xl transition-all font-bold ${
            isDarkMode 
              ? 'bg-slate-800 text-amber-400 hover:bg-slate-700' 
              : 'bg-gray-100 text-primary-600 hover:bg-gray-200'
          }`}
        >
          {isDarkMode ? (
            <>
              <Sun className="w-5 h-5" />
              <span>Modo Claro</span>
            </>
          ) : (
            <>
              <Moon className="w-5 h-5" />
              <span>Modo Escuro</span>
            </>
          )}
        </button>

        <div className="flex items-center gap-3 px-2">
          <div className="w-10 h-10 rounded-xl bg-primary-100 dark:bg-primary-900 flex items-center justify-center text-primary-600 dark:text-primary-300 font-black">
            {userData.nome.charAt(0).toUpperCase()}
          </div>
          <div className="min-w-0">
            <p className="text-sm font-bold truncate" title={userData.nome}>{userData.nome}</p>
            <p className="text-[10px] uppercase font-black tracking-widest text-gray-400 truncate" title={userData.empresa}>{userData.empresa}</p>
          </div>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
