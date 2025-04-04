import React, { useState, useEffect } from "react";
import './App.css';

function App() {
    // State variables
    const [jobs, setJobs] = useState([]);
    const [query, setQuery] = useState("");
    const [location, setLocation] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [message, setMessage] = useState("");
    const [activeSource, setActiveSource] = useState("all");

    // When component is created, run fetchJobs()
    useEffect(() => {
        fetchJobs();
    }, []);

    // Fetch jobs from backend
    const fetchJobs = async (searchQuery = '', searchLocation = '') => {
        setLoading(true);
        setError(null);
        setMessage("Searching for jobs...");

        try {
            let url = 'http://localhost:8080/jobs';

            // Add search parameters if provided
            if (searchQuery || searchLocation) {
                url = `http://localhost:8080/jobs/search?query=${encodeURIComponent(searchQuery)}&location=${encodeURIComponent(searchLocation)}`;
                console.log("Searching with URL:", url);
            }

            const response = await fetch(url);

            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            const data = await response.json();
            setJobs(data);
            setMessage(`Found ${data.length} jobs`);
        } catch (err) {
            setError('Error fetching jobs: ' + err.message);
            setMessage("Error occurred during search");
            console.error('Error fetching jobs:', err);
        } finally {
            setLoading(false);
        }
    };

    // Handle search form submission
    const handleSearch = (event) => {
        event.preventDefault();
        fetchJobs(query, location);
        setActiveSource("all");
    };

    // Test the scraper
    const testScraper = async (site) => {
        setLoading(true);
        setMessage(`Testing ${site} scraper...`);

        try {
            const response = await fetch(`http://localhost:8080/test/scrape-${site.toLowerCase()}?query=${query || 'java'}&location=${location || 'remote'}`);

            if (!response.ok) {
                throw new Error(`${site} scraper test failed`);
            }

            const data = await response.json();
            setJobs(data);
            setMessage(`${site} scraper test completed. Found ${data.length} jobs.`);
            setActiveSource(site.toLowerCase());
        } catch (err) {
            setError(`Error testing ${site} scraper: ` + err.message);
            console.error(`Error testing ${site} scraper:`, err);
        } finally {
            setLoading(false);
        }
    };

    // Filterjobs based on source
    const displayedJobs = activeSource === "all"
        ? jobs
        : jobs.filter(job => job.company.toLowerCase().includes(activeSource));

    return (
        <div className="App">
            <header className="app-header">
                <h1>Job Search Aggregator</h1>
                <p>Find the perfect job from multiple sources</p>
            </header>

            <div className="search-container">
                <form onSubmit={handleSearch} className="search-form">
                    <div className="input-group">
                        <label htmlFor="query">Job Title, Keywords, or Company</label>
                        <input
                            id="query"
                            type="text"
                            placeholder="e.g. JavaScript Developer, Data Analyst"
                            value={query}
                            onChange={(e) => setQuery(e.target.value)}
                        />
                    </div>

                    <div className="input-group">
                        <label htmlFor="location">Location</label>
                        <input
                            id="location"
                            type="text"
                            placeholder="e.g. Remote, New York, London"
                            value={location}
                            onChange={(e) => setLocation(e.target.value)}
                        />
                    </div>

                    <button type="submit" className="search-button">
                        Search Jobs
                    </button>
                </form>

                <div className="source-filters">
                    <button
                        className={`source-button ${activeSource === "all" ? "active" : ""}`}
                        onClick={() => setActiveSource("all")}>
                        All Sources
                    </button>
                    <button
                        className={`source-button ${activeSource === "indeed" ? "active" : ""}`}
                        onClick={() => testScraper('Indeed')}>
                        Indeed
                    </button>
                    <button
                        className={`source-button ${activeSource === "linkedin" ? "active" : ""}`}
                        onClick={() => testScraper('LinkedIn')}>
                        LinkedIn
                    </button>
                </div>
            </div>

            {loading && <div className="status loading">Loading jobs...</div>}
            {message && !loading && <div className="status message">{message}</div>}
            {error && <div className="status error">{error}</div>}

            <main className="jobs-container">
                {displayedJobs.length === 0 && !loading ? (
                    <div className="no-jobs">No jobs found. Try a different search.</div>
                ) : (
                    <ul className="jobs-list">
                        {displayedJobs.map((job, index) => (
                            <li key={job.id || index} className="job-card">
                                <h2 className="job-title">{job.title}</h2>
                                <div className="job-meta">
                                    <span className="job-company">{job.company}</span>
                                    <span className="job-location">{job.location}</span>
                                </div>
                                <p className="job-description">{job.description}</p>
                                <a href={job.url} target="_blank" rel="noopener noreferrer" className="view-job-button">
                                    View Job
                                </a>
                            </li>
                        ))}
                    </ul>
                )}
            </main>

            <footer className="app-footer">
                <p>© {new Date().getFullYear()} Job Aggregator - Search across multiple job sites</p>
            </footer>
        </div>
    );
}

export default App;
