import type { ProblemDetail } from '../../problem/types/problem';


export interface CreateMatchRequest {
  difficultyMin: number;
  difficultyMax: number;
  startDelayInSecond: number;
  durationInMinutes: number;
}
export interface CreateMatchResponse {
  matchId: string;
  roomCode: string;
  shareableLink: string;
}
export interface JoinMatchRequest {
    matchId: string;
}
export interface JoinMatchResponse {
  matchId: string;
}

export interface SwitchTeamRequest {
    toTeam: number;
}
export interface SwitchTeamResponse {
  matchId: string;
  scheduledAt: string;
}

export interface LobbyState {
  matchId: string;
  allPlayers: Player[];
  status: 'IN_LOBBY' | 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED';
  scheduledAt: string | null;
  durationInMinutes: number;
  hostId: number;
  maxPlayers: number;

}

export interface LobbyDetails {

  matchId: string;
  roomCode: string; // we will treat this as its name

}
export interface UserStats {
  userId: number;
  duelsPlayed: number;
  duelsWon: number;
  duelsLost: number;
  duelsDrawn: number;
}
export interface UserStatus { 
  in_match: string;
  in_match_team: number;
}
export interface Player {
  player_id: number;
  player_username: string;
  player_team: number;
}


export interface LiveMatchState {
    matchId: string;
    startedAt: string;
    durationInMinutes: number;
}
export interface ArenaData {
    matchId: string;
    matchStatus : 'IN_LOBBY' | 'SCHEDULED' | 'IN_PROGRESS' | 'DISCUSSION'| 'COMPLETED';
    hostId: number;
    startedAt: string;
    durationInMinutes: number;
    problemDetails: ProblemDetail[];
    currentProblem: number;
    teamScore: number[]; 
}

export interface SubmissionSummaryInResult {
    submissionId: string;
    status: string;
    submittedAt: string;
    runtimeMs: number | null;
    memoryKb: number | null;
}

export interface PlayerResult {
    userId: number;
    solved: boolean;
    finishTime: string | null;
    penalties: number;
    effectiveTime: string | null;
    submissions: SubmissionSummaryInResult[];
}


export interface MatchResult {
    matchId: string;
    problemId: string;
    startedAt: string;
    endedAt: string;
    winnerId: number | null;
    outcome: string;
    winnerUsername: string | null;
    playerOne: PlayerResult & { username: string, score: number };
    playerTwo: PlayerResult & { username: string, score: number };
    winningSubmissionId: string | null;
}

export interface PlayerDiscussionIdentification {
     playerId: number;
     playerName: string; 
     playerCode: string;

}
export interface MatchDiscussion {
    allCode: PlayerDiscussionIdentification[]
}

export interface TeamScore {
    teamID : string
    teamScore : number
}

export interface PlayerJoinedPayload {
  eventType: 'PLAYER_JOINED';
  playerTwoId: number;
}
export interface MatchStartPayload {
    eventType: 'MATCH_START';
    liveState: LiveMatchState;
    playerOneUsername: string;
    playerTwoUsername: string;
}
export interface MatchEndPayload {
    eventType: 'MATCH_END';
    result: MatchResult;
}
export interface MatchCanceledPayload {
    eventType: 'MATCH_CANCELED';
    reason: string;
}
export type MatchEvent =
    | PlayerJoinedPayload
    | MatchStartPayload
    | MatchEndPayload
    | MatchCanceledPayload;

export interface Page<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
    numberOfElements: number;
    empty: boolean;
}

export interface PastMatch {
    matchId: string;
    status: string;
    result: 'WIN' | 'LOSS' | 'DRAW' | 'CANCELED' | 'EXPIRED';
    opponentId: number | null;
    opponentUsername: string;
    problemId: string | null;
    problemTitle: string;
    endedAt: string | null;
    createdAt: string;
}
