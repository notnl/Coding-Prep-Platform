import { api_get } from "src/core/hooks/useAPIRequest";
import type { UserStatus } from "src/features/match/types/match";


const API_BASE_URL = '/api/v1/auth';

export const fetchMyPermissions = async (): Promise<string[]> => {
    try {
        
        const response = await (api_get('/users/me/permissions')); 
        return await response.json(); // will assume can be taken as string[]

    } catch (error) {
        console.error("Failed to fetch user permissions:", error);
        return [];
    }
};

export const fetchUserStatus = async (): Promise<UserStatus | null> => {
    try {
        
        const response = await (api_get(`${API_BASE_URL}/status`)); 
        return response.json(); 

    } catch (error) {
        console.error("Failed to fetch user status", error);
        return null
    }
};
