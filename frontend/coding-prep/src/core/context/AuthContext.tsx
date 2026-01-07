import React, { createContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import { useNavigate } from 'react-router';
import { jwtDecode } from 'jwt-decode';

import {  fetchUserStatus } from '../../features/auth/services/authService';
import type { UserStatus } from 'src/features/match/types/match';

interface DecodedToken {
    sub: string;
    roles: string[];
    userId: string;
    exp: number;
}


export interface AuthenticatedUser {
    username: string;
    id: string;
    roles: string[];
}


export interface AuthContextType {
    user: AuthenticatedUser | null;
    logout: () => void;
    isAuthenticated: boolean;
    permissions: string[];
    hasPermission: (permission: string) => boolean;
    hasRole: (role: string) => boolean;
    updateUser: (user: AuthenticatedUser) => void;
    handleLoginSuccess: (accessToken: string, refreshToken: string) => void;
    userStatus: UserStatus | null;
    
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
    children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
    const [user, setUser] = useState<AuthenticatedUser | null>(null);
    const [userStatus,setUserStatus] = useState<UserStatus | null>(null);
    const [permissions, setPermissions] = useState<string[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const navigate = useNavigate();

    const logout = useCallback(() => {
        setUser(null);
        setPermissions([]);
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        navigate('/login');
    }, [navigate]); // logout function reference unchanged unless navigate

    const handleLoginSuccess = (accessToken: string, refreshToken?: string) => {
        localStorage.setItem('accessToken', accessToken);
        if (refreshToken) {
            localStorage.setItem('refreshToken', refreshToken);
        }

        const decoded = jwtDecode<DecodedToken>(accessToken);
        setUser({
            username: decoded.sub,
            roles: decoded.roles,
            id: decoded.userId,
        });

        //const fetchedPermissions = await fetchMyPermissions();
        //setPermissions(fetchedPermissions);
        navigate('/home');
    };
    const handleUserStatus =  async () => {

        return await fetchUserStatus();
        //setPermissions(fetchedPermissions);

    }

    //const googleLogin = async (credentialResponse: CredentialResponse) => {
    //    try {
    //        if (!credentialResponse.credential) {
    //            throw new Error("Google login failed: No credential returned.");
    //        }
    //        const { accessToken, refreshToken } = await loginWithGoogle(credentialResponse.credential);
    //        
    //        if (!accessToken) {
    //            throw new Error("Login failed: No access token received from backend.");
    //        }

    //        await handleLoginSuccess(accessToken, refreshToken ?? undefined);
    //    } catch (error) {
    //        console.error("Error during Google login:", error);
    //        logout();
    //    }
    //};

    useEffect(() => {
        const initializeAuth = async () => {
            const token = localStorage.getItem('accessToken');
            
            if (!token) {
                setIsLoading(false);
                return;
            }

            try {
                const decoded = jwtDecode<DecodedToken>(token);
                if (decoded.exp * 1000 > Date.now()) {
                    setUser({ 
                username: decoded.sub,
                roles: decoded.roles,
                id: decoded.userId,
                    });
                    const curUserStatus : UserStatus | null = await handleUserStatus()
                    setUserStatus(curUserStatus)
                    //const fetchedPermissions = await fetchMyPermissions();

                    //setPermissions(fetchedPermissions);
                } else {
                    logout();
                }
            } catch (error) {
                console.error("Invalid token:", error);
                logout();
            }
            
            setIsLoading(false);
        };
        initializeAuth();
    }, [logout]);

    const hasPermission = (permission: string): boolean => {
        return permissions.includes(permission);
    };

    const hasRole = (role: string): boolean => {
        return user?.roles.includes(role) ?? false;
    };

    
    const contextValue: AuthContextType = {
        user,
        logout,
        isAuthenticated: !!user,
        permissions,
        hasPermission,
        hasRole,
        updateUser: setUser,
        handleLoginSuccess,
        userStatus
        
    //    googleLogin,
    };
    
    if (isLoading) {
        return <div className="min-h-screen bg-gray-900 flex items-center justify-center text-white">Loading Application...</div>;
    }

    return (
        <AuthContext.Provider value={contextValue}>
            {children}
        </AuthContext.Provider>
    );
};
