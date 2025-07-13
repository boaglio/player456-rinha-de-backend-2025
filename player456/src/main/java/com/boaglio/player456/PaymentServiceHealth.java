package com.boaglio.player456;

public record PaymentServiceHealth(
    Boolean failing,
    Long  minResponseTime
) {
}
