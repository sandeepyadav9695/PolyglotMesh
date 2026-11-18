import { useState } from 'react';

import Editor from '@monaco-editor/react';

import { executeCode } from "./services/executionService";

function App() {

    const [language, setLanguage] = useState('python');

    const [code, setCode] = useState({
        python:
            `print("Hello from PolyglotMesh!")

a = 10
b = 20

print("Addition:", a + b)`,

        javascript:
            `console.log("Hello from GraalJS!");

const a = 100;
const b = 200;

console.log("Addition:", a + b);`
    });

    const [result, setResult] = useState(null);

    const [loading, setLoading] = useState(false);

    const [activeTab, setActiveTab] = useState('editor');

    const handleLanguageChange = (selectedLanguage) => {

        setLanguage(selectedLanguage);

        setResult(null);

    };

    const handleCodeChange = (value) => {

        setCode(previous => ({
            ...previous,
            [language]: value || ''
        }));

    };

    const handleRunCode = async () => {

        setLoading(true);

        setResult(null);

        setActiveTab('output');

        try {

            const response = await executeCode(
                language,
                code[language]
            );

            setResult(response);

        } catch (error) {

            setResult({
                success: false,
                output: '',
                error: error.message,
                executionTimeMs: 0
            });

        } finally {

            setLoading(false);

        }

    };

    return (

        <div className="app">

            {/* Navbar */}

            <header className="navbar">

                <div className="brand">

                    <span className="brand-icon">
                        {'</>'}
                    </span>

                    <div>
                        <h2>PolyglotMesh</h2>
                        <small>Multi-Language Runtime</small>
                    </div>

                </div>

                <div className="nav-right">

                    <span className="status">
                        ● Local Runtime
                    </span>

                    <span className="version">
                        v1.0
                    </span>

                </div>

            </header>

            {/* Main Dashboard */}

            <main className="dashboard">

                <section className="welcome">

                    <div>
                        <h1>Polyglot Code Playground</h1>

                        <p>
                            Write, execute and test multiple
                            programming languages in one environment.
                        </p>
                    </div>

                    <button
                        className="run-button"
                        onClick={handleRunCode}
                        disabled={loading}
                    >

                        {loading ? 'Executing...' : '▶ Run Code'}

                    </button>

                </section>

                {/* Language Tabs */}

                <section className="editor-card">

                    <div className="toolbar">

                        <div className="language-tabs">

                            <button
                                className={
                                    language === 'python'
                                        ? 'language-tab active'
                                        : 'language-tab'
                                }
                                onClick={() =>
                                    handleLanguageChange('python')
                                }
                            >
                                🐍 Python
                            </button>

                            <button
                                className={
                                    language === 'javascript'
                                        ? 'language-tab active'
                                        : 'language-tab'
                                }
                                onClick={() =>
                                    handleLanguageChange('javascript')
                                }
                            >
                                JS JavaScript
                            </button>

                        </div>

                        <span className="language-label">
                            {language.toUpperCase()}
                        </span>

                    </div>

                    {/* Monaco Editor */}

                    <div className="editor-container">

                        <Editor
                            height="440px"

                            language={
                                language === 'python'
                                    ? 'python'
                                    : 'javascript'
                            }

                            theme="vs-dark"

                            value={code[language]}

                            onChange={handleCodeChange}

                            options={{
                                fontSize: 15,
                                minimap: {
                                    enabled: true
                                },
                                automaticLayout: true,
                                scrollBeyondLastLine: false,
                                wordWrap: 'on',
                                padding: {
                                    top: 20
                                },
                                tabSize: 4,
                                formatOnPaste: true,
                                formatOnType: true
                            }}
                        />

                    </div>

                </section>

                {/* Output Console */}

                <section className="output-card">

                    <div className="output-header">

                        <div className="output-tabs">

                            <button
                                className={
                                    activeTab === 'output'
                                        ? 'output-tab selected'
                                        : 'output-tab'
                                }
                                onClick={() => setActiveTab('output')}
                            >
                                Console Output
                            </button>

                            <button
                                className={
                                    activeTab === 'editor'
                                        ? 'output-tab selected'
                                        : 'output-tab'
                                }
                                onClick={() => setActiveTab('editor')}
                            >
                                Editor
                            </button>

                        </div>

                        {result && (

                            <span
                                className={
                                    result.success
                                        ? 'success-status'
                                        : 'error-status'
                                }
                            >
                                {result.success ? 'SUCCESS' : 'FAILED'}
                            </span>

                        )}

                    </div>

                    <div className="console">

                        {loading ? (

                            <p>Executing your code...</p>

                        ) : result ? (

                            <>

                                {result.output && (

                                    <pre className="console-output">
                                        {result.output}
                                    </pre>

                                )}

                                {result.error && (

                                    <pre className="console-error">
                                        {result.error}
                                    </pre>

                                )}

                                <div className="execution-info">

                                    Execution time: {
                                    result.executionTimeMs
                                } ms

                                </div>

                            </>

                        ) : (

                            <p className="console-placeholder">
                                Click Run Code to see execution output here.
                            </p>

                        )}

                    </div>

                </section>

            </main>

            <footer>
                PolyglotMesh © 2026 | Powered by Spring Boot & GraalVM
            </footer>

        </div>

    );

}

export default App;