const API_BASE_URL = "http://localhost:8081";

export const executeCode = async (language, code) => {
    try {
        const response = await fetch(`${API_BASE_URL}/api/execute`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                language,
                code
            })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || data.error || "Execution failed");
        }

        return data;

    } catch (error) {
        console.error("Execution API Error:", error);
        throw error;
    }
};
