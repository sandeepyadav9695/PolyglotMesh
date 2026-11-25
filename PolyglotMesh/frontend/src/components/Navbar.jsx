
export default function Navbar() {
    return (
        <header className="navbar">
            <a className="brand" href="/" aria-label="PolyglotMesh home">
                <span className="brand-logo">{"</>"}</span>
                <span className="brand-name">
          PolyglotMesh
          <small>Developer Playground</small>
        </span>
            </a>

            <nav className="top-navigation">
                <a href="#playground" className="nav-link active">Practice</a>
                <a href="#features" className="nav-link">Features</a>
                <a href="#about" className="nav-link">About</a>
            </nav>

            <div className="navbar-right">
        <span className="online-status">
          <span className="status-dot" />
          Runtime Ready
        </span>
            </div>
        </header>
    );
}
