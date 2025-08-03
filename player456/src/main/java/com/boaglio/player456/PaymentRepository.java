package com.boaglio.player456;

import com.boaglio.player456.dto.PaymentRecord;
import com.boaglio.player456.dto.PaymentRetry;
import com.boaglio.player456.dto.PaymentSummary;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository {

    void addDefaultPayment(String amount);

    void addFallbackPayment(String amount);

    void truncateRetryPayment();

    void addRetryPayment(String correlationId, String amount);

    List<PaymentRetry> getPaymentForRetry();

    PaymentSummary getSummary(LocalDateTime from, LocalDateTime to);

    List<PaymentRecord> getAllPagamentos();

    List<PaymentRecord> getAllPagamentosFallback();

    Long getCountRetryPagamentos();

    List<PaymentRetry> getAllPagamentosRetry();
}
