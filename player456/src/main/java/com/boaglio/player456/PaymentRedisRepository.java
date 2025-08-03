package com.boaglio.player456;

import com.boaglio.player456.dto.PaymentRecord;
import com.boaglio.player456.dto.PaymentRetry;
import com.boaglio.player456.dto.PaymentSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
public class PaymentRedisRepository implements PaymentRepository {

    private static final Logger logger = LoggerFactory.getLogger(PaymentRedisRepository.class);
    private static final String KEY_DEFAULT_PAYMENTS = "payments:default:zset";
    private static final String KEY_DEFAULT_ID = "payments:default:id";
    private static final String KEY_FALLBACK_PAYMENTS = "payments:fallback:zset";
    private static final String KEY_FALLBACK_ID = "payments:fallback:id";
    private static final String KEY_RETRY_PAYMENTS = "payments:retry";
    private static final String KEY_RETRY_SET = "payments:retry:set";
    private static final String KEY_RETRY_ID = "payments:retry:id";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public PaymentRedisRepository(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void addDefaultPayment(String amount) {
        try {
            float parsedAmount = Float.parseFloat(amount.trim());
            long id = redisTemplate.opsForValue().increment(KEY_DEFAULT_ID, 1);
            ZonedDateTime timestamp = ZonedDateTime.now(ZoneId.of("UTC"));
            PaymentRecord record = new PaymentRecord(id, parsedAmount, timestamp);
            String json = objectMapper.writeValueAsString(record);
            double score = timestamp.toEpochSecond();
            redisTemplate.opsForZSet().add(KEY_DEFAULT_PAYMENTS, json, score);
        } catch (Exception e) {
            logger.error("Failed to add default payment: amount={}", amount, e);
            throw new IllegalArgumentException("Failed to add default payment: " + amount, e);
        }
    }

    @Override
    public void addFallbackPayment(String amount) {
        try {
            float parsedAmount = Float.parseFloat(amount.trim());
            long id = redisTemplate.opsForValue().increment(KEY_FALLBACK_ID, 1);
            ZonedDateTime timestamp = ZonedDateTime.now(ZoneId.of("UTC"));
            PaymentRecord record = new PaymentRecord(id, parsedAmount, timestamp);
            String json = objectMapper.writeValueAsString(record);
            double score = timestamp.toEpochSecond();
            redisTemplate.opsForZSet().add(KEY_FALLBACK_PAYMENTS, json, score);
        } catch (Exception e) {
            logger.error("Failed to add fallback payment: amount={}", amount, e);
            throw new IllegalArgumentException("Failed to add fallback payment: " + amount, e);
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void addRetryPayment(String correlationId, String amount) {
        if (correlationId == null || amount == null) {
            throw new IllegalArgumentException("correlationId and amount cannot be null");
        }

        String trimmedCorrelationId = correlationId.trim();
        String trimmedAmount = amount.trim();

        try {
            Float.parseFloat(trimmedAmount);
            String retryKey = trimmedCorrelationId + ":" + trimmedAmount;
            Boolean exists = redisTemplate.opsForSet().isMember(KEY_RETRY_SET, retryKey);

            if (!Boolean.TRUE.equals(exists)) {
                long id = redisTemplate.opsForValue().increment(KEY_RETRY_ID, 1);
                PaymentRetry retry = new PaymentRetry(id, trimmedCorrelationId, trimmedAmount);
                String json = objectMapper.writeValueAsString(retry);
                redisTemplate.opsForList().rightPush(KEY_RETRY_PAYMENTS, json);
                redisTemplate.opsForSet().add(KEY_RETRY_SET, retryKey);
            }
        } catch (Exception e) {
            logger.error("Failed to add retry payment: correlationId={}, amount={}", correlationId, amount, e);
            throw new IllegalArgumentException("Failed to add retry payment: " + amount, e);
        }
    }

    @Override
    public void truncateRetryPayment() {
        redisTemplate.delete(KEY_RETRY_PAYMENTS);
        redisTemplate.delete(KEY_RETRY_SET);
        redisTemplate.delete(KEY_RETRY_ID);
    }

    @Override
    public List<PaymentRetry> getPaymentForRetry() {
        String json = redisTemplate.opsForList().leftPop(KEY_RETRY_PAYMENTS);
        if (json == null) {
            return List.of();
        }
        try {
            PaymentRetry retry = objectMapper.readValue(json, PaymentRetry.class);
            String retryKey = retry.correlationId() + ":" + retry.amount();
            redisTemplate.opsForSet().remove(KEY_RETRY_SET, retryKey);
            return List.of(retry);
        } catch (Exception e) {
            logger.error("Failed to deserialize PaymentRetry: json={}", json, e);
            throw new IllegalStateException("Failed to deserialize PaymentRetry: " + json, e);
        }
    }

    @Override
    public PaymentSummary getSummary(LocalDateTime from, LocalDateTime to) {
        ZonedDateTime fromZoned = from.atZone(ZoneId.of("UTC"));
        ZonedDateTime toZoned = to.atZone(ZoneId.of("UTC"));

        if (fromZoned.isAfter(toZoned)) {
            throw new IllegalArgumentException("'from' date must be before 'to' date");
        }

        double fromScore = fromZoned.toEpochSecond();
        double toScore = toZoned.toEpochSecond();

        // Default payments
        double totalAmount = 0.0;
        long totalRequests = 0;
        Set<String> defaultPayments = redisTemplate.opsForZSet().rangeByScore(KEY_DEFAULT_PAYMENTS, fromScore, toScore);
        if (defaultPayments != null) {
            for (String json : defaultPayments) {
                try {
                    PaymentRecord record = objectMapper.readValue(json, PaymentRecord.class);
                    totalAmount += record.amount();
                    totalRequests++;
                } catch (Exception e) {
                    logger.warn("Skipping invalid PaymentRecord in default payments: json={}", json, e);
                }
            }
        }

        // Fallback payments
        double totalAmountFallback = 0.0;
        long totalRequestsFallback = 0;
        Set<String> fallbackPayments = redisTemplate.opsForZSet().rangeByScore(KEY_FALLBACK_PAYMENTS, fromScore, toScore);
        if (fallbackPayments != null) {
            for (String json : fallbackPayments) {
                try {
                    PaymentRecord record = objectMapper.readValue(json, PaymentRecord.class);
                    totalAmountFallback += record.amount();
                    totalRequestsFallback++;
                } catch (Exception e) {
                    logger.warn("Skipping invalid PaymentRecord in fallback payments: json={}", json, e);
                }
            }
        }

        return new PaymentSummary(totalRequests, totalAmount, totalRequestsFallback, totalAmountFallback);
    }

    @Override
    public List<PaymentRecord> getAllPagamentos() {
        Set<String> jsons = redisTemplate.opsForZSet().range(KEY_DEFAULT_PAYMENTS, 0, -1);
        List<PaymentRecord> records = new ArrayList<>();
        if (jsons != null) {
            for (String json : jsons) {
                try {
                    records.add(objectMapper.readValue(json, PaymentRecord.class));
                } catch (Exception e) {
                    logger.warn("Skipping invalid PaymentRecord in default payments: json={}", json, e);
                }
            }
        }
        return records;
    }

    @Override
    public List<PaymentRecord> getAllPagamentosFallback() {
        Set<String> jsons = redisTemplate.opsForZSet().range(KEY_FALLBACK_PAYMENTS, 0, -1);
        List<PaymentRecord> records = new ArrayList<>();
        if (jsons != null) {
            for (String json : jsons) {
                try {
                    records.add(objectMapper.readValue(json, PaymentRecord.class));
                } catch (Exception e) {
                    logger.warn("Skipping invalid PaymentRecord in fallback payments: json={}", json, e);
                }
            }
        }
        return records;
    }

    @Override
    public Long getCountRetryPagamentos() {
        Long count = redisTemplate.opsForList().size(KEY_RETRY_PAYMENTS);
        return count != null ? count : 0L;
    }

    @Override
    public List<PaymentRetry> getAllPagamentosRetry() {
        List<String> jsons = redisTemplate.opsForList().range(KEY_RETRY_PAYMENTS, 0, -1);
        List<PaymentRetry> retries = new ArrayList<>();
        if (jsons != null) {
            for (String json : jsons) {
                try {
                    retries.add(objectMapper.readValue(json, PaymentRetry.class));
                } catch (Exception e) {
                    logger.warn("Skipping invalid PaymentRetry: json={}", json, e);
                }
            }
        }
        return retries;
    }
}