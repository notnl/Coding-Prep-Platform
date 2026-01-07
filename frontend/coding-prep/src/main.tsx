import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router'
import { ThemeProvider } from './core/context/ThemeContext'

import App from './App'
import { AuthProvider } from './core/context/AuthContext'


createRoot(document.getElementById('root')!).render(

  <BrowserRouter>
    <ThemeProvider>
    <AuthProvider>
      <App />
    </AuthProvider>
    </ThemeProvider>
  </BrowserRouter>

)
