import { useContext, useEffect, useState } from 'react'
import './App.css'
import ThemeToggle from './core/context/ThemeToggle'

import AppRoutes from './routes/AppRoutes'
import { stompService } from './core/sockets/stompClient';
import { AuthContext, type AuthContextType } from './core/context/AuthContext';

import { useNavigate } from 'react-router';


    type TestTy = {
        label: string
    }

function App() {

  const auth : AuthContextType  | undefined = useContext(AuthContext) 
  const navigate = useNavigate();

  useEffect(() => {
      if (!auth) { 
        console.log("Just return")
        return 
      }
    // This logic will now run whenever the user logs in or out
      //

    if (auth.isAuthenticated) {
       stompService.connect();
      console.log("User is authenticated, connecting WebSocket...");
    } else {
      console.log("User is not authenticated, disconnecting WebSocket...");

     navigate(`/`, { replace: true });
      stompService.disconnect();
    }

    // Disconnect when the component unmounts
    return () => {
      stompService.disconnect();
    };
  }, [auth?.isAuthenticated]); // Run this effect when the auth state changes

  return (
    <>
    <AppRoutes/>
    </>
  )

}

export default App
