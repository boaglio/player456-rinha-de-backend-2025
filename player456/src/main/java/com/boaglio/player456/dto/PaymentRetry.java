package com.boaglio.player456.dto;

public record PaymentRetry(
    long id,
    String correlationId,
    String amount
) {
}