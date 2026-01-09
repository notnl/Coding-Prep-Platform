import React, { useState, useCallback, useEffect, useContext } from 'react';
import { useParams, useNavigate } from 'react-router';
import { Loader2, Ban, Swords, Play } from 'lucide-react';
import type { LobbyState, Player, MatchEvent,SwitchTeamRequest } from '../types/match';
import { getMatchLobbyState, startMatch,switchTeam } from '../services/matchService';
import { stompService } from '../../../core/sockets/stompClient';
import { useServerTimer } from '../../../core/components/useServerTimer';
import MainLayout from '../../../components/layout/MainLayout';
import { AuthContext,type AuthContextType } from 'src/core/context/AuthContext';

//import { useAuth } from '../../../core/hooks/useAuth';

const LobbyStateLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => (
    <MainLayout>
        <div className="flex flex-col items-center justify-center text-center pt-24">
            {children}
        </div>
    </MainLayout>
);

const MatchLobbyPage: React.FC = () => {
  const { matchId } = useParams<{ matchId: string }>(); 
  const navigate = useNavigate();
  const [lobbyState, setLobbyState] = useState<LobbyState | null>(null);

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [cancellationReason, setCancellationReason] = useState<string | null>(null);
  const [redirectCountdown, setRedirectCountdown] = useState(5);
  const [timerData, setTimerData] = useState<{ startTime: number | null}>({ startTime: null});
  const [statusMessage, setStatusMessage] = useState('Connecting to Lobby...');
  const { minutes, seconds, isFinished } = useServerTimer(timerData.startTime);

  const auth : AuthContextType  | undefined = useContext(AuthContext) 

  // Group players by team
  const teamPlayers = React.useMemo(() => {
    if (!lobbyState?.allPlayers) return { 0: [], 1: [], 2: [], 3: [] };
    
    const teams: { [key: number]: any[] } = { 0: [], 1: [], 2: [], 3: [] };
    
    lobbyState.allPlayers.forEach((player) => {
      if (player && player.player_team !== undefined) {
        teams[player.player_team] = teams[player.player_team] || [];
        teams[player.player_team].push(player);
      }
    });
    
    return teams;
  }, [lobbyState]);

  // Check if current user is the host
  const isHost = React.useMemo(() => {

    return lobbyState?.hostId === auth?.user?.id;
  }, [lobbyState?.hostId]);

  //TEMPORARY FIX
  //const isHost = React.useMemo(() => {
  //  return lobbyState?.hostId == 3;
  //}, [lobbyState]);


  const fetchFullLobbyData = useCallback(async () => {
    if (!matchId) return;
    try {
      //if (!lobbyState) setStatusMessage('Synchronizing Match Data...');
      const state: LobbyState = await getMatchLobbyState(matchId);

      //auth?.setUserTeam(state.allPlayers.find(
      setLobbyState(state);
      if (state.status === 'IN_LOBBY')
        {
          setStatusMessage('Waiting for host to start...');
          setTimerData({ startTime: null}); // Have to set this to null
        }
      else if (state.status === 'SCHEDULED') {
        setStatusMessage('Match Ready! Preparing countdown...');
      }

      if (state.status === 'SCHEDULED' && state.scheduledAt) {
        setTimerData({ startTime: new Date(state.scheduledAt).getTime()});
      }
    } catch (err: any) {
      setError(err.message || 'Could not find the match lobby.');
    } finally {
      setIsLoading(false);
    }
  }, [matchId]);

  const handleSwitchTeam = async(toTeam : number) => {

    if (!matchId || !auth?.user || !lobbyState) return;


    if (lobbyState.status !== 'IN_LOBBY') { return ; }

    try {

      await switchTeam(matchId,{toTeam:toTeam});
      // The socket event will handle navigation to arena
    } catch (err: any) {
      setError(err.message || 'Failed to switch team');
    }
  }
  const handleStartMatch = async () => {
    if (!matchId || !isHost) return;
    
    try {
      setStatusMessage('Starting match...');
      await startMatch(matchId);
      // The socket event will handle navigation to arena
    } catch (err: any) {
      setError(err.message || 'Failed to start match');
    }
  };

  useEffect(() => {
    if (cancellationReason) {
      const timer = setInterval(() => setRedirectCountdown(prev => (prev > 1 ? prev - 1 : 0)), 1000);
      const redirect = setTimeout(() => navigate('/home'), 5000);
      return () => { clearInterval(timer); clearTimeout(redirect); };
    }
  }, [cancellationReason, navigate]);

  useEffect(() => {
    if (!matchId) { setError("Match ID is missing."); setIsLoading(false); return; }
    fetchFullLobbyData();
    stompService.connect();

    const subs = [
      stompService.subscribeToMatchUpdates(matchId, (event: MatchEvent) => {
        switch (event.eventType) {
          case 'PLAYER_JOINED': fetchFullLobbyData(); break;
          case 'MATCH_START': navigate(`/match/arena/${matchId}`); break;
          case 'MATCH_CANCELED': setCancellationReason(event.reason || "The match was canceled."); break;
        }
      }),
      stompService.subscribeToCountdown(matchId, (event: any) => {
        if (event.eventType?.toUpperCase() === 'LOBBY_COUNTDOWN_STARTED') {
          setTimerData({ startTime: event.payload.startTime }); // Start timer without fetchFullLobbyData, if we do refresh then we have to fetch lobby data again
        }
      })
    ];
    return () => { 
      subs.forEach(sub => sub?.unsubscribe()); };
  }, [matchId, fetchFullLobbyData, navigate]);

  if (isLoading) {
    return (
      <LobbyStateLayout>
        <Loader2 className="animate-spin text-[#F97316]" size={48} />
        <p className="mt-4 text-lg animate-pulse text-gray-700 dark:text-gray-400">{statusMessage}</p>
      </LobbyStateLayout>
    );
  }

  if (cancellationReason) {
    return (
      <LobbyStateLayout>
        <Ban className="text-red-500" size={64} />
        <h1 className="mt-4 text-3xl font-bold text-gray-900 dark:text-white">Match Canceled</h1>
        <p className="mt-2 text-lg text-gray-600 dark:text-gray-400">{cancellationReason}</p>
        <p className="mt-6 text-sm text-gray-500">Redirecting to home in {redirectCountdown} seconds...</p>
      </LobbyStateLayout>
    );
  }
  
  if (error) {
   return (
      <LobbyStateLayout>
        <Ban className="text-yellow-500" size={64} />
        <h1 className="mt-4 text-3xl font-bold text-gray-900 dark:text-white">An Error Occurred</h1>
        <p className="mt-2 text-lg text-gray-600 dark:text-gray-400">{error}</p>
      </LobbyStateLayout>
    );
  }
  
  if (!lobbyState) return null;

  return (
<MainLayout>
  <div className="container mx-auto max-w-6xl">
    <div className="text-center mb-10">
      {timerData.startTime ? (
        isFinished ? (
          <>
            <p className="text-2xl font-semibold animate-pulse text-white">Finalizing Match...</p>
            <div className="my-3 flex justify-center items-center h-[96px]">
              <Loader2 className="animate-spin text-orange-500" size={72} />
            </div>
            <p className="text-gray-400 mt-2">Preparing the arena...</p>
            <p className="text-gray-400 mt-2">Scheduler runs every 10 seconds, so please wait</p>
          </>
        ) : (
          <>
            <p className="text-2xl font-semibold text-orange-500">Match Starting In</p>
            <h1 className="text-5xl font-bold text-white mt-2">
              {String(minutes).padStart(2, '0')}:{String(seconds).padStart(2, '0')}
            </h1>
            <p className="text-gray-400 mt-3">The arena will open automatically.</p>
          </>
        )
      ) : (
        <>
          <div className="relative inline-block mb-6">
            <div className="absolute -inset-2 bg-orange-500/20 rounded-full blur-md"></div>
            <Swords className="text-orange-500 relative mx-auto text-6xl mb-2" />
          </div>
          <h1 className="text-4xl font-bold text-white mb-3">{statusMessage}</h1>
          <p className="text-gray-400">
            {lobbyState.status === 'IN_LOBBY'
              ? `Players: ${lobbyState.allPlayers?.length || 0}/${lobbyState.maxPlayers}`
              : 'Please wait.'}
          </p>
          
          {/* Start Match Button for Host */}
          {isHost && lobbyState.status === 'IN_LOBBY' && (
            <div className="mt-8">
              <button
                onClick={handleStartMatch}
                className="px-10 py-4 bg-orange-500 hover:bg-orange-600 text-white font-bold text-lg rounded-lg transition-all duration-300 transform hover:scale-105 active:scale-95 shadow-lg hover:shadow-orange-500/25 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <div className="flex items-center gap-3">
                  <Play size={22} />
                  <span>Start Match</span>
                </div>
              </button>
            </div>
          )}
        </>
      )}
    </div>
    
    {/* 4-Team Grid Layout */}
    <div className="grid grid-cols-2 gap-8">
      {[0, 1, 2, 3].map((teamIndex) => (
        <div 
          key={teamIndex} 
          className={`
            p-6 rounded-xl border-2 bg-black/50 backdrop-blur-sm
            ${teamIndex === 0 ? 'border-blue-500 hover:border-blue-400' : ''}
            ${teamIndex === 1 ? 'border-red-500 hover:border-red-400' : ''}
            ${teamIndex === 2 ? 'border-green-500 hover:border-green-400' : ''}
            ${teamIndex === 3 ? 'border-purple-500 hover:border-purple-400' : ''}
            transition-all duration-300 hover:shadow-xl cursor-pointer
          `} 
          onClick={() => handleSwitchTeam(teamIndex)}
        >
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-3">
              <h2 className="text-2xl font-bold text-white">
                Team {teamIndex + 1}
              </h2>
              {lobbyState.hostId && teamPlayers[teamIndex]?.some(p => p.player_id === lobbyState.hostId) && (
                <span className="text-xs font-semibold px-3 py-1 bg-yellow-500 text-white rounded-full">
                  Host
                </span>
              )}
            </div>
            <span className="text-sm font-semibold px-4 py-2 rounded-full bg-gray-800 text-white border border-gray-700">
              {teamPlayers[teamIndex]?.length || 0} players
            </span>
          </div>
          
          <div className="space-y-4">
            {teamPlayers[teamIndex]?.length > 0 ? (
              teamPlayers[teamIndex].map((player, iIndex) => (
                <div 
                  key={iIndex} 
                  className="flex items-center justify-between p-4 bg-gray-900/50 rounded-lg border border-gray-700 hover:border-gray-600 transition-colors"
                >
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 rounded-full bg-gradient-to-r from-orange-500 to-red-500 flex items-center justify-center text-white font-bold text-lg">
                      {player.player_username?.charAt(0).toUpperCase() || 'P'}
                    </div>
                    <span className="font-medium text-white">
                      {player.player_username}
                    </span>
                  </div>
                  {player.player_id === lobbyState.hostId && (
                    <span className="text-xs font-semibold px-3 py-1.5 bg-yellow-500/20 text-yellow-300 border border-yellow-500/30 rounded">
                      Host
                    </span>
                  )}
                </div>
              ))
            ) : (
              <div className="text-center p-8 border-2 border-dashed border-gray-700 rounded-lg bg-gray-900/30">
                <div className="w-12 h-12 mx-auto mb-3 flex items-center justify-center bg-gray-800 rounded-full">
                  <svg className="w-6 h-6 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M12 4v16m8-8H4" />
                  </svg>
                </div>
                <p className="text-gray-500">Waiting for players...</p>
              </div>
            )}
          </div>
        </div>
      ))}
    </div>
  </div>
</MainLayout>
  );
};

export default MatchLobbyPage;
