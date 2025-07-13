package com.boaglio.player456;

public record PaymentRetry(
    long id,
    String correlationId,
    String amount
) {
}