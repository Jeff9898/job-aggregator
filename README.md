# Job Aggregator App

A full-stack web application that scrapes job postings from multiple platforms and displays them in a clean, searchable interface. Built to simplify the job hunt by aggregating listings from sites like LinkedIn and Indeed into one streamlined dashboard.

---

## 💡 Features

- ✅ Real-time job scraping using custom-built scrapers
- ✅ Aggregates job listings from LinkedIn (stable) and Indeed (partially supported)
- ✅ Clean and responsive React frontend with keyword search
- ✅ Backend job scraper built with JSoup and Java Spring Boot
- ✅ RESTful API for managing job data
- ✅ Simple UI for browsing and filtering results

---

## 🛠 Technologies Used

**Backend:**
- Java
- Spring Boot / Spring Data JPA
- JSoup (for web scraping)
- Maven

**Frontend:**
- JavaScript
- React
- HTML / CSS

**Database:**
- H2 (in-memory) or configured SQL database (optional for persistence)

---

## 🚀 How It Works

1. Scrapers run and fetch job listings based on predefined keywords or locations.
2. Job data is parsed and stored via Spring Boot API endpoints.
3. The React frontend displays scraped listings in a searchable and user-friendly interface.

---

## 🔍 Current Support

- **LinkedIn**: Fully functional with reliable scraping.
- **Indeed**: Partial support due to anti-scraping protections. Improvements ongoing.
- Future support planned for additional platforms.

---

## 📌 Notes

- Built as a personal project to explore data scraping, backend API design, and full-stack application development.
