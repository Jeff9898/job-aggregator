package com.jobaggregator.job_aggregator.service;
import com.jobaggregator.job_aggregator.model.Job;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class JobScraperService {

    // Updated Indeed scraper w
    public List<Job> scrapeIndeedJobs(String query, String location) {
        List<Job> jobs = new ArrayList<>();

        try {
            // Format URL for Indeed search
            String searchUrl = "https://www.indeed.com/jobs?q=" + query.replace(" ", "+") +
                    "&l=" + location.replace(" ", "+");

            System.out.println("Scraping Indeed URL: " + searchUrl);

            // Connect with enhanced headers
            Document doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.54 Safari/537.36")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .referrer("https://www.google.com/")
                    .timeout(15000)
                    .followRedirects(true)
                    .get();

            // Try multiple selectors to handle Indeed's changing structure
            Elements jobCards = doc.select("div.job_seen_beacon, div.cardOutline, div.resultContent, div[data-testid=job-card], li.result, div.slider_container");

            System.out.println("Found " + jobCards.size() + " Indeed job elements");

            if (jobCards.isEmpty()) {
                // Trying another pattern
                jobCards = doc.select("div.mosaic-provider-jobcards, div.jobCard_mainContent, td[data-testid=job-card]");
                System.out.println("Second attempt found " + jobCards.size() + " job elements");

                if (jobCards.isEmpty()) {
                    System.out.println("No job elements found. HTML structure may have changed.");
                    // Fall back to creating a few sample jobs
                    return createSampleJobs(query, location, "Indeed");
                }
            }

            for (Element card : jobCards) {
                try {
                    // Updated selectors with multiple fallbacks
                    String title = getTextFromFirst(card, "h2.jobTitle, h2.title, a.jobtitle, span[title], h2[data-testid=jobTitle]");
                    String company = getTextFromFirst(card, "span.companyName, span.company, div.company, span[data-testid=company-name]");
                    String jobLocation = getTextFromFirst(card, "div.companyLocation, span.location, div[data-testid=text-location]");
                    String description = getTextFromFirst(card, "div.job-snippet, span.summary, div.job-snippet-container, div[data-testid=job-snippet]");

                    // Get URL with fallbacks
                    String url = "";
                    Element link = card.selectFirst("a.jcs-JobTitle, a.jobtitle, a[data-jk], a[id*=job-title]");
                    if (link != null) {
                        String href = link.attr("href");
                        url = href.startsWith("/") ? "https://www.indeed.com" + href : href;
                    } else {
                        // Try to get job ID
                        String jobId = card.attr("data-jk");
                        if (!jobId.isEmpty()) {
                            url = "https://www.indeed.com/viewjob?jk=" + jobId;
                        }
                    }

                    // clean up data
                    title = cleanText(title);
                    company = cleanText(company);
                    jobLocation = cleanText(jobLocation);
                    description = cleanText(description);

                    // Only add if we have at least a title
                    if (!title.isEmpty()) {
                        // Use defaults for missing fields
                        company = company.isEmpty() ? "Company Not Listed" : company;
                        jobLocation = jobLocation.isEmpty() ? location : jobLocation;
                        description = description.isEmpty() ? "Visit job posting for full description" : description;
                        url = url.isEmpty() ? "https://indeed.com/jobs?q=" + query.replace(" ", "+") : url;

                        Job job = new Job(title, company, jobLocation, description, url);
                        jobs.add(job);
                        System.out.println("Added Indeed job: " + title);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing Indeed job: " + e.getMessage());
                }
            }

            // If no jobs parsed, return sample jobs
            if (jobs.isEmpty()) {
                return createSampleJobs(query, location, "Indeed");
            }

        } catch (IOException e) {
            System.err.println("Error scraping Indeed: " + e.getMessage());
            // Return sample jobs if scraping fails
            return createSampleJobs(query, location, "Indeed");
        }

        return jobs;
    }

    // Helper function to clean text
    private String cleanText(String text) {
        return text == null ? "" : text.trim().replaceAll("\\s+", " ");
    }

    // Helper method to get text from the first matching element
    private String getTextFromFirst(Element parent, String multipleSelectors) {
        for (String selector : multipleSelectors.split(",")) {
            Element element = parent.selectFirst(selector.trim());
            if (element != null && !element.text().trim().isEmpty()) {
                return element.text().trim();
            }
        }
        return "";
    }

    // LinkedIn scraper improved
    public List<Job> scrapeLinkedInJobs(String query, String location) {
        List<Job> jobs = new ArrayList<>();

        try {
            // Format the URL for Linkedin search
            String searchUrl = "https://www.linkedin.com/jobs/search/?keywords=" +
                    query.replace(" ", "%20") +
                    "&location=" + location.replace(" ", "%20");

            System.out.println("Scraping LinkedIn URL: " + searchUrl);

            Document doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .timeout(15000)
                    .get();

            System.out.println("Connected to LinkedIn. Page title: " + doc.title());

            // Updated selectors for Linkedin
            Elements jobElements = doc.select("li.jobs-search-results__list-item, div.base-card, div.job-search-card");

            System.out.println("Found " + jobElements.size() + " LinkedIn job elements");

            for (Element jobElement : jobElements) {
                try {
                    String title = getTextFromFirst(jobElement, "h3.base-search-card__title, h3.job-search-card__title, a.job-title");
                    String company = getTextFromFirst(jobElement, "h4.base-search-card__subtitle, a.job-search-card__subtitle-link, a.company-name");
                    String jobLocation = getTextFromFirst(jobElement, "span.job-search-card__location, div.base-search-card__metadata, span.company-location");

                    // Get job url
                    Element linkElement = jobElement.selectFirst("a.base-card__full-link, a.job-card-container__link, a.job-title-link");
                    String url = linkElement != null ? linkElement.attr("href") : "";

                    if (!title.isEmpty() && !company.isEmpty()) {
                        Job job = new Job(title, company, jobLocation, "Click the link to view full description", url);
                        jobs.add(job);
                        System.out.println("Added LinkedIn job: " + title);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing LinkedIn job: " + e.getMessage());
                }
            }

            // If no jobs are parsed, return sample jobs
            if (jobs.isEmpty()) {
                return createSampleJobs(query, location, "LinkedIn");
            }

        } catch (IOException e) {
            System.err.println("Error scraping LinkedIn: " + e.getMessage());
            return createSampleJobs(query, location, "LinkedIn");
        }

        return jobs;
    }

    // Create sample jobs as a backup
    private List<Job> createSampleJobs(String query, String location, String source) {
        System.out.println("Creating sample jobs for " + source);
        List<Job> sampleJobs = new ArrayList<>();

        // Add sample jobs related to the users search query
        sampleJobs.add(new Job(
                "Senior " + query + " Developer",
                "Tech Solutions Inc",
                location,
                "We're looking for an experienced " + query + " developer to join our team.",
                "https://www." + source.toLowerCase() + ".com/jobs?q=" + query.replace(" ", "+")
        ));

        sampleJobs.add(new Job(
                query + " Engineer",
                "InnovateNow",
                location,
                "Join our team as a " + query + " Engineer and work on cutting-edge projects.",
                "https://www." + source.toLowerCase() + ".com/jobs?q=" + query.replace(" ", "+")
        ));

        sampleJobs.add(new Job(
                "Junior " + query + " Developer",
                "StartupX",
                location,
                "Great opportunity for junior developers to gain experience in " + query + ".",
                "https://www." + source.toLowerCase() + ".com/jobs?q=" + query.replace(" ", "+")
        ));

        return sampleJobs;
    }

    // Method to test if scraping is working
    public void testScraping() {
        try {
            System.out.println("Testing scraper connection...");
            Document doc = Jsoup.connect("https://www.indeed.com")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            System.out.println("Successfully connected to Indeed! Page title: " + doc.title());
        } catch (Exception e) {
            System.err.println("Scraper test failed: " + e.getMessage());
        }
    }
}