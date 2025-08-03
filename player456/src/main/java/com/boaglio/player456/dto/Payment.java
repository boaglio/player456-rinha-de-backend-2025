package com.boaglio.player456.dto;

public record Payment(
        String correlationId,
        Double amount
) {
}
