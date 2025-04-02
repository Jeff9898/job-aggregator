package com.jobaggregator.job_aggregator.controller;

import com.jobaggregator.job_aggregator.model.Job;
import com.jobaggregator.job_aggregator.service.JobScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "http://localhost:3000")
public class TestController {

    @Autowired
    private JobScraperService scraperService;

    @GetMapping("/scrape-indeed")
    public List<Job> testIndeedScraper(
            @RequestParam(defaultValue = "java developer") String query,
            @RequestParam(defaultValue = "remote") String location) {

        System.out.println("Testing Indeed scraper with query: " + query + ", location: " + location);
        return scraperService.scrapeIndeedJobs(query, location);
    }

    @GetMapping("/scrape-linkedin")
    public List<Job> testLinkedinScraper(
            @RequestParam(defaultValue = "java developer") String query,
            @RequestParam(defaultValue = "remote") String location) {

        System.out.println("Testing LinkedIn scraper with query: " + query + ", location: " + location);
        return scraperService.scrapeLinkedInJobs(query, location);
    }

    @GetMapping("/test-connection")
    public String testConnection() {
        scraperService.testScraping();
        return "Check console for results";
    }
}