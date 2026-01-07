import { useNavigate } from "react-router";

export default function Navbar(){

    const navigate = useNavigate();

    return (
            <div className="flex border-2 border-solid mx-10 my-10 max-w-full justify-between gap-5 ">

            <button
              className="  min-w-1/8 min-h-1/4 my-5 mx-5 bg-[#F97316] hover:bg-[#EA580C] text-white font-bold py-3 px-4 rounded-md transition-transform transform hover:scale-105 disabled:bg-orange-900/50 disabled:cursor-not-allowed disabled:transform-none" onClick={() => navigate('/home')}
              >
                Coding Prep
              </button>



            </div>

    )


}
