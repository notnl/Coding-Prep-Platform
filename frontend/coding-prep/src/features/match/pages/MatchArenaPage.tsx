import React, { useState, useCallback, useEffect, useRef, useContext, act, type ReactNode } from 'react';
import { useParams, useNavigate } from 'react-router';
import { Loader2 } from 'lucide-react';
import { Panel, Group, Separator } from 'react-resizable-panels';
import MatchHeader from '../components/MatchHeader';
import { MatchResultOverlay } from '../components/MatchResultOverlay';
import ProblemDetails from '../../problem/components/ProblemDetails';
import CodeEditor from '../../problem/components/CodeEditor';
import SubmissionsList from '../../problem/components/SubmissionsList';
import SubmissionDetailModal from '../../problem/SubmissionDetailModal';
import Tabs from '../../../core/components/Tabs';
import { createSubmission, getSubmissionDetails ,getAllSubmissionRefresh} from '../../problem/services/problemService';
import { getArenaData,getDiscussionData,getMatchLobbyState, hostEndMatch, hostNextProblem, hostStartDiscussion } from '../services/matchService';
import { stompService } from '../../../core/sockets/stompClient';
import type { ArenaData, LobbyState, MatchDiscussion, MatchResult, PlayerDiscussionIdentification } from '../types/match';
import type { SubmissionSummary, SubmissionDetails } from '../../problem/types/problem';
import { useServerTimer } from '../../../core/components/useServerTimer';
import Navbar from '../../../components/layout/Navbar';
import { AuthContext, type AuthContextType } from 'src/core/context/AuthContext';
import MatchDiscussionOverlay from '../components/MatchDiscussionOverlay';




const useArenaData = (matchId: string | undefined) => {
    const [arenaData, setArenaData] = useState<ArenaData | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [shouldRedirect, setShouldRedirect] = useState(false);
    const [shouldRefresh, setShouldRefresh] = useState(false);

    useEffect(() => {
        if (!matchId) {
            setError("No Match ID provided.");
            setIsLoading(false);
            return;
        }
        const fetchDetails = async () => {
            try {
                const data = await getArenaData(matchId);
                setArenaData(data);

                 
            } catch (err: any) {
                if (err.response?.status === 409) {

                    setShouldRedirect(true);
                } else {
                    setError(err.message || "Failed to load match data.");
                }
            } finally {
                setIsLoading(false);
            }
        };
        fetchDetails();
    }, [matchId,shouldRefresh]);

    return { arenaData, isLoading, error, shouldRedirect,setShouldRefresh };
};


const useMatchEvents = (
    matchId: string | undefined,
    onMatchEnd: () => void,
    onMatchDiscussion: () => void,
    onMatchProgress: () => void,
    onMatchNextProblem: () => void,
    onCountdownStart: (data: { startTime: number; duration: number }) => void
) => {
    const handleMatchEvent = useCallback((event: any) => {
        if (event.eventType === 'MATCH_END') {
            onMatchEnd();
        } else if ( event.eventType === 'MATCH_DISCUSSION') {
            onMatchDiscussion()
        }else if ( event.eventType === 'MATCH_PROGRESS'){
            onMatchProgress();  //Reset to match progress state
        }else if ( event.eventType === 'MATCH_NEXTPROBLEM') {
            onMatchNextProblem();
        }
    }, [onMatchEnd]);



    const handleCountdownEvent = useCallback((event: any) => {
        if (event.eventType === 'MATCH_COUNTDOWN_STARTED') {
            onCountdownStart(event.payload);
        }
    }, [onCountdownStart]);

    useEffect(() => {
        if (!matchId) return;
        stompService.connect();

        const subs = [
            stompService.subscribeToMatchUpdates(matchId, handleMatchEvent),
            stompService.subscribeToCountdown(matchId, handleCountdownEvent),
        ];
        return () => {
            subs.forEach(sub => { if (sub) sub.unsubscribe() });
        };
    }, [matchId, handleMatchEvent, handleCountdownEvent]);
};



const ArenaStateLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => (
    <div className="flex flex-col h-screen bg-white dark:bg-zinc-950">
        <main className="flex-grow flex items-center justify-center text-center p-4">
            {children}
        </main>
    </div>
);


type TabType = {
    label: string,
    content: ReactNode
}

