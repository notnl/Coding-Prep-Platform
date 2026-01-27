import type { LobbyDetails, Page, PastMatch, SwitchTeamRequest, SwitchTeamResponse } from '../types/match';
import { api_get,api_post } from 'src/core/hooks/useAPIRequest';



import type {
  CreateMatchRequest,
  CreateMatchResponse,
  JoinMatchRequest,
  JoinMatchResponse,
  LobbyState,
  UserStats,
  ArenaData,
  MatchResult,
  MatchDiscussion
} from '../types/match';

const API_BASE_URL = '/api/v1/match';
const API_STATS_URL = '/stats';



export const createMatch = async (requestData: CreateMatchRequest): Promise<CreateMatchResponse> => {
    return (await (api_post(`${API_BASE_URL}/create`,JSON.stringify(requestData)))).json() 

};



export const joinMatch = async (requestData: JoinMatchRequest): Promise<JoinMatchResponse> => {
    return (await (api_post(`${API_BASE_URL}/join`,JSON.stringify(requestData)))).json() 
};

export const startMatch = async (matchId: string): Promise<Boolean> => {
    return (await (api_post(`${API_BASE_URL}/lobby/${matchId}/start`,''))).json() 
};

export const switchTeam = async (matchId: string, requestData: SwitchTeamRequest): Promise<String> => {
    //return Promise.resolve(true)
    return (await (api_post(`${API_BASE_URL}/lobby/${matchId}/switch`,JSON.stringify(requestData)))).text()
};

export const getAllLobby = async () : Promise<LobbyDetails[]> => {
   return (await api_get(`${API_BASE_URL}/alllobby`)).json();

};

export const getPointsForMatch = async (matchId: string) : Promise<number[]> => {
   return (await api_get(`${API_BASE_URL}/${matchId}/points`)).json();
};

//
export const getMatchLobbyState = async (matchId: string) : Promise<LobbyState> => {
   return (await api_get(`${API_BASE_URL}/lobby/${matchId}`)).json();

};

export const getArenaData = async (matchId: string) : Promise<ArenaData> => {
   return (await api_get(`${API_BASE_URL}/${matchId}`)).json();
};

export const hostStartDiscussion = async (matchId: string): Promise<boolean> => {
    return (await (api_post(`${API_BASE_URL}/${matchId}/startdiscussion`,''))).ok
};

export const hostNextProblem = async (matchId: string): Promise<string> => {
    return (await (api_post(`${API_BASE_URL}/${matchId}/nextproblem`,''))).text()
};

export const hostEndMatch = async (matchId: string): Promise<boolean> => {
    return (await (api_post(`${API_BASE_URL}/${matchId}/end`,''))).ok
};
export const getDiscussionData = async (matchId: string) : Promise<MatchDiscussion> => {

   return (await api_get(`${API_BASE_URL}/${matchId}/discussion`)).json();
};


//
//
//export const getArenaData = async (matchId: string): Promise<ArenaData> => {
//    try {
//        const response = await api.get<ArenaData>(`${API_BASE_URL}/${matchId}`);
//        return response.data;
//    } catch (error) {
//        console.error(`Failed to fetch arena data for match ${matchId}`, error);
//        throw new Error("Could not load match data. It might have expired or is invalid.");
//    }
//};
//
//
//export const getMatchResult = async (matchId: string): Promise<MatchResult> => {
//  const response = await api.get<MatchResult>(`${API_BASE_URL}/${matchId}/results`);
//  return response.data;
//};
//
//
//export const getPlayerStats = async (userId: number): Promise<UserStats> => {
//    const response = await api.get<UserStats>(`${API_STATS_URL}/${userId}`);
//    return response.data;
//};
//
//
//
//
//export const getMatchHistory = async (params: { page: number, size: number, result?: string }): Promise<Page<PastMatch>> => {
//    const response = await api.get<Page<PastMatch>>(`${API_BASE_URL}/history`, {
//        params: params
//    });
//    return response.data;
//};
//
//
//export const getCurrentUserStats = async (): Promise<UserStats> => {
//    const response = await api.get<UserStats>(`${API_STATS_URL}/me`);
//    return response.data;
//};
