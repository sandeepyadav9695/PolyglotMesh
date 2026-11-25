
import { useCallback, useState } from "react";
import { executeCode } from "./services/executionService";

import Navbar from "./components/Navbar";
import Sidebar from "./components/Sidebar";
import LanguageSelector from "./components/LanguageSelector";
import CodeEditor from "./components/CodeEditor";
import OutputConsole from "./components/OutputConsole";

import "./App.css";

const INITIAL_CODE = {
    python: `print("Hello from PolyglotMesh!")

a = 10
b = 20

print("Addition:", a + b)
`,
    javascript: `console.log("Hello from GraalJS!");

const a = 100;
const b = 200;

console.log("Addition:", a + b);
`,
};

function App() {
    const [language, setLanguage] = useState("python");
    const [code, setCode] = useState(INITIAL_CODE);
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);
    const [activeSection, setActiveSection] = useState("playground");

    const handleLanguageChange = useCallback((nextLanguage) => {
        setLanguage(nextLanguage);
        setResult(null);
    }, []);

    const handleCodeChange = useCallback((value) => {
        setCode((previous) => ({
            ...previous,
            [language]: value ?? "",
        }));
    }, [language]);

    const handleRunCode = async () => {
        if (loading) return;

        const currentCode = code[language];

        if (!currentCode.trim()) {
            setResult({
                success: false,
                output: "",
                error: "Please enter some code before execution.",
                executionTimeMs: 0,
                resourceExhausted: false,
            });
            return;
        }

        setLoading(true);
        setResult(null);

        try {
            // Preserve the existing service signature and backend contract.
            const response = await executeCode(language, currentCode);
            setResult(response);
        } catch (error) {
            setResult({
                success: false,
                output: "",
                error:
                    error?.response?.data?.message ||
                    error?.response?.data?.error ||
                    error?.message ||
                    "Unable to execute code. Please check the backend connection.",
                executionTimeMs: 0,
                resourceExhausted: false,
            });
        } finally {
            setLoading(false);
        }
    };

    const handleReset = () => {
        setCode((previous) => ({
            ...previous,
            [language]: INITIAL_CODE[language],
        }));
        setResult(null);
    };

    return (
        <div className="app-shell">
            <Navbar />

            <div className="app-layout">
                <Sidebar
                    activeSection={activeSection}
                    onSectionChange={setActiveSection}
                />

                <main className="main-content">
                    <section className="welcome-section">
                        <div>
                            <div className="breadcrumb">
                                Home <span>/</span> Code Playground
                            </div>

                            <h1>Code Playground</h1>

                            <p>
                                Practice, write and execute Python and JavaScript
                                programs in your own polyglot environment.
                            </p>
                        </div>

                        <div className="runtime-indicator">
                            <span className="status-dot" />
                            Runtime Environment
                        </div>
                    </section>

                    <section className="workspace">
                        <div className="workspace-header">
                            <div>
                                <h2>Online Compiler</h2>
                                <p>Write your code and execute it instantly.</p>
                            </div>

                            <div className="workspace-actions">
                                <button
                                    type="button"
                                    className="secondary-button"
                                    onClick={handleReset}
                                    disabled={loading}
                                >
                                    ↺ Reset
                                </button>

                                <button
                                    type="button"
                                    className="run-button"
                                    onClick={handleRunCode}
                                    disabled={loading}
                                >
                                    {loading ? "Executing..." : "▶ Run Code"}
                                </button>
                            </div>
                        </div>

                        <LanguageSelector
                            language={language}
                            onLanguageChange={handleLanguageChange}
                        />

                        <CodeEditor
                            language={language}
                            value={code[language]}
                            onChange={handleCodeChange}
                        />

                        <OutputConsole
                            result={result}
                            loading={loading}
                        />
                    </section>

                    <section className="feature-grid">
                        <article className="feature-card">
                            <span className="feature-icon">⌘</span>
                            <h3>Multi-language</h3>
                            <p>Write and execute Python and JavaScript code.</p>
                        </article>

                        <article className="feature-card">
                            <span className="feature-icon">⚡</span>
                            <h3>Fast execution</h3>
                            <p>Execute programs using the GraalVM runtime.</p>
                        </article>

                        <article className="feature-card">
                            <span className="feature-icon">◈</span>
                            <h3>Sandboxed runtime</h3>
                            <p>Run programs through your backend execution service.</p>
                        </article>
                    </section>

                    <footer className="app-footer">
                        PolyglotMesh © 2026 · Powered by Spring Boot and GraalVM
                    </footer>
                </main>
            </div>
        </div>
    );
}

export default App;
