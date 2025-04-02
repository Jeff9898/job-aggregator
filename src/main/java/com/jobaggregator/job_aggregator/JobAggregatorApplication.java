package com.jobaggregator.job_aggregator;

import com.jobaggregator.job_aggregator.model.Job;
import com.jobaggregator.job_aggregator.repository.JobRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JobAggregatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobAggregatorApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(JobRepository repository) {
		return (args) -> {
			// Save a few job listings to db
			repository.save(new Job(
					"Software Engineer",
					"Indeed",
					"Remote",
					"Develop software using Java and Spring Boot.",
					"https://indeed.com/job/swe"
			));
			repository.save(new Job(
					"Junior Backend Developer",
					"LinkedIn",
					"New York",
					"Work on backend systems and API development.",
					"https://linkedin.com/jobs/jun-be"
			));
		};
	}








}
