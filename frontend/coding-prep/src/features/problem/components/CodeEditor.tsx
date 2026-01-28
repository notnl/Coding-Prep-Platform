import React, { useState } from 'react';
import Editor from '@monaco-editor/react';
import { useTheme } from '../../../core/context/ThemeContext';
import Timer from './Timer';

type Language = 'cpp' | 'java' | 'python';

interface CodeEditorProps {
    language: Language;
    setLanguage: (language: Language) => void;
    code: string;
    setCode: (tabIndex: number,codeText : string) => void;
    tabIndex: number
    codeTemplate : string;
    onSubmit: () => void;
    isSubmittingDisabled: boolean;
    submitButtonText?: string;
    timer?: {
        time: number;
        isActive: boolean;
        handleStart: () => void;
        handlePause: () => void;
        handleReset: () => void;
    };
}

const languageMap: { [key in Language]: string } = {
    cpp: 'cpp',
    java: 'java',
    python: 'python',
};

const CodeEditor: React.FC<CodeEditorProps> = ({ 
    language, 
    setLanguage, 
    code, 
    tabIndex,
    setCode, 
    codeTemplate,
    onSubmit, 
    isSubmittingDisabled, 
    submitButtonText = "Submit",
    timer 
}) => {
    const { theme, toggleTheme } = useTheme();
    const [isTemplateModalOpen, setIsTemplateModalOpen] = useState(false);
    const [editorTheme, setEditorTheme] = useState<'light' | 'vs-dark'>();

    const openTemplateModal = () => {
        setIsTemplateModalOpen(true);
    };

    const closeTemplateModal = () => {
        setIsTemplateModalOpen(false);
    };

    const applyTemplate = () => {
        setCode(tabIndex || 0, codeTemplate || '');
        closeTemplateModal();
    };

    const toggleEditorTheme = () => {
        setEditorTheme(prev => prev === 'light' ? 'vs-dark' : 'light');
    };

    return (
        <div className="flex flex-col h-full bg-white dark:bg-gray-900">
            <div className="flex items-center justify-between p-2 bg-gray-100 dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
                <div className="flex items-center gap-4">
                    <select
                        value={language}
                        onChange={(e) => setLanguage(e.target.value as Language)}
                        className="p-2 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 border border-gray-300 dark:border-gray-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    >
                        <option value="cpp">C++</option>
                        <option value="java">Java</option>
                        <option value="python">Python</option>
                    </select>
                    
                    <button
                        onClick={openTemplateModal}
                        className="flex items-center gap-2 px-3 py-2 bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-gray-200 font-medium rounded-md hover:bg-gray-300 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-gray-400 transition-colors"
                        title="View Code Template"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="w-5 h-5">
                            <path fillRule="evenodd" d="M5.5 3A2.5 2.5 0 0 0 3 5.5v2.879a2.5 2.5 0 0 0 .732 1.767l6.5 6.5a2.5 2.5 0 0 0 3.536 0l2.878-2.878a2.5 2.5 0 0 0 0-3.536l-6.5-6.5A2.5 2.5 0 0 0 8.38 3H5.5ZM6 7a1 1 0 1 0 0-2 1 1 0 0 0 0 2Z" clipRule="evenodd" />
                        </svg>
                        Template
                    </button>
                </div>
                
                <div className="flex items-center gap-4">
                    {/* Theme Toggle Button */}
                    <button
                        onClick={toggleEditorTheme}
                        className="flex items-center gap-2 px-3 py-2 bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-gray-200 font-medium rounded-md hover:bg-gray-300 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-gray-400 transition-colors"
                        title={`Switch to ${editorTheme === 'light' ? 'Dark' : 'Light'} Theme`}
                    >
                        {editorTheme === 'light' ? (
                            <>
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="w-5 h-5">
                                    <path d="M10 2a.75.75 0 0 1 .75.75v1.5a.75.75 0 0 1-1.5 0v-1.5A.75.75 0 0 1 10 2ZM10 15a.75.75 0 0 1 .75.75v1.5a.75.75 0 0 1-1.5 0v-1.5A.75.75 0 0 1 10 15ZM10 7a3 3 0 1 0 0 6 3 3 0 0 0 0-6ZM15.657 5.404a.75.75 0 1 0-1.06-1.06l-1.061 1.06a.75.75 0 0 0 1.06 1.06l1.06-1.06ZM6.464 14.596a.75.75 0 1 0-1.06-1.06l-1.06 1.06a.75.75 0 0 0 1.06 1.06l1.06-1.06ZM18 10a.75.75 0 0 1-.75.75h-1.5a.75.75 0 0 1 0-1.5h1.5A.75.75 0 0 1 18 10ZM5 10a.75.75 0 0 1-.75.75h-1.5a.75.75 0 0 1 0-1.5h1.5A.75.75 0 0 1 5 10ZM14.596 15.657a.75.75 0 0 0 1.06-1.06l-1.06-1.061a.75.75 0 1 0-1.06 1.06l1.06 1.06ZM5.404 6.464a.75.75 0 0 0 1.06-1.06l-1.06-1.06a.75.75 0 1 0-1.061 1.06l1.06 1.06Z" />
                                </svg>
                                Light
                            </>
                        ) : (
                            <>
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="w-5 h-5">
                                    <path fillRule="evenodd" d="M7.455 2.004a.75.75 0 0 1 .26.77 7 7 0 0 0 9.958 7.967.75.75 0 0 1 1.067.853A8.5 8.5 0 1 1 6.647 1.921a.75.75 0 0 1 .808.083Z" clipRule="evenodd" />
                                </svg>
                                Dark
                            </>
                        )}
                    </button>

                    {timer && (
                        <Timer 
                            time={timer.time}
                            isActive={timer.isActive}
                            onStart={timer.handleStart}
                            onPause={timer.handlePause}
                            onReset={timer.handleReset}
                        />
                    )}
                    
                    <button 
                        onClick={onSubmit}
                        disabled={isSubmittingDisabled}
                        className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white font-semibold rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50 disabled:bg-gray-500 disabled:cursor-not-allowed transition-colors"
                        title={isSubmittingDisabled ? 'Submissions Disabled' : 'Submit Solution'}
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="w-5 h-5">
                          <path d="M9.309 3.14c-.094-.323-.527-.323-.62 0L4.363 8.32c-.2.685.028 1.488.544 1.838l3.187 2.126a2.25 2.25 0 0 0 2.373 0l3.187-2.126c.516-.35.744-1.153.544-1.838L9.309 3.14Z" />
                          <path fillRule="evenodd" d="M11.664 12.84a2.25 2.25 0 0 1-3.328 0 2.25 2.25 0 0 0-2.373 0l-3.187 2.126c-.516.35-.744 1.153-.544 1.838L4.363 17.68c.094.323.527.323.62 0l4.326-5.18a2.25 2.25 0 0 1 2.373 0l4.326 5.18c.094.323.527.323.62 0l1.414-4.949c.2-.685-.028-1.488-.544-1.838L11.664 12.84Z" clipRule="evenodd" />
                        </svg>
                        {submitButtonText}
                    </button>
                </div>
            </div>
            <div className="flex-grow">
                <Editor
                    height="100%"
                    language={languageMap[language]}
                    value={code}
                    onChange={(value) => setCode(tabIndex || 0,value || '')}
                    theme={editorTheme}
                    options={{
                        minimap: { enabled: false },
                        fontSize: 14,
                        wordWrap: 'on',
                    }}
                />
            </div>

            {/* Template Modal */}
            {isTemplateModalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4">
                    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-4xl flex flex-col max-h-[90vh]">
                        <div className="flex justify-between items-center p-4 border-b border-gray-200 dark:border-gray-700">
                            <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100">
                                Code Template for {language.toUpperCase()}
                            </h2>
                            <button
                                onClick={closeTemplateModal}
                                className="p-2 rounded-md hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                            >
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="w-6 h-6 text-gray-500 dark:text-gray-400">
                                    <path d="M6.28 5.22a.75.75 0 0 0-1.06 1.06L8.94 10l-3.72 3.72a.75.75 0 1 0 1.06 1.06L10 11.06l3.72 3.72a.75.75 0 1 0 1.06-1.06L11.06 10l3.72-3.72a.75.75 0 0 0-1.06-1.06L10 8.94 6.28 5.22Z" />
                                </svg>
                            </button>
                        </div>
                        
                        <div className="flex-grow overflow-y-auto p-4">
                            <pre className="bg-gray-50 dark:bg-gray-900 text-gray-800 dark:text-gray-200 rounded-lg p-4 border border-gray-200 dark:border-gray-700 text-sm font-mono whitespace-pre-wrap break-words">
                                {codeTemplate}
                            </pre>
                        </div>
                        
                        <div className="flex justify-end gap-3 p-4 border-t border-gray-200 dark:border-gray-700">
                            <button
                                onClick={closeTemplateModal}
                                className="px-4 py-2 text-gray-700 dark:text-gray-300 font-medium rounded-md border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                            >
                                Close
                            </button>
                            <button
                                onClick={applyTemplate}
                                className="px-4 py-2 bg-blue-600 text-white font-medium rounded-md hover:bg-blue-700 transition-colors"
                            >
                                Apply Template
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CodeEditor;
