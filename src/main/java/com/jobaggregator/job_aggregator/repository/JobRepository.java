package com.jobaggregator.job_aggregator.repository;

import com.jobaggregator.job_aggregator.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
}
