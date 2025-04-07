document.addEventListener('DOMContentLoaded', () => {
    const userIdInput = document.getElementById('userId');
    const userNameInput = document.getElementById('userName');
    const userAgeInput = document.getElementById('userAge');
    const patchDataInput = document.getElementById('patchData');
    const resultsDiv = document.getElementById('results');

    const API_BASE_URL = '/api/v1/users'; // Relative path works because served from same origin

    // Helper function to display results
    const displayResults = (data) => {
        resultsDiv.textContent = JSON.stringify(data, null, 2); // Pretty print JSON
    };

    // Helper function to display errors
    const displayError = (error) => {
        console.error('API Error:', error);
        resultsDiv.textContent = `Error: ${error.message}\n\n${error.body ? `Details: ${JSON.stringify(error.body, null, 2)}` : '(See console for more details)'}`;
    };

    // Generic fetch function
    const fetchData = async (url, options = {}) => {
        try {
            const response = await fetch(url, options);

            // Prepare error object if response is not ok
            if (!response.ok) {
                let errorBody = null;
                try {
                     // Try to parse error body as JSON
                    errorBody = await response.json();
                } catch (e) {
                     // If not JSON, try to read as text
                    errorBody = await response.text().catch(() => 'Could not read error response body');
                }
                const error = new Error(`HTTP error! Status: ${response.status}`);
                error.status = response.status;
                error.body = errorBody; // Attach parsed body to error object
                throw error;
            }

            // Handle 204 No Content specifically
            if (response.status === 204) {
                return { status: 204, message: 'Operation successful (No Content)' };
            }

            // Otherwise, parse JSON response body
            const data = await response.json();
            return data;

        } catch (error) {
             // Re-throw custom error or standard network error
            throw error;
        }
    };

    // --- Event Listeners ---

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
        if (!name || !age) {
            resultsDiv.textContent = 'Please enter both Name and Age for creating a user.';
            return;
        }
        resultsDiv.textContent = 'Creating user...';
        const userData = { name, age: parseInt(age) }; // Ensure age is number
        try {
            const data = await fetchData(API_BASE_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(userData),
            });
            displayResults(data);
            userNameInput.value = ''; // Clear fields after successful creation
            userAgeInput.value = '';
        } catch (error) {
            displayError(error);
        }
    });

    document.getElementById('btnUpdate').addEventListener('click', async () => {
        const id = userIdInput.value;
        const name = userNameInput.value;
        const age = userAgeInput.value;
        if (!id || !name || !age) {
            resultsDiv.textContent = 'Please enter User ID, Name, and Age for updating a user (PUT).';
            return;
        }
        resultsDiv.textContent = `Updating user with ID ${id}...`;
        const userData = { name, age: parseInt(age) };
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
             // Basic check if patchJson is valid JSON before sending
            const patchDataObject = JSON.parse(patchJson);

            const data = await fetchData(`${API_BASE_URL}/${id}`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(patchDataObject), // Send the parsed object
            });
            displayResults(data);
        } catch (error) {
             if (error instanceof SyntaxError) { // Catch JSON parsing errors
                 displayError({ message: 'Invalid JSON format in Patch Data field.' });
             } else {
                 displayError(error);
             }
        }
    });


    document.getElementById('btnDelete').addEventListener('click', async () => {
        const id = userIdInput.value;
        if (!id) {
            resultsDiv.textContent = 'Please enter a User ID to delete.';
            return;
        }
        if (!confirm(`Are you sure you want to delete user with ID ${id}?`)) {
             return;
        }
        resultsDiv.textContent = `Deleting user with ID ${id}...`;
        try {
            const data = await fetchData(`${API_BASE_URL}/${id}`, {
                method: 'DELETE',
            });
             // Handle 204 No Content specifically if needed, or just show success
            displayResults(data || { message: `User ${id} deleted successfully.` });
        } catch (error) {
            displayError(error);
        }
    });
}); 