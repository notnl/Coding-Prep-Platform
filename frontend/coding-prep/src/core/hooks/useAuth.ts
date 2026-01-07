import {type Player} from '../../features/match/types/match'
import { api_post } from './useAPIRequest'

export default function useAuth() 
{
    
    //Fetch from our database the current user and return its details based on me
    // But we don't want to call it again if its data is already populated
   
    //api_post("")
    
   
   //const user : Player = { player_id:0,player_username:"Hi",player_team:0  } 
   const user = {email:"hiemail"}
   return  { user } // Whatever that is inside is a property?
}
