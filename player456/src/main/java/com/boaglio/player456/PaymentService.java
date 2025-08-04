package com.boaglio.player456;

import com.boaglio.player456.dto.PaymentRecord;
import com.boaglio.player456.dto.PaymentSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;

import static com.boaglio.player456.util.LogUtil.*;
import static com.boaglio.player456.Player456Application.*;

@Service
public class PaymentService {

    private URI URI_PAYMENTS;

    @Value("${payment.default}")
    private String paymentDefaultServer;

    private final PaymentRepository paymentRepository;
    private final WebClient webClient;

    public PaymentService(PaymentRedisRepository paymentRepository ) throws URISyntaxException {
        this.paymentRepository = paymentRepository;
        this.webClient = WebClient.builder()
                .baseUrl(paymentDefaultServer)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        URI_PAYMENTS = new URI("/payments");
    }

    @Transactional
    public void processaPagamento(String correlationId, String amount) {

        // Create JSON request body
        String req = """
                {
                 "correlationId": "%s",
                 "amount": %s
                }
                """.formatted(correlationId, amount);

        webClient.post()
                .uri(paymentDefaultServer+URI_PAYMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Payment failed: " + errorBody)))
                )
                .toBodilessEntity()
                .then()
                .doOnSuccess(unused -> paymentRepository.addDefaultPayment(amount))
                .doOnError(e -> logError("Default server request failed: " + e.getMessage()))
                .retry(RETRY_TIMES)
                .subscribe();

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