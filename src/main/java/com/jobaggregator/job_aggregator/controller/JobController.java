package com.jobaggregator.job_aggregator.controller;

import com.jobaggregator.job_aggregator.model.Job;
import com.jobaggregator.job_aggregator.repository.JobRepository;
import com.jobaggregator.job_aggregator.service.JobSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
@CrossOrigin(origins = "http://localhost:3000") // Allow requests from the React
public class JobController {

    private final JobRepository jobRepository;

    @Autowired
    private JobSearchService jobSearchService;

    public JobController(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @GetMapping
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @GetMapping("/search")
    public List<Job> searchJobs(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String location) {

        // If both query and location are given, scrape new jobs
        if (query != null && !query.isEmpty() && location != null && !location.isEmpty()) {
            return jobSearchService.searchJobs(query, location);
        }

        // If only query is given, search in existing jobs
        if (query != null && !query.isEmpty()) {
            return jobSearchService.searchJobsByKeyword(query);
        }

        // Default: return all jobs
        return jobRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteJob(@PathVariable Long id) {
        jobRepository.deleteById(id);
    }
}
