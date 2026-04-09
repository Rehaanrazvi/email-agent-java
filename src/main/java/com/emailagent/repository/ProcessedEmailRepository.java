package com.emailagent.repository;

import com.emailagent.model.ProcessedEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessedEmailRepository extends JpaRepository<ProcessedEmail, Long> {

    // Check if email already processed for this user
    boolean existsByUserIdAndEmailId(String userId, String emailId);

    // Get all logs for a user — dashboard will call this
    List<ProcessedEmail> findByUserIdOrderByProcessedAtDesc(String userId);

    // Stats for dashboard
    long countByUserId(String userId);
    long countByUserIdAndAction(String userId, String action);
    long countByUserIdAndDecisionSource(String userId, String decisionSource);
}