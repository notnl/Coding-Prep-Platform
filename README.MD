# Overview

A Coding Platform referenced from (Abhinav1416 repo, please see disclaimer section) designed to encourage discussion-based solving. A self-hosted Judge0 API is used to judge submissions.


# Configuration

See application.properties and create a .env file and fill all ${} values

```
Example:

app.frontend.url=${FRONTEND_URL}

```

in .env
```

FRONTEND_URL = YOUR_URL

```


# Build and Run


Start Backend Services 

Navigate to backend folder 

```

chmod +x backend/judge0-v1.13.1/RunService.sh

sh backend/judge0-v1.13.1/RunService.sh


```

Start web

Navigate to frontend folder 


```

pnpm run dev

```


# IMPORTANT
DISCLAIMER: A fork of https://github.com/Abhinav1416/coding-platform , some of the code used are 1:1 copies or modified based on new requirements 
