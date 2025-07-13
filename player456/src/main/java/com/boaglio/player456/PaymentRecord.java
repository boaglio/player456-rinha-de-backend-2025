package com.boaglio.player456;

import java.time.ZonedDateTime;

public record PaymentRecord(
    long id,
    double amount,
    ZonedDateTime dtTransaction
) {
}