
import { type TeamScore } from "../types/match" 

type LeaderboardComponentProps = {
    allTeamScore : TeamScore[]
}

export default function LeaderboardComponent ( {allTeamScore} : LeaderboardComponentProps )  { // Assume already sorted


return (
  <div className="h-full overflow-y-auto p-4 md:p-6">
    <div className="space-y-3">
      {allTeamScore.map((team, index) => {
        const isTopThree = index < 3;
        const rankColors = [
          "from-yellow-500 to-yellow-300", // 1st
          "from-gray-400 to-gray-300",     // 2nd
          "from-amber-700 to-amber-500",   // 3rd
        ];
        
        return (
          <div 
            key={`team-${team.teamID || index}`}
            className="group relative overflow-hidden rounded-xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-4 shadow-sm transition-all duration-300 hover:shadow-md hover:-translate-y-0.5 hover:border-blue-200 dark:hover:border-blue-800"
          >
            {/* Background highlight for top teams */}
            {isTopThree && (
              <div className={`absolute inset-0 opacity-5 bg-gradient-to-r ${rankColors[index]}`} />
            )}
            
            <div className="relative flex items-center justify-between">
              <div className="flex items-center space-x-3">
                {/* Rank badge with gradient for top 3 */}
                <div className={`flex-shrink-0 w-8 h-8 rounded-lg flex items-center justify-center ${
                  isTopThree 
                    ? `bg-gradient-to-br ${rankColors[index]} text-white font-bold`
                    : "bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300"
                }`}>
                  <span className="text-sm font-semibold">#{index + 1}</span>
                </div>
                
                <div className="flex flex-col">
                  <span className="text-base font-semibold text-gray-900 dark:text-gray-100">
                    Team {team.teamID}
                  </span>
                  <span className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                    {index === 0 &&  "🏆 Leader" }
                  </span>
                </div>
              </div>
              
              {/* Score badge with animation */}
              <div className="flex items-center space-x-2">
                <span className="text-sm font-medium text-gray-600 dark:text-gray-300">
                  Score:
                </span>
                <span className="inline-flex items-center rounded-full bg-gradient-to-r from-blue-500 to-blue-600 dark:from-blue-600 dark:to-blue-700 px-3 py-1 text-sm font-semibold text-white shadow-sm transition-transform group-hover:scale-105">
                  {team.teamScore}
                  <span className="ml-1 text-xs font-normal opacity-90">pts</span>
                </span>
              </div>
            </div>
          </div>
        );
      })}
    </div>
  </div>
);
}
