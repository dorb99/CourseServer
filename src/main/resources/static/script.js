document.addEventListener('DOMContentLoaded', () => {
    const userIdInput = document.getElementById('userId');
    const userNameInput = document.getElementById('userName');
    const userAgeInput = document.getElementById('userAge');
    const patchDataInput = document.getElementById('patchData');
    const resultsDiv = document.getElementById('results');

    // Added elements for login
    const loginUsernameInput = document.getElementById('loginUsername');
    const loginPasswordInput = document.getElementById('loginPassword');
    const btnLogin = document.getElementById('btnLogin');
    const authStatusDiv = document.getElementById('authStatus');
    let currentJwt = localStorage.getItem('jwtToken'); // Load token from storage on startup

    const API_BASE_URL = '/api/v1/users';
    const AUTH_BASE_URL = '/api/v1/auth'; // Base URL for auth endpoints

    // Helper function to display results
    const displayResults = (data) => {
        resultsDiv.textContent = JSON.stringify(data, null, 2); // Pretty print JSON
    };

    // Helper function to display errors
    const displayError = (error) => {
        console.error('API Error:', error);
        let errorMessage = `Error: ${error.message || 'Unknown error'}`;
        if (error.body) {
            try {
                // Try to parse body as JSON first
                const errorBodyParsed = JSON.parse(JSON.stringify(error.body)); // Deep copy might be needed
                errorMessage += `\n\nDetails: ${JSON.stringify(errorBodyParsed, null, 2)}`;
            } catch (e) {
                 // If body is not JSON, display as text
                errorMessage += `\n\nDetails: ${error.body}`;
            }
        }
        if (error.status === 401) { // Specific message for unauthorized
            errorMessage += "\n(Check if you are logged in and the token is valid)";
        }
         resultsDiv.textContent = errorMessage;
    };

    const updateAuthStatus = () => {
        if (currentJwt) {
            // You could optionally decode the JWT here to show username/expiry, but keep it simple for now
            authStatusDiv.textContent = 'Status: Logged In (Token present)';
            authStatusDiv.style.color = 'green';
        } else {
            authStatusDiv.textContent = 'Status: Not Logged In';
            authStatusDiv.style.color = 'red';
        }
    };

    // Generic fetch function - NOW ADDS AUTH HEADER
    const fetchData = async (url, options = {}) => {
        const headers = { ...options.headers }; // Copy existing headers

        // Add Authorization header if JWT exists
        if (currentJwt) {
            headers['Authorization'] = `Bearer ${currentJwt}`;
        }

        const fetchOptions = {
             ...options, // Spread original options (method, body, etc.)
             headers: headers // Use the potentially modified headers
         };

        try {
            const response = await fetch(url, fetchOptions);

            if (!response.ok) {
                let errorBody = null;
                const contentType = response.headers.get("content-type");
                try {
                     if (contentType && contentType.includes("application/json")) {
                        errorBody = await response.json();
                    } else {
                         errorBody = await response.text();
                     }
                } catch (e) {
                    errorBody = await response.text().catch(() => 'Could not read error response body');
                }
                const error = new Error(`HTTP error! Status: ${response.status}`);
                error.status = response.status;
                error.body = errorBody;
                 if (response.status === 401) { // Unauthorized - likely bad/expired token
                     // Optionally clear the potentially invalid token
                     // currentJwt = null;
                     // localStorage.removeItem('jwtToken');
                     // updateAuthStatus();
                 }
                throw error;
            }

            if (response.status === 204) {
                return { status: 204, message: 'Operation successful (No Content)' };
            }

            const data = await response.json();
            return data;

        } catch (error) {
            throw error;
        }
    };

    // --- Event Listeners ---

    // Login Button
    btnLogin.addEventListener('click', async () => {
        const username = loginUsernameInput.value;
        const password = loginPasswordInput.value;
        if (!username || !password) {
            resultsDiv.textContent = 'Please enter username and password.';
            return;
        }
        resultsDiv.textContent = 'Attempting login...';
        try {
            // Use fetch directly (no auth header needed for login itself)
            const response = await fetch(`${AUTH_BASE_URL}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

             if (!response.ok) {
                  let errorBody = await response.json().catch(() => ({ error: 'Login failed. Check credentials.' }));
                  const error = new Error(`HTTP error! Status: ${response.status}`);
                  error.status = response.status;
                  error.body = errorBody;
                  throw error;
              }

            const data = await response.json(); // Should contain { "token": "..." }
            if (data.token) {
                currentJwt = data.token;
                localStorage.setItem('jwtToken', currentJwt); // Store token
                updateAuthStatus();
                displayResults({ message: 'Login Successful!', receivedToken: '********' }); // Don't display token directly
                loginUsernameInput.value = ''; // Clear fields
                loginPasswordInput.value = '';
            } else {
                 throw new Error('Login response did not contain a token.');
            }
        } catch (error) {
            currentJwt = null;
            localStorage.removeItem('jwtToken');
            updateAuthStatus();
            displayError(error);
        }
    });

    document.getElementById('btnGetAll').addEventListener('click', async () => {
        resultsDiv.textContent = 'Fetching all users...';
        try {
            const data = await fetchData(API_BASE_URL);
            displayResults(data);
        } catch (error) {
            displayError(error);
        }
    });

    document.getElementById('btnGetOne').addEventListener('click', async () => {
        const id = userIdInput.value;
        if (!id) {
            resultsDiv.textContent = 'Please enter a User ID.';
            return;
        }
        resultsDiv.textContent = `Fetching user with ID ${id}...`;
        try {
            const data = await fetchData(`${API_BASE_URL}/${id}`);
            displayResults(data);
        } catch (error) {
            displayError(error);
        }
    });

    document.getElementById('btnCreate').addEventListener('click', async () => {
        const name = userNameInput.value;
        const age = userAgeInput.value;
        // --- IMPORTANT: Add password input for creation --- 
        // Need to add a password field to index.html for this to work properly
        // const password = document.getElementById('userPassword').value; 
        const password = prompt("Enter password for new user:"); // Quick HACK: Use prompt

        if (!name || !age || !password) {
            resultsDiv.textContent = 'Please enter Name, Age, and Password (via prompt) for creating a user.';
            return;
        }
        resultsDiv.textContent = 'Creating user...';
        // Include password in the DTO sent to the server
        const userData = { name, age: parseInt(age), password }; 
        try {
            // Create endpoint might be protected, so use authenticated fetchData
            const data = await fetchData(API_BASE_URL, { 
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(userData),
            });
            displayResults(data); // Server response likely won't include password
            userNameInput.value = ''; 
            userAgeInput.value = '';
        } catch (error) {
            displayError(error);
        }
    });

    document.getElementById('btnUpdate').addEventListener('click', async () => {
        const id = userIdInput.value;
        const name = userNameInput.value;
        const age = userAgeInput.value;
        // --- Handle optional password update ---
        // const password = document.getElementById('userPassword').value; // Need password field in HTML
        const password = prompt("Enter NEW password (or leave blank to keep unchanged):"); // Quick HACK

        if (!id || !name || !age) {
            resultsDiv.textContent = 'Please enter User ID, Name, and Age for updating a user (PUT).';
            return;
        }
        resultsDiv.textContent = `Updating user with ID ${id}...`;
        const userData = { name, age: parseInt(age) };
        if (password) { // Only include password if provided
             userData.password = password;
        }
        try {
            const data = await fetchData(`${API_BASE_URL}/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(userData),
            });
            displayResults(data);
        } catch (error) {
            displayError(error);
        }
    });

     document.getElementById('btnPatch').addEventListener('click', async () => {
        const id = userIdInput.value;
        const patchJson = patchDataInput.value;
        if (!id || !patchJson) {
            resultsDiv.textContent = 'Please enter User ID and JSON Patch Data for patching a user.';
            return;
        }
        resultsDiv.textContent = `Patching user with ID ${id}...`;
        try {
            const patchDataObject = JSON.parse(patchJson);
            // --- Hashing password if included in PATCH ---
            // The backend now handles hashing if 'password' is in the patch data.
            // Frontend doesn't need to hash here.

            const data = await fetchData(`${API_BASE_URL}/${id}`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(patchDataObject),
            });
            displayResults(data);
        } catch (error) {
            displayError(error);
        }
    });

    document.getElementById('btnDelete').addEventListener('click', async () => {
        const id = userIdInput.value;
        if (!id) {
            resultsDiv.textContent = 'Please enter a User ID.';
            return;
        }
        resultsDiv.textContent = `Deleting user with ID ${id}...`;
        try {
            const data = await fetchData(`${API_BASE_URL}/${id}`, {
                method: 'DELETE',
            });
            displayResults(data);
        } catch (error) {
            displayError(error);
        }
    });

    // Update auth status on page load
    updateAuthStatus();
}); 