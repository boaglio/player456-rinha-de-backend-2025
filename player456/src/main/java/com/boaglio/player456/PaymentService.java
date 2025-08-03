package com.boaglio.player456;

import com.boaglio.player456.dto.PaymentRecord;
import com.boaglio.player456.dto.PaymentSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Future;

import static com.boaglio.player456.util.LogUtil.*;
import static com.boaglio.player456.Player456Application.*;

@Service
public class PaymentService {

    @Value("${payment.default}")
    private String paymentDefaultServer;

    private final PaymentRepository paymentRepository;
    private final WebClient restClientPaymentDefault;

    public PaymentService(PaymentRedisRepository paymentRepository ) {
        this.paymentRepository = paymentRepository;
        this.restClientPaymentDefault = WebClient.create();
    }

//    @Async
    public Future<Void> processaPagamento(String correlationId, String amount) {

        Future<Void> future = null;
        // Create JSON request body
        String req = """
                {
                 "correlationId": "%s",
                 "amount": %s
                }
                """.formatted(correlationId, amount);

        try {
            restClientPaymentDefault.post()
                    .uri(new URI(paymentDefaultServer + "/payments"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retry(RETRY_TIMES);
            paymentRepository.addDefaultPayment(amount);
        } catch (Exception e) {
            logError("Default server request failed: %s".formatted(e.getMessage()));
            return future;
        }

        return future;
    }

    public PaymentSummary getSummary(LocalDateTime from, LocalDateTime to) {
        return paymentRepository.getSummary(from,to);
    }

    public List<PaymentRecord> getAllPagamentos() {
        return paymentRepository.getAllPagamentos();
    }

    public List<PaymentRecord> getAllPagamentosFallback() {
        return paymentRepository.getAllPagamentosFallback();
    }

}