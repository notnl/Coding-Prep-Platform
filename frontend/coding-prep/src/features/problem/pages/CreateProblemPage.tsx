import type { FormEvent } from "react"



export default function CreateProblemPage()  {

    const handleSubmit = (e : FormEvent ) => {
        e.preventDefault()

    }


    return ( 
           <form className="flex flex-col max-w-7XL border-2 border-solid mx-10 my-10 h-[64rem] " onSubmit={handleSubmit}    > 
                 
                <h1> Create Problem : </h1>
                
                <input> title </input>
                
                

                    
           </form>
                         
    )


}
