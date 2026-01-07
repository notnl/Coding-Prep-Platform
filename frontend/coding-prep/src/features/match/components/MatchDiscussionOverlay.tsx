import { motion } from 'framer-motion';
import { useEffect, useState } from 'react';
import type { MatchDiscussion } from '../types/match';
import { Panel, Group, Separator } from 'react-resizable-panels';
import Tabs from '../../../core/components/Tabs';
import CodeFeedback from './CodeFeedback';



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


interface TabType {
        label:string
        content: Element
}
interface MatchDiscussionProps {
    allCode : MatchDiscussion
    isHost : boolean
    endDiscussion : () => void
}


export default function MatchDiscussionOverlay ({allCode, isHost, endDiscussion}  : MatchDiscussionProps){

    const [activeVisualTab, setActiveVisualTab] = useState(0);
    const [allTabs, setAllTabs] = useState<TabType[]>()
        
    const leftPanelTabs = 
        allCode.allCode.map( (i,ind) => { 

            return( 
                   { label: i.playerName, content: <CodeFeedback curCode={i} /> })
        }) 

    //useEffect( () => {

    //    console.log(typeof(leftPanelTabs))
    //
    //}, 


    //          []

    //)
        
    const onSubmit = () => {
        

    }
    if (allCode.allCode.length == 0 ){

        return  ( 


    <motion.div
        className={`flex fixed max-w-7XL mx-20 h-full inset-0 bg-black/80  items-center justify-center z-50 p-4` }
        initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.5 }}

    >


    
                 { // will this clean up in the future
                    isHost && (
                    <button 
                        onClick={endDiscussion}
                        className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white font-semibold rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50 disabled:bg-gray-500 disabled:cursor-not-allowed transition-colors"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="w-5 h-5">
                          <path d="M9.309 3.14c-.094-.323-.527-.323-.62 0L4.363 8.32c-.2.685.028 1.488.544 1.838l3.187 2.126a2.25 2.25 0 0 0 2.373 0l3.187-2.126c.516-.35.744-1.153.544-1.838L9.309 3.14Z" />
                          <path fillRule="evenodd" d="M11.664 12.84a2.25 2.25 0 0 1-3.328 0 2.25 2.25 0 0 0-2.373 0l-3.187 2.126c-.516.35-.744 1.153-.544 1.838L4.363 17.68c.094.323.527.323.62 0l4.326-5.18a2.25 2.25 0 0 1 2.373 0l4.326 5.18c.094.323.527.323.62 0l1.414-4.949c.2-.685-.028-1.488-.544-1.838L11.664 12.84Z" clipRule="evenodd" />
                        </svg>
                        {"End discussion(host)"}
                    </button>)
                 }
    </motion.div>
                )
    }



    return ( 
    <motion.div
        className={`flex fixed max-w-7XL mx-20 h-full inset-0 bg-black/80  items-center justify-center z-50 p-4` }
        initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.5 }}
    >

                 {
                    isHost && (
                    <button 
                        onClick={endDiscussion}
                        className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white font-semibold rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50 disabled:bg-gray-500 disabled:cursor-not-allowed transition-colors"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="w-5 h-5">
                          <path d="M9.309 3.14c-.094-.323-.527-.323-.62 0L4.363 8.32c-.2.685.028 1.488.544 1.838l3.187 2.126a2.25 2.25 0 0 0 2.373 0l3.187-2.126c.516-.35.744-1.153.544-1.838L9.309 3.14Z" />
                          <path fillRule="evenodd" d="M11.664 12.84a2.25 2.25 0 0 1-3.328 0 2.25 2.25 0 0 0-2.373 0l-3.187 2.126c-.516.35-.744 1.153-.544 1.838L4.363 17.68c.094.323.527.323.62 0l4.326-5.18a2.25 2.25 0 0 1 2.373 0l4.326 5.18c.094.323.527.323.62 0l1.414-4.949c.2-.685-.028-1.488-.544-1.838L11.664 12.84Z" clipRule="evenodd" />
                        </svg>
                        {"End discussion(host)"}
                    </button>)
                 }
                    <Group className="flex h-4/5 flex-grow overflow-hidden">
                        <Panel defaultSize={50} minSize={25} className="flex flex-col bg-gray-50 dark:bg-zinc-900">
                            <Tabs tabs={leftPanelTabs} activeTab={activeVisualTab} setActiveTab={setActiveVisualTab
                                    } />
                        </Panel>
                    </Group>
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
    </motion.div>
    )
}