const MatchArenaPage: React.FC = () => {
    const { matchId } = useParams<{ matchId: string }>();

    const navigate = useNavigate();
    const  auth  = useContext<AuthContextType | undefined>(AuthContext)
    const [currentProblem, setCurrentProblem] = useState(0)

    const { arenaData, isLoading, error, shouldRedirect,setShouldRefresh } = useArenaData(matchId);
    const [timerData, setTimerData] = useState<{ startTime: number; duration: number } | null>(null);
    const [matchState, setMatchState] = useState<'LOADING' | 'IN_PROGRESS' | 'DISCUSSION'| 'AWAITING_RESULT' | 'COMPLETED'>('LOADING');
    const [teamCode, setTeamCode] = useState<MatchDiscussion>({allCode:[]}); 
    const [matchResult, setMatchResult] = useState<MatchResult | null>(null);
    const [language, setLanguage] = useState<'cpp' | 'java' | 'python'>('cpp');
    const [code, setCode] = useState<string[] >(); 
    const [submissions, setSubmissions] = useState<SubmissionSummary[]>([]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [selectedSubmission, setSelectedSubmission] = useState<SubmissionDetails | null>(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [activeTab, setActiveTab] = useState(0);
    const [activeVisualTab, setActiveVisualTab] = useState(0);
    const [allTab, setAllTab] = useState<TabType[]>()
    const [submissionTab, setSubmissionTab] = useState(0);
    const COOLDOWN_SECONDS = 5;
    const [isCoolingDown, setIsCoolingDown] = useState(false);
    const [cooldownTime, setCooldownTime] = useState(COOLDOWN_SECONDS);
    const cooldownIntervalRef = useRef<number | null>(null);

    //Check host
  const isHost = React.useMemo(() => {

    return arenaData?.hostId === auth?.user?.id;
  }, [arenaData]);

    useEffect(() => {

        if (code == undefined || !arenaData) { 
            return
        }

        if (matchId && auth?.user?.username) {
            try {
                const storageKey = `code-cache-${auth?.user?.username}-${matchId}-${activeTab}`;
                localStorage.setItem(storageKey, code[activeTab]);
            } catch (e) {
            }
        }
    }, [code, matchId, auth?.user?.username, arenaData?.currentProblem]);

    useEffect ( () => {

        if (matchId && auth?.user?.username) {

            try {


                if (!arenaData) {
                        throw new Error('Arena Data does not exist');
                }

                if (!arenaData.problemDetails) {
                        throw new Error('Arena Problems does not exist');
                }

                
                const test : string[] | undefined = arenaData.problemDetails.map( () => { return ''} ) || undefined




                test.forEach((element,ind) => {

                    const storageKey = `code-cache-${auth?.user?.username}-${matchId}-${ind}`;
                    const savedCode = localStorage.getItem(storageKey) || '';

                    test[ind] = savedCode
                });
                
                setCode(test)

                
            } catch (e) {
                console.error("Could not read from localStorage:", e);
            }
        }
    }, [arenaData]) // if the problem details has been updated

    useEffect(() => {
        if (!arenaData) { 
            return
        }

            const problemTabs =  arenaData.problemDetails.filter(
                    (p,pIndex) => (pIndex <= currentProblem) //Filter up to our current problem
             ).map ( 
             
                    (p,pIndex) => {
                        
                        return ({ label: `${p.title}`, content: <ProblemDetails problem={p} /> }) 

                    }
                   )

            
            const leftPanelTabs  = [
                    ...problemTabs,
                { label: 'My Submissions', content: <SubmissionsList submissions={submissions} onSubmissionClick={handleSubmissionClick} /> },
            ];

            setAllTab(leftPanelTabs)

            setSubmissionTab(leftPanelTabs.length - 1) // Assume submission page is always the last 

    },[arenaData,arenaData?.problemDetails,currentProblem,submissions])


    useEffect(() => {

            //User does not belong to this match
        if (!matchId || !auth?.userStatus || auth?.userStatus.in_match !== matchId) { 
            navigate(`/home`, { replace: true });
        }




        if (shouldRedirect && matchId) {
            navigate(`/match/results/${matchId}`, { replace: true });
        }
    }, [shouldRedirect, matchId,auth?.userStatus?.in_match, navigate]);

    useMatchEvents(matchId,
        () => {
            setMatchState('COMPLETED');
        },
        () => { 
            setMatchState('DISCUSSION');
            getDiscussionCode()
        },
        () => {
            setMatchState('IN_PROGRESS'); // Just reset back to in progress state
        },() => { 
            setCurrentProblem(prev => prev+1) // next problem
        },
        (payload) => {
            setTimerData(payload);
        }
    );


    const getDiscussionCode = async () => { 
        if (!matchId) { 
            return
        } 

        const allD : MatchDiscussion = await getDiscussionData(matchId);
        setTeamCode(allD)
                
        
    }
    
    const getSubmissionsRefresh = async () => { 

        if (submissions.length != 0 ) { // there is no need to fetch if we have a submission, it indicates that we didn't exit this page , and if populated, we don't need to fetch 
            return
        }

        if (!matchId) { 
            return
        } 

        const allD : SubmissionDetails[] = await getAllSubmissionRefresh(matchId);
        const mapSumDetails : SubmissionSummary[]  = allD.map( 
                (i,ind) => {
                    return ({
                id: i.id, status: i.status, language, runtimeMs: null,
                createdAt: new Date().toISOString(),
                matchId: matchId ?? null
            }  )
                }
            )

        setSubmissions(mapSumDetails) 

                
        
    }
    

    useEffect(() => {
        if (arenaData) {

            switch (arenaData.matchStatus){
                case 'IN_PROGRESS':
                     setMatchState('IN_PROGRESS');
                    break;
                case 'DISCUSSION':
                     setMatchState('DISCUSSION');
                     getDiscussionCode()
                    //Fetch all code
                    break;
                case 'COMPLETED':
                     setMatchState('COMPLETED');
                    break;
            }

            setCurrentProblem(arenaData.currentProblem)



             
            if (arenaData.startedAt && arenaData.durationInMinutes > 0) {
                setTimerData({
                    startTime: new Date(arenaData.startedAt).getTime(),
                    duration: arenaData.durationInMinutes * 60 *1000,
                });
            }
        }
    }, [arenaData]);

    const { totalSeconds: timeLeft, isFinished } = useServerTimer(timerData?.startTime ?? null, timerData?.duration ?? null);

    useEffect(() => {
        //switch (matchState){
        //    case 'DISCUSSION':
        //        

        //}

        if (isFinished && matchState === 'IN_PROGRESS' && timerData) {
            setMatchState('COMPLETED');
        }

    }, [isFinished, matchState]);
    useEffect(() => {
        if (matchState === 'COMPLETED' && matchId) {
            const timer = setTimeout(() => {
                navigate(`/match/results/${matchId}`);
            }, 4000);
            return () => clearTimeout(timer);
        }
    }, [matchState, matchId, navigate]);

    useEffect(() => {
        if (isCoolingDown) {
            cooldownIntervalRef.current = window.setInterval(() => {
                setCooldownTime(prevTime => {
                    if (prevTime <= 1) {
                        if (cooldownIntervalRef.current) {
                            clearInterval(cooldownIntervalRef.current);
                        }
                        setIsCoolingDown(false);
                        return COOLDOWN_SECONDS;
                    }
                    return prevTime - 1;
                });
            }, 1000);
        }
        return () => {
            if (cooldownIntervalRef.current) {
                clearInterval(cooldownIntervalRef.current);
            }
        };
    }, [isCoolingDown]);


    const handleSubmissionClick = async (submissionId: string) => {
        try {
            const details = await getSubmissionDetails(submissionId);
            setSelectedSubmission(details);
            setIsModalOpen(true);
        } catch (err) {
            console.error("Failed to fetch submission details:", err);
        }
    };

    const handleSubmissionUpdate = useCallback((update: { submissionId: string; status: string }) => {
        setSubmissions(prev => prev.map(sub => sub.id === update.submissionId ? { ...sub, status: update.status } : sub));
    }, []);


    const handleHostStartDiscussion = async () => {

        if (!arenaData || !arenaData.matchId) { 

            return
        }
        const startDiscussionResults : boolean = await hostStartDiscussion(arenaData.matchId)

        if (!startDiscussionResults) {
            console.log("Failed to start discusison phase!")
        }
        

    }

    const handleHostNextProblem = async () => {

        if (!arenaData || !arenaData.matchId) { 

            return
        }
        const hostNextRes : number = Number(await hostNextProblem(arenaData.matchId))

        
        if (hostNextRes <= 0) {
            console.log("Failed to go next problem, could have exceeded maximum")
            return
        }

        //setCurrentProblem(hostNextRes) // host will get updated by the event instead
        

    }

    const handleHostEndMatch = async () => {

        if (!arenaData || !arenaData.matchId) { 

            return
        }
        const startDiscussionResults : boolean = await hostEndMatch(arenaData.matchId)

        if (!startDiscussionResults) {
            console.log("Failed to end match")
        }
        

    }

    const handleSubmit = async (tabToSubmit : number) => {
        if (!arenaData?.problemDetails || isSubmitting || isCoolingDown || matchState !== 'IN_PROGRESS' || tabToSubmit == submissionTab || tabToSubmit != currentProblem || !auth?.userStatus) return;
        setIsSubmitting(true);
        

        setActiveVisualTab(submissionTab);

        if (!code || !matchId) { return }

        try {

            const response = await createSubmission({
                problemId: arenaData.problemDetails[tabToSubmit].id, // 
                language:language, code:code[tabToSubmit], 
                matchId: matchId
            });
            const newSubmission: SubmissionSummary = {
                id: response.submissionId, status: 'PENDING', language, runtimeMs: null,
                createdAt: new Date().toISOString(),
                matchId: matchId ?? null
            };
            setSubmissions(prev => [newSubmission, ...prev]);
            stompService.subscribeToSubmissionResult(response.submissionId, handleSubmissionUpdate);
        } catch (err) {
            console.error("Submission failed:", err);
        } finally {
            setIsSubmitting(false);
            setIsCoolingDown(true);
        }
    };


    
    if (isLoading) return (
        <ArenaStateLayout>
            <div>
                <Loader2 className="animate-spin text-[#F97316] mx-auto" size={48} />
                <p className="mt-4 text-lg animate-pulse text-gray-600 dark:text-gray-400">Loading Arena...</p>
            </div>
        </ArenaStateLayout>
    );

    if (shouldRedirect) return (
        <ArenaStateLayout>
            <p className="text-lg text-gray-800 dark:text-gray-200">Match already completed. Redirecting...</p>
        </ArenaStateLayout>
    );
    
    if (error){ 

        navigate(`/home`, { replace: true })

        return (


        <ArenaStateLayout>
            <p className="text-red-500 text-xl">{error}</p>
        </ArenaStateLayout>
    );
    
    }
    if (!arenaData?.problemDetails) return (
        <ArenaStateLayout>
            <p className="text-lg text-gray-800 dark:text-gray-200">Match data or problem could not be found.</p>
        </ArenaStateLayout>
    );


    const isEditorDisabled = matchState !== 'IN_PROGRESS' || isSubmitting || isCoolingDown || activeVisualTab != currentProblem;


    //const problemTabs = problemDetails.filter( 
    //            (p,pIndex) => 
    //            { 
    //                if (pIndex == currentProblem) {
    //                    return ({ label: `Problem ${pIndex} Description`, content: <ProblemDetails problem={p} /> }) 
    //                }else {


    //                
    //            }

    //)
    //const problemTabs =  { label: `Problem ${currentProblem} Description`, content: <ProblemDetails problem={problemDetails[currentProblem]} /> } 

    
    

    let submitButtonText = "Submit";
    if (isSubmitting) submitButtonText = "Submitting...";
    else if (isCoolingDown) submitButtonText = `Wait ${cooldownTime}s`;

    return (
        <>
            <div className="flex flex-col h-screen bg-white dark:bg-zinc-950 text-gray-900 dark:text-white">

                { isHost && (

                    <>
                    <button 
                        onClick={handleHostStartDiscussion}
                        className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white font-semibold rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50 disabled:bg-gray-500 disabled:cursor-not-allowed transition-colors"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="w-5 h-5">
                          <path d="M9.309 3.14c-.094-.323-.527-.323-.62 0L4.363 8.32c-.2.685.028 1.488.544 1.838l3.187 2.126a2.25 2.25 0 0 0 2.373 0l3.187-2.126c.516-.35.744-1.153.544-1.838L9.309 3.14Z" />
                          <path fillRule="evenodd" d="M11.664 12.84a2.25 2.25 0 0 1-3.328 0 2.25 2.25 0 0 0-2.373 0l-3.187 2.126c-.516.35-.744 1.153-.544 1.838L4.363 17.68c.094.323.527.323.62 0l4.326-5.18a2.25 2.25 0 0 1 2.373 0l4.326 5.18c.094.323.527.323.62 0l1.414-4.949c.2-.685-.028-1.488-.544-1.838L11.664 12.84Z" clipRule="evenodd" />
                        </svg>
                        {"Start discussion(host)"}
                    </button>

                    <button 
                        onClick={handleHostNextProblem}
                        className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white font-semibold rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50 disabled:bg-gray-500 disabled:cursor-not-allowed transition-colors"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="w-5 h-5">
                          <path d="M9.309 3.14c-.094-.323-.527-.323-.62 0L4.363 8.32c-.2.685.028 1.488.544 1.838l3.187 2.126a2.25 2.25 0 0 0 2.373 0l3.187-2.126c.516-.35.744-1.153.544-1.838L9.309 3.14Z" />
                          <path fillRule="evenodd" d="M11.664 12.84a2.25 2.25 0 0 1-3.328 0 2.25 2.25 0 0 0-2.373 0l-3.187 2.126c-.516.35-.744 1.153-.544 1.838L4.363 17.68c.094.323.527.323.62 0l4.326-5.18a2.25 2.25 0 0 1 2.373 0l4.326 5.18c.094.323.527.323.62 0l1.414-4.949c.2-.685-.028-1.488-.544-1.838L11.664 12.84Z" clipRule="evenodd" />
                        </svg>
                        {"Next Problem"}
                    </button>

                    <button 
                        onClick={handleHostEndMatch}
                        className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white font-semibold rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50 disabled:bg-gray-500 disabled:cursor-not-allowed transition-colors"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="w-5 h-5">
                          <path d="M9.309 3.14c-.094-.323-.527-.323-.62 0L4.363 8.32c-.2.685.028 1.488.544 1.838l3.187 2.126a2.25 2.25 0 0 0 2.373 0l3.187-2.126c.516-.35.744-1.153.544-1.838L9.309 3.14Z" />
                          <path fillRule="evenodd" d="M11.664 12.84a2.25 2.25 0 0 1-3.328 0 2.25 2.25 0 0 0-2.373 0l-3.187 2.126c-.516.35-.744 1.153-.544 1.838L4.363 17.68c.094.323.527.323.62 0l4.326-5.18a2.25 2.25 0 0 1 2.373 0l4.326 5.18c.094.323.527.323.62 0l1.414-4.949c.2-.685-.028-1.488-.544-1.838L11.664 12.84Z" clipRule="evenodd" />
                        </svg>
                        {"End Match"}
                    </button>

                    </>
                )
                }


                <div className="flex-grow flex flex-col min-h-0">
                    <MatchHeader
                        timeLeft={timeLeft}
                        status={matchState}
                    />

                    { matchState === 'DISCUSSION' && 
                            (<MatchDiscussionOverlay allCode={teamCode} isHost={isHost} endDiscussion={handleHostStartDiscussion} />)

                    }

                    <Group className="flex-grow overflow-hidden">
                        <Panel defaultSize={50} minSize={25} className="flex flex-col bg-gray-50 dark:bg-zinc-900">
                        { allTab && <Tabs tabs={allTab} activeTab={activeVisualTab} setActiveTab={
                                    (c) => {
                                        if (c != submissionTab){ //Submission tab will always be the last index, this is to only submit the active tab index 
                                              setActiveTab(c)
                                        }else { 
                                            //Fetch all our submissions , this will only be called once so its fine if we do this
                                            getSubmissionsRefresh()
                                        }

                                        setActiveVisualTab(c)

                                    }} />
                        }
                        </Panel>
                        <Separator className="w-2 bg-gray-300 dark:bg-zinc-800 hover:bg-[#F97316]/80 active:bg-[#F97316] transition-colors duration-200" />
                        <Panel defaultSize={50} minSize={35} className="flex flex-col bg-gray-50 dark:bg-zinc-900">
                            <CodeEditor
                                language={language} setLanguage={setLanguage}
                                code={code ? code[activeTab] : "Good luck!"} 
                                tabIndex={activeTab}
                                setCode={( activeTab, curCode) =>  {  //upon onChange editor, we will change the code for that index, which will save to localStorage

                        setCode(code?.map((c,i) => {  
                                if (i == activeTab) {
                                    return curCode
                                }else { 
                                    return c
                                }

                        }) );
                                }}
                                onSubmit={() => { handleSubmit(activeTab) } } 
                                isSubmittingDisabled={isEditorDisabled}
                                submitButtonText={submitButtonText}
                            />
                        </Panel>
                    </Group>
                </div>

            </div>

            {matchState === 'COMPLETED' && (
                <div className="absolute inset-0 bg-black/80 backdrop-blur-sm flex flex-col items-center justify-center z-50 transition-opacity duration-300">
                    <Loader2 className="animate-spin text-[#F97316]" size={64} />
                    <h2 className="text-4xl font-bold text-white mt-8">Match's over</h2>
                    <p className="text-xl text-gray-400 mt-2 animate-pulse">Redirecting you to see the results</p>
                </div>
            )}
            

            {isModalOpen && (
                <SubmissionDetailModal submission={selectedSubmission} onClose={() => setIsModalOpen(false)} />
            )}
        </>
    );
};

export default MatchArenaPage;

