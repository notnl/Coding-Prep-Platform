import { useContext, useState } from "react"
import { Loader2 } from 'lucide-react';
import { useNavigate } from "react-router";
import useAuth from "src/core/hooks/useAuth";
import { AuthContext, type AuthContextType } from "src/core/context/AuthContext";


interface Props {
  theme: 'light' | 'dark';
}
type LoginResponseType = { 
    accessToken: string,
    refreshToken: string
}

export default function Login({theme} : Props){

    const navigate = useNavigate();
    const [userName, setUserName] = useState('')
    const [password, setPassword] = useState('')

    const [loading, setLoading] = useState(false)

    const auth : AuthContextType  | undefined = useContext(AuthContext) 

    const HandleSubmit = async (e: React.FormEvent) => {
            e.preventDefault();
            try {
             const r : LoginResponseType  = await fetch('/api/v1/auth/login', 
                        {
                                        method:"POST",
                                        body: JSON.stringify({username:userName , password:password}),
                                        headers: {
                                            "Content-Type": "application/json"
                                        },
                        }

                       ).then((resolvedResponse : Response) => {
                            return resolvedResponse.json()
                       }).catch(
                                (e) => {
                                    console.log("Caught htis : " + e)
                                }
                       )

                       auth?.handleLoginSuccess(r.accessToken,r.refreshToken)
                       
                  
            }
            catch (er) {
                console.log(er);
            }
                    
    }


    const labelClass = theme === 'dark' ? 'text-gray-400' : 'text-slate-600';
    return (
        
        <form onSubmit={HandleSubmit} className="flex max-w-full h-screen justify-center items-center">
                <div className="flex flex-col border-2 border-solid  min-w-1/2 min-h-1/2 items-center">
                            
                            <span className="text-5xl py-5">
                                    Coding Prep Platform
                            </span>

                            <span className="text-1xl py-5">
                                    Welcome to Coding Prep, compete with one another and share your answers to earn meaningful prizes
                            </span>


            <input id="Username" name="username" value={userName} onChange={(e) => setUserName(e.target.value)} placeholder="Key in any name"
              className={`border-2 border-solid selection:bg-indigo-500 min-w-1/2 ${labelClass}`}
              required disabled={loading}/>

            <input id="Password" name="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Key in any password"
              className={`border-2 border-solid selection:bg-indigo-500 min-w-1/2 ${labelClass}`}
              required disabled={loading}/>

            <button type="submit"
              className="border-2 border-solid  min-w-1/8 min-h-1/4  bg-[#F97316] hover:bg-[#EA580C] text-white font-bold py-3 px-4 rounded-md transition-transform transform hover:scale-105 disabled:bg-orange-900/50 disabled:cursor-not-allowed disabled:transform-none"
              disabled={loading }>
              {loading ? <Loader2 className="h-5 w-5 animate-spin" /> : 'Sign In'}
            </button>

                </div>


        </form>

    )
}
