package com.boaglio.player456;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import static com.boaglio.player456.LogUtil.log;

@RestController
public class Controllers {

    private final PaymentService paymentService;

    public Controllers(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public ResponseEntity<Void>  fazPagamento(@RequestBody Payment payment) {

//        log(payment.toString());

//        TimerUtil timer = new TimerUtil();
        paymentService.processaPagamento(payment.correlationId(),String.valueOf(payment.amount()));
//        timer.logElapsedTime("Payment Service");

        return ResponseEntity.ok().build();
    }

    @GetMapping("/payments-summary")
    public ResponseEntity<String> resumoPagamentos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date must be before 'to' date");
        }
        log("Payments Summary - from:%s to:%s".formatted(from,to));

        var timer = new TimerUtil();
        PaymentSummary summary = paymentService.getSummary(from, to);

        var responseSummary = """
                {
                    "default" : {
                        "totalRequests": %d,
                        "totalAmount": %.2f
                    },
                    "fallback" : {
                        "totalRequests": %d,
                        "totalAmount": %.2f
                    }
                }
                """.formatted(summary.totalRequests(),summary.totalAmount(),summary.totalRequestsFallback(),summary.totalAmountFallback());

        timer.logElapsedTime("Payment Summary");

        log("Payments Summary Response - %s".formatted(responseSummary));

        return ResponseEntity.ok(responseSummary);
    }

    @GetMapping("/pagamentos")
    public ResponseEntity<List<PaymentRecord>> getAllPagamentos() {
        List<PaymentRecord> records = paymentService.getAllPagamentos();
        return ResponseEntity.ok(records);
    }

    @GetMapping("/pagamentos-fallback")
    public ResponseEntity<List<PaymentRecord>> getAllPagamentosFallback() {
        List<PaymentRecord> records = paymentService.getAllPagamentosFallback();
        return ResponseEntity.ok(records);
    }

    @GetMapping("/pagamentos-retry-count")
    public ResponseEntity<Long> getCountRetryPagamentos() {
        var count = paymentService.getCountRetryPagamentos();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/pagamentos-retry")
    public ResponseEntity<List<PaymentRetry>> getRetryPagamentos() {
        var count = paymentService.getAllPagamentosRetry();
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/pagamentos-retry")
    public ResponseEntity<Long> truncateRetryPagamentos() {
        paymentService.truncateRetryPagamentos();
        return ResponseEntity.ok().build();
    }

}