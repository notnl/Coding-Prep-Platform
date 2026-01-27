import { useContext, useState } from "react"
import { Loader2 } from 'lucide-react';
import { useNavigate } from "react-router";
import useAuth from "src/core/hooks/useAuth";
import { AuthContext, type AuthContextType } from "src/core/context/AuthContext";

import DevHubLogo from "../assets/devhublogo.svg";


interface Props {
  theme: 'light' | 'dark';
}
type LoginResponseType = { 
    accessToken: string,
    refreshToken: string
}
type JoinRoomResponseType = { 
    matchId: string,
    accessToken: string,
}

export default function Login({theme} : Props){

    const navigate = useNavigate();
    const [userName, setUserName] = useState('')
    const [password, setPassword] = useState('')
    const [roomCode, setRoomCode] = useState('')
    
    const [loading, setLoading] = useState(false)
    const [roomLoading, setRoomLoading] = useState(false)

    const auth : AuthContextType  | undefined = useContext(AuthContext) 

    const HandleSubmit = async (e: React.FormEvent) => {
            e.preventDefault();
            setLoading(true);
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
                                    console.log("Caught this : " + e)
                                }
                       )

                       auth?.handleLoginSuccess(r.accessToken,r.refreshToken)
                       
                  
            }
            catch (er) {
                console.log(er);
            } finally {
                setLoading(false);
            }
                    
    }

    const handleJoinRoom = async (e: React.FormEvent) => {
        e.preventDefault();
        setRoomLoading(true);
        try {
            console.log("Joining room with code:", roomCode);
            const getExistingToken : string | null = localStorage.getItem("accessToken")

             const r : JoinRoomResponseType  = await fetch('/api/v1/match/joinRoomCode', 
                        {
                                        method:"POST",
                                        body: JSON.stringify({roomCode:roomCode}),
                                        headers: {
                                            "Content-Type": "application/json",
                                            ...(getExistingToken && { Authorization: `Bearer ${getExistingToken}` }),

                                        },
                        }

                       ).then((resolvedResponse : Response) => {
                            return resolvedResponse.json()
                       }).catch(
                                (e) => {
                                    console.error("Error converting to json: " + e.message)
                                }
                       )

                       if (r) {
                            auth?.handleRoomCodeSuccess(r.accessToken,r.matchId)
                       }
            
            
            
        } catch (error) {
            console.error("Error joining room:", error);
        } finally {
            setRoomLoading(false);
        }
    }


    const labelClass = theme === 'dark' ? 'text-gray-400' : 'text-slate-600';
    const dividerClass = theme === 'dark' ? 'text-gray-400' : 'text-gray-600';
    const borderClass = theme === 'dark' ? 'border-gray-700' : 'border-gray-300';
    
    return (

        
        <div className="flex max-w-full h-screen justify-center items-center">

            <div className="flex flex-col border-2 border-solid gap-8 min-w-1/3 min-h-1/2 items-center p-8">
                
                <span className="text-5xl py-5 text-center">
                    Coding Prep Platform
                    <img
                        className="z-50 inset-0 mx-auto h-8 w-auto transition-all duration-300 ease-in-out hover:scale-102"
                        src={DevHubLogo}
                        alt="DevHub Logo"
                    />
                </span>

                {/* Login Form Section */}
                <form onSubmit={HandleSubmit} className="w-full space-y-6">
                    <span className="flex items-center gap-4">
                        <span className="mx-4 font-bold min-w-24">
                            Username : 
                        </span>
                        <input 
                            id="Username" 
                            name="username" 
                            value={userName} 
                            onChange={(e) => setUserName(e.target.value)} 
                            placeholder="Key in any name"
                            className={`border-2 border-solid selection:bg-indigo-500 w-full p-2 rounded ${labelClass} ${borderClass}`}
                            required 
                            disabled={loading}
                        />
                    </span>

                    <span className="flex items-center gap-4">
                        <span className="mx-4 font-bold min-w-24">
                            Password : 
                        </span>
                        <input 
                            id="Password" 
                            name="password" 
                            value={password} 
                            onChange={(e) => setPassword(e.target.value)} 
                            placeholder="Key in any password"
                            className={`border-2 border-solid selection:bg-indigo-500 w-full p-2 rounded ${labelClass} ${borderClass}`}
                            type="password"
                            required 
                            disabled={loading}
                        />
                    </span>
                    
                    <button 
                        type="submit"
                        className="w-full bg-[#F97316] hover:bg-[#EA580C] text-white font-bold py-3 px-4 rounded-md transition-transform transform hover:scale-105 disabled:bg-orange-900/50 disabled:cursor-not-allowed disabled:transform-none"
                        disabled={loading}
                    >
                        {loading ? <Loader2 className="h-5 w-5 animate-spin mx-auto" /> : 'Sign In'}
                    </button>
                </form>

                <div className="flex items-center w-full my-4">
                    <div className={`flex-grow h-px ${borderClass}`}></div>
                    <span className={`px-4 ${dividerClass} font-semibold`}>OR</span>
                    <div className={`flex-grow h-px ${borderClass}`}></div>
                </div>

                {/* Join Room Section */}
                <div className="w-full space-y-4">
                    <h3 className={`text-center text-lg font-semibold ${labelClass}`}>
                        Join with Room Code
                    </h3>
                    
                    <form onSubmit={handleJoinRoom} className="flex gap-2">
                        <input
                            type="text"
                            value={roomCode}
                            onChange={(e) => setRoomCode(e.target.value.toUpperCase())}
                            placeholder="Enter room code"
                            className={`flex-grow border-2 border-solid p-2 rounded ${labelClass} ${borderClass}`}
                            disabled={roomLoading}
                            maxLength={6}
                            pattern="[A-Z0-9]{6}"
                            title="Room code should be 6 alphanumeric characters"
                        />
                        <button
                            type="submit"
                            disabled={!roomCode.trim() || roomLoading}
                            className="bg-[#3B82F6] hover:bg-[#2563EB] text-white font-bold py-2 px-4 rounded-md transition-colors disabled:bg-blue-900/50 disabled:cursor-not-allowed disabled:transform-none"
                        >
                            {roomLoading ? <Loader2 className="h-5 w-5 animate-spin" /> : 'Join'}
                        </button>
                    </form>
                    
                    <p className={`text-sm text-center ${dividerClass}`}>
                        Enter the 6-digit code shared by the host
                    </p>
                </div>

            </div>

        </div>

    )
}
