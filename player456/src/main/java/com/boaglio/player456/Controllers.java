package com.boaglio.player456;

import com.boaglio.player456.dto.Payment;
import com.boaglio.player456.dto.PaymentRecord;
import com.boaglio.player456.dto.PaymentSummary;
import com.boaglio.player456.util.TimerUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.boaglio.player456.util.LogUtil.log;

@RestController
public class Controllers {

    static int calls = 1 ;

    private final PaymentService paymentService;

    public Controllers(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public Mono<ResponseEntity<Void>> fazPagamentoReactive(@RequestBody Payment paymentRequest) {
        return Mono.fromRunnable(() ->
                paymentService.processaPagamento(
                                paymentRequest.correlationId(),
                                String.valueOf(paymentRequest.amount())
                        ));

    }

    @GetMapping("/payments-summary")
    public ResponseEntity<String> resumoPagamentos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        log("- Payments Summary PARAM - from:%s to:%s".formatted(from,to));
        if (Objects.isNull(from)) {
            from = LocalDateTime.now();
        }

        if (Objects.isNull(to)) {
            to = LocalDateTime.now();
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date must be before 'to' date");
        }
        log("%d - Payments Summary - from:%s to:%s".formatted(calls,from,to));

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

        log("%d - Payments Summary Response - %s".formatted(calls++,responseSummary));

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

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("UP");
    }

}