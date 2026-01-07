import { useEffect, useState } from "react"
import { Loader2 } from 'lucide-react';
import MatchLobbyCard from "src/features/match/components/MatchLobbyCard"
import Navbar from "src/components/layout/Navbar";
import { useJoinMatch } from "src/features/match/hooks/useJoinMatch";
import { type JoinMatchRequest, type LobbyDetails } from "src/features/match/types/match";
import { getAllLobby } from "src/features/match/services/matchService";



interface Props {
  theme: 'light' | 'dark';
}

export default function Home({theme} : Props){

    const [allLobby,setAllLobby] = useState<LobbyDetails[]>()
    const {joinMatchMutation, isLoading, error } = useJoinMatch()

    
    const HandleSubmit = (matchID : string) => {
        const a : JoinMatchRequest = {matchId: matchID} 
        try { 
            joinMatchMutation(a)
        }catch(e){
            console.log("Error fetch : " + e)

        }
    }


    const fetchAllLobby = async () => {
        try { 
          const getLobbies : LobbyDetails[]  = await getAllLobby();
          setAllLobby(getLobbies)

        }catch(e){
            console.log("Error fetch : " + e)

        }
    }

    useEffect(
      () => {
            fetchAllLobby()
      }
    , []) // Fetch lobby once

    const labelClass = theme === 'dark' ? 'text-gray-400' : 'text-slate-600';
    return (
        
        <>
        <Navbar/>

            <div className="flex flex-col border-2 border-solid mx-10 my-10 h-[64rem] ">
                    <div className="flex mx-10 my-10 gap-5">

                            <span className='px-3 py-1 text-5xl font-bold rounded-full'>
                                Match Rooms
                            </span>


                    </div>


                    <div className="flex flex-wrap mx-10 my-10 gap-5 overflow-auto h-1/2">
                        {
                            allLobby?.map((lC : LobbyDetails,keyInd) => { 

                                    return <MatchLobbyCard key={keyInd} title={lC.roomCode} matchID={lC.matchId} playerCount={1} maxPlayerCount={2} joinRoomClick={ () => HandleSubmit(lC.matchId) }  />
                            })
                        
                        }
                    </div>
            </div>


        </>
    )
}
