import { useEffect, useState } from "react"
import { Loader2 } from 'lucide-react';
import MatchLobbyCard from "src/features/match/components/MatchLobbyCard"
import Navbar from "src/components/layout/Navbar";
import { useJoinMatch } from "src/features/match/hooks/useJoinMatch";
import { type JoinMatchRequest, type LobbyDetails } from "src/features/match/types/match";
import { getAllLobby } from "src/features/match/services/matchService";


import SBPengi from "../assets/sbpengi.svg";

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
  <div className="flex flex-col mx-10 my-10 h-[64rem] bg-black rounded-2xl shadow-lg border border-gray-200 overflow-hidden">
    
    {/* Header Section */}
    <div className="flex mx-10 my-10 gap-5 justify-between items-center bg-black rounded-lg p-6 border border-gray-300 shadow-sm">
      
      <div className="flex items-center gap-6">
        <div className="relative">
          <span className="px-3 py-1 text-5xl font-bold text-white-700">
            Match Rooms
          </span>
        </div>
        
        <div className="relative">
          <div className="absolute -inset-1 bg-orange-100 rounded-full opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
          <img
            className="relative w-20 h-20 transition-all duration-300 ease-in-out hover:scale-105"
            src={SBPengi}
            alt="Pengi"
          />
        </div>
      </div>

      {/* Refresh Button */}
      <button
        className="px-8 py-4 bg-orange-500 hover:bg-orange-600 text-white font-bold rounded-lg transition-all duration-300 transform hover:scale-[1.02] active:scale-95 shadow-md hover:shadow-lg  disabled:cursor-not-allowed disabled:transform-none disabled:shadow-none"
        onClick={fetchAllLobby}
      >
        <div className="flex items-center gap-3">
          <svg className="w-5 h-5 transition-transform duration-500 hover:rotate-180" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
          <span>Refresh</span>
        </div>
      </button>
      
    </div>

    {/* Lobby Cards Container */}
    <div className="flex flex-col mx-10 my-10 h-1/2">
      <div className="flex items-center justify-between mb-4 px-2">
        <h3 className="text-xl font-semibold text-white-700">Available Rooms</h3>
        <span className="px-3 py-1  text-white-700 text-sm font-medium rounded-full">
          {allLobby?.length || 0} rooms
        </span>
      </div>
      
      <div className="flex flex-wrap gap-5 overflow-auto p-4 rounded-lg border border-gray-200  h-full">
        {allLobby?.length > 0 ? (
          allLobby.map((lC: LobbyDetails, keyInd) => (
            <MatchLobbyCard
              key={keyInd}
              title={lC.roomCode}
              joinRoomClick={() => HandleSubmit(lC.matchId)}
            />
          ))
        ) : (
          <div className="flex-1 flex flex-col items-center justify-center text-gray-400 p-8 bg-black rounded-xl border-2 border-dashed border-gray-300">
            <div className="w-20 h-20 mb-4 flex items-center justify-center bg-black rounded-full">
              <svg className="w-10 h-10 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1" d="M12 4v16m8-8H4" />
              </svg>
            </div>
            <p className="text-xl font-medium text-gray-500">Waiting for admin to create room...</p>
          </div>
        )}
      </div>
    </div>
    
  </div>
</>
    )
}
