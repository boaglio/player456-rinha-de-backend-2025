package com.boaglio.player456;

public record Payment(
        String correlationId,
        Double amount
) {
}
