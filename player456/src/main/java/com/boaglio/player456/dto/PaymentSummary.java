package com.boaglio.player456.dto;

public record PaymentSummary(
   long totalRequests,
   double  totalAmount,
   long totalRequestsFallback,
   double  totalAmountFallback
   ) {
}
