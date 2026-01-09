

type MatchLobbyCardType = { 
    title : string,
    joinRoomClick: () => void

}

export default function MatchLobbyCard ({title,joinRoomClick} : MatchLobbyCardType) {
    
    return ( 
            <div className=" flex flex-col border-2 w-fit h-fit transition-all duration-200 ease-out  hover:bg-white/70 hover:scale-102 cursor-pointer" onClick={joinRoomClick} >
                    <div className="p-5">
                            {title }
                    </div>


                    <div className="flex flex-row-reverse p-5  gap-2 ">

                    </div>

                    
            </div>
        
           )
}
