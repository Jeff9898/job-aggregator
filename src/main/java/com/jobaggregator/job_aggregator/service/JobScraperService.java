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

    // For debugging
    private void logHtml(Document doc) {
        System.out.println("HTML structure for debugging:");
        System.out.println(doc.outerHtml().substring(0, 500) + "...");
    }

    public List<Job> scrapeIndeedJobs(String query, String location) {
        List<Job> jobs = new ArrayList<>();

        try {
            // Format URL for Indeed search
            String searchUrl = "https://www.indeed.com/jobs?q=" + query.replace(" ", "+") +
                    "&l=" + location.replace(" ", "+");

            System.out.println("Scraping URL: " + searchUrl);

            // Connect to the website with headers to mimic a browser
            Document doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .timeout(10000)
                    .get();

            // For debugging
            System.out.println("Connected to Indeed. Page title: " + doc.title());

            // Find job listings - try different selector patterns since sites update frequently
            Elements jobElements = doc.select("div.job_seen_beacon, div.jobsearch-ResultsList div.result");

            System.out.println("Found " + jobElements.size() + " job elements");

            if (jobElements.isEmpty()) {
                // If no jobs found, print little bit of the HTML for debugging
                logHtml(doc);
            }

            for (Element jobElement : jobElements) {
                try {
                    // Try multiple selectors since the sites change
                    String title = getTextFromElement(jobElement, "h2.jobTitle, h2.title, a.jobtitle");
                    String company = getTextFromElement(jobElement, "span.companyName, span.company, div.company");
                    String jobLocation = getTextFromElement(jobElement, "div.companyLocation, span.location");
                    String description = getTextFromElement(jobElement, "div.job-snippet, span.summary");

                    // Extract job URL - try different attributes
                    String url = "";
                    Element linkElement = jobElement.selectFirst("a.jcs-JobTitle, a.jobtitle");
                    if (linkElement != null) {
                        String href = linkElement.attr("href");
                        if (href.startsWith("/")) {
                            url = "https://www.indeed.com" + href;
                        } else {
                            url = href;
                        }
                    } else {
                        // Try to get job ID
                        String jobId = jobElement.attr("data-jk");
                        if (!jobId.isEmpty()) {
                            url = "https://www.indeed.com/viewjob?jk=" + jobId;
                        }
                    }

                    if (!title.isEmpty() && !company.isEmpty()) {
                        Job job = new Job(title, company, jobLocation, description, url);
                        jobs.add(job);
                        System.out.println("Added job: " + title + " at " + company);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing job: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error scraping Indeed: " + e.getMessage());
            e.printStackTrace();
        }

        return jobs;
    }

    // Helper method to get text from an element
    private String getTextFromElement(Element parent, String selector) {
        Element element = parent.selectFirst(selector);
        return element != null ? element.text() : "";
    }

    public List<Job> scrapeLinkedInJobs(String query, String location) {
        List<Job> jobs = new ArrayList<>();

        try {
            // Format the URL for Linkedin search
            String searchUrl = "https://www.linkedin.com/jobs/search/?keywords=" +
                    query.replace(" ", "%20") +
                    "&location=" + location.replace(" ", "%20");

            System.out.println("Scraping URL: " + searchUrl);

            Document doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .timeout(10000)
                    .get();

            System.out.println("Connected to LinkedIn. Page title: " + doc.title());

            // LinkedIn is proving to be tricky to scrape due to dynamic content
            Elements jobElements = doc.select("li.jobs-search-results__list-item, div.base-card");

            System.out.println("Found " + jobElements.size() + " LinkedIn job elements");

            if (jobElements.isEmpty()) {
                // If no jobs found, log HTML for debugging
                logHtml(doc);
            }

            for (Element jobElement : jobElements) {
                try {
                    String title = getTextFromElement(jobElement, "h3.base-search-card__title, h3.job-search-card__title");
                    String company = getTextFromElement(jobElement, "h4.base-search-card__subtitle, a.job-search-card__subtitle-link");
                    String jobLocation = getTextFromElement(jobElement, "span.job-search-card__location, div.base-search-card__metadata");

                    // Get job URL
                    Element linkElement = jobElement.selectFirst("a.base-card__full-link, a.job-card-container__link");
                    String url = linkElement != null ? linkElement.attr("href") : "";

                    // For LinkedIn get the full description from the job page
                    String description = "Click the link to view full description";

                    if (!title.isEmpty() && !company.isEmpty()) {
                        Job job = new Job(title, company, jobLocation, description, url);
                        jobs.add(job);
                        System.out.println("Added LinkedIn job: " + title + " at " + company);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing LinkedIn job: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error scraping LinkedIn: " + e.getMessage());
            e.printStackTrace();
        }

        return jobs;
    }

    // method to test if scraping is working
    public void testScraping() {
        try {
            System.out.println("Testing scraper connection...");
            Document doc = Jsoup.connect("https://www.indeed.com")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();
            System.out.println("Successfully connected to Indeed! Page title: " + doc.title());
        } catch (Exception e) {
            System.err.println("Scraper test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}