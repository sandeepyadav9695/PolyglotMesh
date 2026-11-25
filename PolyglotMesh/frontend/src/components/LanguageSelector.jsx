
const languages = [
    { id: "python", label: "Python", icon: "🐍" },
    { id: "javascript", label: "JavaScript", icon: "JS" },
];

export default function LanguageSelector({
                                             language,
                                             onLanguageChange,
                                         }) {
    return (
        <div className="language-toolbar">
            <div className="language-tabs">
                {languages.map((item) => (
                    <button
                        key={item.id}
                        type="button"
                        className={`language-tab ${
                            language === item.id ? "active" : ""
                        }`}
                        onClick={() => onLanguageChange(item.id)}
                    >
                        <span>{item.icon}</span>
                        {item.label}
                    </button>
                ))}
            </div>

            <span className="language-label">
        {language.toUpperCase()}
      </span>
        </div>
    );
}
