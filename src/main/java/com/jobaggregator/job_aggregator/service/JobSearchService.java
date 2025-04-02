package com.jobaggregator.job_aggregator.service;

import com.jobaggregator.job_aggregator.model.Job;
import com.jobaggregator.job_aggregator.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class JobSearchService {

    @Autowired
    private JobScraperService scraperService;

    @Autowired
    private JobRepository jobRepository;

    public List<Job> searchJobs(String query, String location) {
        List<Job> results = new ArrayList<>();

        // Search jobs from multiple sources at same time
        CompletableFuture<List<Job>> indeedFuture = CompletableFuture.supplyAsync(() ->
                scraperService.scrapeIndeedJobs(query, location)
        );

        CompletableFuture<List<Job>> linkedInFuture = CompletableFuture.supplyAsync(() ->
                scraperService.scrapeLinkedInJobs(query, location)
        );

        // Wait for all futures to complete and combine the results
        CompletableFuture.allOf(indeedFuture, linkedInFuture).join();

        try {
            results.addAll(indeedFuture.get());
            results.addAll(linkedInFuture.get());

            // Save all results to the database
            jobRepository.saveAll(results);

        } catch (Exception e) {
            System.err.println("Error combining job search results: " + e.getMessage());
        }

        return results;
    }

    public List<Job> searchJobsByKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return jobRepository.findAll();
        }

        String lowercaseKeyword = keyword.toLowerCase();

        // Filter jobs from the repository based on keyword
        return jobRepository.findAll().stream()
                .filter(job ->
                        job.getTitle().toLowerCase().contains(lowercaseKeyword) ||
                                job.getDescription().toLowerCase().contains(lowercaseKeyword) ||
                                job.getCompany().toLowerCase().contains(lowercaseKeyword)
                )
                .collect(Collectors.toList());
    }
}