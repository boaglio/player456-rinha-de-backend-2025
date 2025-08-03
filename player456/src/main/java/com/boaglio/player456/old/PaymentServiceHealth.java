package com.boaglio.player456.old;

public record PaymentServiceHealth(
    Boolean failing,
    Long  minResponseTime
) {
}
