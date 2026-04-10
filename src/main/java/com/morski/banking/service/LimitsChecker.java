package com.morski.banking.service;

import com.morski.banking.exception.LimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@Service
public class LimitsChecker {
    private static final Logger log = LoggerFactory.getLogger(LimitsChecker.class);
    private final WebClient webClient;

    @Autowired
    public LimitsChecker(WebClient limitsWebClient) {
        this.webClient = limitsWebClient;
    }

    public void checkLimit(BigDecimal amount) {
        Map<String, Object> request = Map.of("amount", amount);
        Map response = webClient.post()
                .uri("/limits/check")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        Mono.error(new RuntimeException("Limits service error")))
                .bodyToMono(Map.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .timeout(Duration.ofSeconds(5))
                .block();

        Boolean allowed = (Boolean) response.get("allowed");
        if (allowed == null || !allowed) {
            throw new LimitExceededException("Transaction amount exceeds allowed limit");
        }
    }
}
