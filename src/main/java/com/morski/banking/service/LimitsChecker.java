package com.morski.banking.service;

import com.morski.banking.exception.LimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.Map;

@Service
public class LimitsChecker {
    private static final Logger log = LoggerFactory.getLogger(LimitsChecker.class);
    private final RestTemplate restTemplate;
    private final String limitsCheckUrl;

    public LimitsChecker(@Value("${limits.check.url}") String limitsCheckUrl) {
        this.restTemplate = new RestTemplate();
        this.limitsCheckUrl = limitsCheckUrl;
    }

    public void checkLimit(BigDecimal amount) {
        Map<String, Object> request = Map.of("amount", amount);
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(limitsCheckUrl, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Boolean allowed = (Boolean) response.getBody().get("allowed");
                if (allowed == null || !allowed) {
                    throw new LimitExceededException("Transaction amount exceeds allowed limit: " + amount);
                }
            } else {
                throw new LimitExceededException("Limits service unavailable");
            }
        } catch (Exception e) {
            log.error("Limit check failed", e);
            throw new LimitExceededException("Failed to verify limit: " + e.getMessage());
        }
    }
}