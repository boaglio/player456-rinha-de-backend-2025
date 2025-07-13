package com.boaglio.player456;

public record PaymentSummary(
   long totalRequests,
   double  totalAmount,
   long totalRequestsFallback,
   double  totalAmountFallback
   ) {
}
