
const navigationItems = [
    { id: "playground", icon: "⌘", label: "Code Playground" },
    { id: "practice", icon: "▤", label: "Practice Problems" },
    { id: "languages", icon: "⌁", label: "Languages" },
];

export default function Sidebar({ activeSection, onSectionChange }) {
    return (
        <aside className="sidebar">
            <div className="sidebar-heading">LEARN AND PRACTICE</div>

            {navigationItems.map((item) => (
                <button
                    key={item.id}
                    type="button"
                    className={`sidebar-item ${
                        activeSection === item.id ? "active" : ""
                    }`}
                    onClick={() => onSectionChange(item.id)}
                >
                    <span>{item.icon}</span>
                    {item.label}
                </button>
            ))}

            <div className="sidebar-bottom">
                <div className="sidebar-heading">YOUR ENVIRONMENT</div>
                <div className="runtime-card">
                    <span className="status-dot" />
                    <div>
                        <strong>Local Runtime</strong>
                        <small>Spring Boot + GraalVM</small>
                    </div>
                </div>
            </div>
        </aside>
    );
}
