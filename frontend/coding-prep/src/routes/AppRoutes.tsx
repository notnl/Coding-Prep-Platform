import { Navigate, Route,Routes } from "react-router"; 
import Login from "src/pages/Login";
import Home from "src/pages/Home";
import ProfilePage from "src/features/profile/pages/ProfilePage";
import MatchLobbyPage from "src/features/match/pages/MatchLobbyPage";
import MatchArenaPage from "src/features/match/pages/MatchArenaPage";
import MatchResultPage from "src/features/match/pages/MatchResultPage";

export default function AppRoutes() {

    return (
        <Routes>

        <Route index element={<Login theme="dark"/>} / >
        <Route path="/login" element={<Login theme="dark"/>} / >
        <Route path="/home" element={<Home theme="dark"/>} / >
        <Route path="/profile" element={<ProfilePage theme="dark"/>} / >
        <Route path="/match/lobby/:matchId" element={<MatchLobbyPage />} />
        <Route path="/match/arena/:matchId" element={<MatchArenaPage />} />
        <Route path="/match/results/:matchId" element={<MatchResultPage />} />
        <Route path='*' element={<Navigate to='/' />} />

        </Routes>





    )
}


