import type { PlayerDiscussionIdentification } from "../types/match";

export type CodeFeedbackType = {
  curCode: PlayerDiscussionIdentification;
};

export default function CodeFeedback({ curCode }: CodeFeedbackType) {
  return (
    <div className="flex flex-col h-full">
      {/* Team Support Card */}
      <div className="mb-6 rounded-xl bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-gray-800 dark:to-gray-900 p-5 shadow-sm border border-blue-100 dark:border-gray-700">
        <div className="flex items-center gap-3 mb-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100 dark:bg-blue-900">
            <svg className="h-5 w-5 text-blue-600 dark:text-blue-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
          </div>
          <h3 className="text-lg font-semibold text-gray-800 dark:text-gray-200">
            Team Support Opportunity
          </h3>
        </div>
        
        <div className="space-y-3">
          <p className="flex items-start gap-2 text-gray-700 dark:text-gray-300">
            <span className="mt-0.5 flex h-5 w-5 flex-shrink-0 items-center justify-center rounded-full bg-blue-100 dark:bg-blue-900 text-blue-600 dark:text-blue-300 text-xs">1</span>
            <span>Talk to <span className="font-semibold text-blue-600 dark:text-blue-400">{curCode.playerName}</span> and help them out!</span>
          </p>
          
          <p className="flex items-start gap-2 text-gray-700 dark:text-gray-300">
            <span className="mt-0.5 flex h-5 w-5 flex-shrink-0 items-center justify-center rounded-full bg-green-100 dark:bg-green-900 text-green-600 dark:text-green-300 text-xs">2</span>
            <span>This is your chance to earn <span className="font-bold text-green-600 dark:text-green-400">100 points</span> for your team!</span>
          </p>
          
          <p className="flex items-start gap-2 text-gray-700 dark:text-gray-300">
            <span className="mt-0.5 flex h-5 w-5 flex-shrink-0 items-center justify-center rounded-full bg-amber-100 dark:bg-amber-900 text-amber-600 dark:text-amber-300 text-xs">3</span>
            <span>This shows {curCode.playerName}'s latest submission. If blank, they haven't submitted yet.</span>
          </p>
        </div>
      </div>

      {/* Code Display */}
      <div className="flex-grow flex flex-col bg-white dark:bg-gray-900 rounded-xl border border-gray-200 dark:border-gray-700 overflow-hidden shadow-sm">
        {/* Code Header */}
        <div className="flex items-center justify-between px-4 py-3 bg-gray-50 dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
          <div className="flex items-center gap-2">
            <div className="h-3 w-3 rounded-full bg-red-500"></div>
            <div className="h-3 w-3 rounded-full bg-yellow-500"></div>
            <div className="h-3 w-3 rounded-full bg-green-500"></div>
            <span className="ml-2 text-sm font-medium text-gray-700 dark:text-gray-300">
              {curCode.playerName}'s Code
            </span>
          </div>
        </div>
        
        {/* Code Content */}
        <div className="flex-grow p-4 overflow-auto">
          <pre className="bg-gray-50 dark:bg-gray-900 text-gray-800 dark:text-gray-200 rounded-lg p-4 border border-gray-200 dark:border-gray-700 text-sm font-mono whitespace-pre-wrap">
            {curCode.playerCode.trim() || 
              `// ${curCode.playerName} hasn't submitted any code yet.
// When they do, it will appear here.`}
          </pre>
        </div>
      </div>
    </div>
  );
}
