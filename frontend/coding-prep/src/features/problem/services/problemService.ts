
import type { ProblemDetail, SubmissionRequest, PaginatedSubmissionResponse, SubmissionDetails, ProblemCountResponse } from '../types/problem';

import { api_get,api_post } from 'src/core/hooks/useAPIRequest';

const API_BASE_URL = '/api/v1/submissions';
const API_STATS_URL = '/stats';



export const getProblemBySlug = async (slug: string): Promise<ProblemDetail> => {
    const response = await api_get(`/problems/${slug}`);
    const responseJson = await response.json();
    return responseJson
};

export interface SubmissionResponse {
    submissionId: string;
}

export const createSubmission = async (request: SubmissionRequest): Promise<SubmissionResponse> => {

    
    const response = await api_post(`${API_BASE_URL}/create`, JSON.stringify(request));

    const responseJson = await response.json();
    return responseJson
};

export const getProblemSubmissions = async (problemId: string): Promise<PaginatedSubmissionResponse> => {
    const response = await api_get(`/submissions/problem/${problemId}`);

    const responseJson = await response.json();
    const normalizedSubmissions = responseJson.submissions.map((sub: any) => ({
        ...sub,
        language: sub.language.name, 
    }));
    return {
        ...responseJson,
        submissions: normalizedSubmissions,
    };
};

export const getSubmissionDetails = async (submissionId: string): Promise<SubmissionDetails> => {
    const response = await api_get(`${API_BASE_URL}/${submissionId}`);

    const responseJson = await response.json();
    return responseJson
};

export const getProblemCount = async (): Promise<ProblemCountResponse> => {

    const response = await api_get('/problems/count');
    const responseJson = await response.json();
    return responseJson
};

export const getAllSubmissionRefresh = async (matchId: string) : Promise<SubmissionDetails[]> => {
   return (await api_get(`${API_BASE_URL}/get/${matchId}`)).json();
};
