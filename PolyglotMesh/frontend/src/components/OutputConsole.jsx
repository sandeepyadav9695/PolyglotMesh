
export default function OutputConsole({ result, loading }) {
    return (
        <section className="output-card">
            <div className="output-header">
                <div>
                    <span className="terminal-icon">⌁</span>
                    <strong>Execution Console</strong>
                </div>

                {result && (
                    <span
                        className={`execution-status ${
                            result.success ? "success" : "failure"
                        }`}
                    >
            {result.success ? "SUCCESS" : "FAILED"}
          </span>
                )}
            </div>

            <div className="console-body" aria-live="polite">
                {loading ? (
                    <div className="console-message">
                        <span className="loading-spinner" />
                        Executing your program...
                    </div>
                ) : result ? (
                    <>
                        {result.output && (
                            <div className="console-section">
                                <span className="console-label">OUTPUT</span>
                                <pre className="console-output">{result.output}</pre>
                            </div>
                        )}

                        {result.error && (
                            <div className="console-section error-section">
                <span className="console-label">
                  {result.resourceExhausted
                      ? "SANDBOX RESOURCE LIMIT"
                      : "EXECUTION ERROR"}
                </span>
                                <pre className="console-error">{result.error}</pre>
                            </div>
                        )}

                        {!result.output && !result.error && (
                            <p className="console-placeholder">
                                Program executed without producing output.
                            </p>
                        )}

                        <div className="execution-info">
                            Execution time: {result.executionTimeMs ?? 0} ms
                        </div>
                    </>
                ) : (
                    <p className="console-placeholder">
                        Run your program to view output and execution details.
                    </p>
                )}
            </div>
        </section>
    );
}
