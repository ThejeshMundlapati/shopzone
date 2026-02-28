import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { Provider } from 'react-redux'
import { BrowserRouter } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import { store } from './store/store'
import App from './App'
import './index.css'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <Provider store={store}>
      <BrowserRouter>
        <App />
        <Toaster
          position="bottom-right"
          toastOptions={{
            duration: 2000,
            style: {
              background: '#1e293b',
              color: '#f8fafc',
              borderRadius: '8px',
              padding: '12px 16px',
              fontSize: '14px',
              maxWidth: '300px',
            },
            success: {
              iconTheme: { primary: '#22c55e', secondary: '#f8fafc' },
              duration: 1500,
            },
            error: {
              iconTheme: { primary: '#ef4444', secondary: '#f8fafc' },
              duration: 3000,
            },
          }}
        />
      </BrowserRouter>
    </Provider>
  </StrictMode>
)