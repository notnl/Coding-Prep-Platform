export const api_post = async (requestURI : string, requestData : any): Promise<Response>  => {

    
    
    const accessToken = localStorage.getItem('accessToken');
    //For now will do this without error checking
                    return await fetch(requestURI, 
                                 { method:"POST",
                                    body:requestData,
                                        headers: {
                                            "Authorization":`Bearer ${accessToken}`,
                                            "Content-Type": "application/json"
                                        }})

}

export const api_get = async (requestURI : string): Promise<Response>  => {

    const accessToken = localStorage.getItem('accessToken');
    //For now will do this without error checking
                    return await fetch(requestURI, 
                                 { method:"GET",
                                        headers: {
                                            "Authorization":`Bearer ${accessToken}`,
                                            "Content-Type": "application/json"
                                        }})

}


