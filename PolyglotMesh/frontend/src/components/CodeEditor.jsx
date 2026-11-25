
import Editor from "@monaco-editor/react";

export default function CodeEditor({ language, value, onChange }) {
    return (
        <div className="editor-container">
            <Editor
                height="430px"
                language={language}
                theme="vs-dark"
                value={value}
                onChange={onChange}
                loading={<div className="editor-loading">Loading editor...</div>}
                options={{
                    fontSize: 15,
                    minimap: { enabled: false },
                    automaticLayout: true,
                    scrollBeyondLastLine: false,
                    wordWrap: "on",
                    tabSize: 4,
                    formatOnPaste: true,
                    formatOnType: true,
                    padding: { top: 18 },
                    lineNumbers: "on",
                    renderLineHighlight: "all",
                }}
            />
        </div>
    );
}
