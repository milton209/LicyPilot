import { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import Dashboard from './pages/Dashboard';
import Admin from './pages/Admin';
import Perfil from './pages/Perfil';
import LicitacaoDetalhes from './pages/LicitacaoDetalhes';
import LandingPage from './pages/LandingPage';

function App() {
  const [isDarkMode, setIsDarkMode] = useState(() => {
    return localStorage.getItem('theme') === 'dark';
  });

  useEffect(() => {
    if (isDarkMode) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('theme', 'light');
    }
  }, [isDarkMode]);

  const toggleTheme = () => setIsDarkMode(!isDarkMode);

  return (
    <Router>
      <div className={`min-h-screen w-full transition-colors duration-300 ${isDarkMode ? 'bg-slate-900 text-slate-100' : 'bg-gray-50 text-gray-900'}`}>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/*" element={
            <div className="flex w-full min-h-screen">
              <Sidebar isDarkMode={isDarkMode} toggleTheme={toggleTheme} />
              <main className="flex-1 min-w-0 overflow-x-hidden">
                <Routes>
                  <Route path="/dashboard" element={<Dashboard />} />
                  <Route path="/licitacao/:id" element={<LicitacaoDetalhes />} />
                  <Route path="/admin" element={<Admin />} />
                  <Route path="/perfil" element={<Perfil />} />
                </Routes>
              </main>
            </div>
          } />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
