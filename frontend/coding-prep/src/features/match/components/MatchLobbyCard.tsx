

type MatchLobbyCardType = { 
    title : string,
    matchID : string,
    playerCount : number,
    maxPlayerCount : number,
    joinRoomClick: () => void

}

export default function MatchLobbyCard ({title,matchID,playerCount,maxPlayerCount,joinRoomClick} : MatchLobbyCardType) {
    
    return ( 
            <div className=" flex flex-col border-2 min-w-1/5 h-fit transition-all duration-200 ease-out  hover:bg-white/70 hover:scale-102 cursor-pointer" onClick={joinRoomClick} >
                    <div className="p-5">
                            {title }
                    </div>


                    <div className="flex flex-row-reverse p-5  gap-2 ">

                    <span>
                            {maxPlayerCount } 
                            </span>
                             
                    <span>
                            {playerCount }  / 
                            </span>

                    </div>

                    
            </div>
        
           )
}
