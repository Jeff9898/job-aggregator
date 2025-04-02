import React, { useState, useEffect } from "react";

function App() {
    // State variables
    const [jobs, setJobs] = useState([]);
    const [query, setQuery] = useState("");
    const [location, setLocation] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [message, setMessage] = useState("");

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
        } catch (err) {
            setError(`Error testing ${site} scraper: ` + err.message);
            console.error(`Error testing ${site} scraper:`, err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="App">
            <header>
                <h1>Job Search Aggregator</h1>
            </header>

            <div style={{ margin: '20px 0', padding: '10px', backgroundColor: '#f0f0f0' }}>
                <form onSubmit={handleSearch} style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                    <input
                        type="text"
                        placeholder="Job title, keywords, or company"
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                        style={{ padding: '8px', flex: '1' }}
                    />

                    <input
                        type="text"
                        placeholder="City, state, or remote"
                        value={location}
                        onChange={(e) => setLocation(e.target.value)}
                        style={{ padding: '8px', flex: '1' }}
                    />

                    <button type="submit" style={{ padding: '8px 16px', backgroundColor: '#0056b3', color: 'white', border: 'none' }}>
                        Search Jobs
                    </button>
                </form>

                <div style={{ marginTop: '10px', display: 'flex', gap: '10px' }}>
                    <button onClick={() => testScraper('Indeed')} style={{ padding: '6px 12px' }}>Test Indeed Scraper</button>
                    <button onClick={() => testScraper('LinkedIn')} style={{ padding: '6px 12px' }}>Test LinkedIn Scraper</button>
                </div>
            </div>

            {loading && <div style={{ textAlign: 'center', padding: '20px' }}>Loading...</div>}
            {message && <div style={{ textAlign: 'center', padding: '10px' }}>{message}</div>}
            {error && <div style={{ textAlign: 'center', padding: '10px', color: 'red' }}>{error}</div>}

            <main>
                <ul style={{ padding: '0', listStyle: 'none' }}>
                    {jobs.map((job) => (
                        <li key={job.id} style={{ marginBottom: '1rem', borderBottom: '1px solid #ccc', paddingBottom: '1rem' }}>
                            <h2>{job.title}</h2>
                            <p><strong>Company:</strong> {job.company}</p>
                            <p><strong>Location:</strong> {job.location}</p>
                            <p>{job.description}</p>
                            <a href={job.url} target="_blank" rel="noopener noreferrer" style={{ color: '#0056b3' }}>
                                View Job
                            </a>
                        </li>
                    ))}
                </ul>
            </main>
        </div>
    );
}

export default App;
