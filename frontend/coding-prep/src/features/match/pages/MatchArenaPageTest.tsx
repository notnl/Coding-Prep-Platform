import { useState } from "react";
import { Group,Panel, Separator } from "react-resizable-panels";
import Tabs from "src/core/components/Tabs";
import CodeEditor from "src/features/problem/components/CodeEditor";

export default function MatchArenaPageTest() {

    const leftPanelTabs = [
        { label: 'Description' ,content:"test"},
        { label: 'My Submissions'  ,content:"test"},
    ];
    
    const [activeTab, setActiveTab] = useState(0);

    const [language, setLanguage] = useState<'cpp' | 'java' | 'python'>('cpp');

    const [code, setCode] = useState<string>('// Loading code...'); 

    const onSub = () => {
            
    }
    return (
        <>
            <div className="flex flex-col h-screen bg-white dark:bg-zinc-950 text-gray-900 dark:text-white">
                <div className="flex-grow flex flex-col min-h-0">
                    <Group className="flex-grow overflow-hidden">
                        <Panel defaultSize={50} minSize={25} className="flex flex-col bg-gray-50 dark:bg-zinc-900">
                            <Tabs tabs={leftPanelTabs} activeTab={activeTab} setActiveTab={setActiveTab} />
                        </Panel>
                        <Separator className="w-2 bg-gray-300 dark:bg-zinc-800 hover:bg-[#F97316]/80 active:bg-[#F97316] transition-colors duration-200" />
                        <Panel defaultSize={50} minSize={35} className="flex flex-col bg-gray-50 dark:bg-zinc-900">
                            <CodeEditor
                                language={language} setLanguage={setLanguage}
                                code={code} setCode={setCode}
                                onSubmit={onSub}
                                isSubmittingDisabled={false}
                            />
                        </Panel>
                    </Group>
                </div>
            </div>
        </>
    );
}
