import { useState } from "react"
import { Loader2 } from 'lucide-react';
import MatchLobbyCard from "src/features/match/components/MatchLobbyCard"
import Navbar from "src/components/layout/Navbar";


interface Props {
  theme: 'light' | 'dark';
}

export default function ProfilePage({theme} : Props){
    const [userName, setUserName] = useState('')
    const [loading, setLoading] = useState(false)


    const HandleSubmit = (matchID : string) => {
    }


    const labelClass = theme === 'dark' ? 'text-gray-400' : 'text-slate-600';
    return (
        
        <>
        <Navbar/>

            <div className="flex flex-col border-2 border-solid mx-10 my-10 h-[64rem] ">
                    <div className="flex mx-10 my-10 gap-5">

                            <span className='px-3 py-1 text-5xl font-bold rounded-full'>
                                Dashboard
                            </span>


                    </div>


                    <div className="flex flex-wrap mx-10 my-10 gap-5 overflow-auto h-1/2">
                        <MatchLobbyCard title='Test' matchID="10" playerCount={1} maxPlayerCount={2} joinRoomClick={ () => HandleSubmit("10") }  />
                    </div>
            </div>


        </>
    )
}
