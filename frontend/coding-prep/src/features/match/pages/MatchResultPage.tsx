import React, { useState, useEffect, useContext } from 'react';
import Confetti from 'react-confetti';
import { motion } from 'framer-motion';
import { FaTrophy, FaTimes, FaHandshake } from 'react-icons/fa';
import type { MatchResult, PlayerResult, TeamScore } from '../types/match';
import LeaderboardComponent from '../components/LeaderboardComponent';
import { useParams,useNavigate } from 'react-router';
import { getPointsForMatch } from '../services/matchService';
import { stompService } from '../../../core/sockets/stompClient';

import { AuthContext, type AuthContextType } from 'src/core/context/AuthContext';


const useWindowSize = () => {
    const [size, setSize] = useState([0, 0]);
    useEffect(() => {
        function updateSize() {
            setSize([window.innerWidth, window.innerHeight]);
        }
        window.addEventListener('resize', updateSize);
        updateSize();
        return () => window.removeEventListener('resize', updateSize);
    }, []);
    return size;
};


const PlayerResultDisplay = ({ }: { playerResult: PlayerResult }) => (
    <motion.div
        className="bg-gray-100 dark:bg-zinc-800 p-4 rounded-lg"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
    >
        <h4 className="font-bold text-xl text-gray-900 dark:text-white capitalize">{}</h4>
    </motion.div>
);

interface MatchResultOverlayProps {
  result: MatchResult;
  currentUserEmail?: string;
}

export default function MatchResultPage(){

    const navigate = useNavigate();

    const { matchId } = useParams<{ matchId: string }>();


    const [width, height] = useWindowSize();

//  const outcomeConfig = {
//    WIN: { text: "Victory!", icon: <FaTrophy />, textColor: "text-green-600 dark:text-green-400", borderColor: "border-green-500", shadowColor: "shadow-[0_0_30px_5px_rgba(34,197,94,0.3)]" },
//    LOSS: { text: "Defeat", icon: <FaTimes />, textColor: "text-red-600 dark:text-red-500", borderColor: "border-red-500", shadowColor: "shadow-[0_0_20px_5px_rgba(239,68,68,0.3)]" },
//    DRAW: { text: "It's a Draw", icon: <FaHandshake />, textColor: "text-yellow-600 dark:text-yellow-400", borderColor: "border-yellow-500", shadowColor: "shadow-[0_0_20px_5px_rgba(234,179,8,0.3)]" }
//  }[outcome];



    //const  auth  = useContext<AuthContextType | undefined>(AuthContext) // will just assume anyone can view the results of any match

    const [tScore,setTScore] = useState<TeamScore[]>([])
    const [isFetching,setIsFetching] = useState(false)


    const getResults = async () => {  //It is known that users can access this page while the match is ongoing and view the points, but its okay for now 

        if (!matchId) { 
            return
        }

        

        const allPointsInTeamOrder : number[] = await getPointsForMatch(matchId)


        setTScore(

        allPointsInTeamOrder.map(
            (i,ind) => {
                return { teamID:`${ind}`, teamScore:i }
            }
        ).sort( 
                (a,b) => {
                    if (a.teamScore < b.teamScore){ return 1}
                    else if (a.teamScore > b.teamScore){ return -1}
                    
                    return 0
                }
              )

        )

    }
    useEffect(() => {

        if (!matchId) { 
            navigate('/home')
            return
        }

        stompService.connect();

        const subs = [
            stompService.subscribeToPointUpdates(matchId, (payload) => { getResults() // have to get payload if not stomp service will give an error

            }),
        ];


        setIsFetching(true)

        getResults()    

        setIsFetching(false)
        return () => {
            subs.forEach(sub => { if (sub) sub.unsubscribe() });
        };


    }, [])




  return (
    <motion.div
        className={`w-full h-full fixed  bg-black/80 flex items-center justify-center z-50 p-4`}
        initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.5 }}
    >

          <h2 className={`text-5xl font-bold` }> Match results </h2>

        { !isFetching ? (
      <motion.div
        className="bg-white dark:bg-zinc-900 border-2 rounded-xl p-8 pb-12 max-w-lg w-full text-center mx-20"
        initial={{ y: "-100vh", opacity: 0 }} animate={{ y: 0, opacity: 1 }} transition={{ type: 'spring', stiffness: 100, damping: 15, delay: 0.2 }}
      >

        <Confetti width={width} height={height} recycle={false} numberOfPieces={400} />
        <motion.p className="text-gray-600 dark:text-gray-400 mb-6" initial={{ opacity: 0 }} animate={{ opacity: 1 }} >
            The match has ended. Here are the final results:
        </motion.p>

        <motion.div
            initial="hidden" animate="visible"
            variants={{ hidden: {}, visible: { transition: { staggerChildren: 0.3, delayChildren: 1.5 } } }}
        >

        <LeaderboardComponent allTeamScore={tScore}/>

                

        </motion.div>

        <motion.div className='flex items-center justify-center  min-w-full initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 1.2 }}'>
                    <button 
                        onClick={ () => { 
                                navigate('/home');
                                    }
                            }
                        className="flex items-center   gap-2 px-4 py-2 bg-blue-600 text-white font-semibold rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50 disabled:bg-gray-500 disabled:cursor-not-allowed transition-colors"
                    >
                        Back Home
                    </button>

        </motion.div>
      </motion.div>) : (
      <motion.div
      className="bg-white dark:bg-zinc-900 border-2 rounded-xl p-8 pb-12 max-w-lg w-full text-center mx-20"
      initial={{ y: "-100vh", opacity: 0 }} animate={{ y: 0, opacity: 1 }} transition={{ type: 'spring', stiffness: 100, damping: 15, delay: 0.2 }}
      >
        <motion.p className="text-gray-600 dark:text-gray-400 mb-6" initial={{ opacity: 0 }} animate={{ opacity: 1 }} >
            Fetching match data... 
        </motion.p>
      </motion.div> 
      )
        }
    </motion.div>
  );
};
