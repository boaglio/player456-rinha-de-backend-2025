package com.boaglio.player456.old;

import com.boaglio.player456.PaymentRedisRepository;
import com.boaglio.player456.PaymentRepository;
import com.boaglio.player456.dto.PaymentRecord;
import com.boaglio.player456.dto.PaymentRetry;
import com.boaglio.player456.dto.PaymentSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

//import static com.boaglio.player456.Player456Application.RETRY_TIMES;
//import static com.boaglio.player456.Player456Application.SERVICE_TIMEOUT_DEFAULT_IN_MS;
import static com.boaglio.player456.util.LogUtil.logError;

//@Service
public class OldPaymentService {
//
//    public enum PAYMENT_SERVER { DEFAULT , FALLBACK}
//
//    @Value("${payment.default}")
//    private String paymentDefaultServer;
//
////    @Value("${payment.fallback}")
////    private String paymentFallbackServer;
//
//    private final PaymentRepository paymentRepository;
//    private final WebClient restClientPaymentDefault;
////    private final WebClient restClientPaymentFallback;
//
//    public OldPaymentService(PaymentRedisRepository paymentRepository ) {
//        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofMillis(SERVICE_TIMEOUT_DEFAULT_IN_MS));
//        this.paymentRepository = paymentRepository;
////        this.restClientPaymentDefault = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
//        this.restClientPaymentDefault = WebClient.create();
////        this.restClientPaymentFallback = restClientBuilder.requestFactory(new HttpClientRequestFactory(Duration.ofMillis(SERVICE_TIMEOUT_FALLBACK_IN_MS))).build();
//    }
//
////    @Async
//    public Mono<Void> processaPagamento(String correlationId, String amount) {
//
//        // Create JSON request body
//        String req = """
//                {
//                 "correlationId": "%s",
//                 "amount": %s
//                }
//                """.formatted(correlationId, amount);
//
//        //log("Using default payment server: %s".formatted(paymentDefaultServer));
//        //log("Request body: %s".formatted(req));
//
//        // Default server
////        ResponseEntity<String> response;
//        Mono<String> response;
//        try {
//            restClientPaymentDefault.post()
//                    .uri(new URI(paymentDefaultServer + "/payments"))
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .bodyValue(req)
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .retry(RETRY_TIMES);
//            paymentRepository.addDefaultPayment(amount);
//        } catch (Exception e) {
//            logError("Default server request failed: %s".formatted(e.getMessage()));
//            //response = ResponseEntity.status(500).body("Error: " + e.getMessage());
////            if (response.getBody().contains("already exists")) {
//                return Mono.empty();
////            }
//        }
//
////        if (response.getStatusCode().is2xxSuccessful()) {
//
//       //     var resp = response.getBody();
//      //      log("Default server response: %s".formatted(resp));
////            TimerUtil timer = new TimerUtil();
//    //         paymentRepository.addDefaultPayment(amount);
////            timer.logElapsedTime("Payment Repository - default");
//
////        } else {
//
//       //     log("Default server error: %s".formatted(response.getStatusCode()));
//
//            // Fallback
//     //       log("Falling back to payment server: %s".formatted(paymentFallbackServer));
////            ResponseEntity<String> responseFallback = null;
////            try {
////                responseFallback = restClientPaymentFallback.post()
////                        .uri(new URI(paymentFallbackServer + "/payments"))
////                        .contentType(MediaType.APPLICATION_JSON)
////                        .body(req)
////                        .retrieve()
////                        .toEntity(String.class);
////            } catch (Exception e) {
////                logError("Fallback server request failed: %s".formatted(e.getMessage()));
////                responseFallback = ResponseEntity.status(500).body("Error: " + e.getMessage());
////                if (responseFallback.getBody().contains("already exists")) {
////                    return;
////                }
////            }
//
////            if (Objects.nonNull(responseFallback) &&
////                Objects.nonNull(responseFallback.getStatusCode()) &&
////                    responseFallback.getStatusCode().is2xxSuccessful()) {
//////                var resp = responseFallback.getBody();
//////                log("Fallback server response: %s".formatted(resp));
//////                TimerUtil timer = new TimerUtil();
////                paymentRepository.addFallbackPayment(amount);
//////                timer.logElapsedTime("Payment Repository - Fallback");
////            } else {
////                logErrorBothServers("both server error - retry! ");
////                paymentRepository.addRetryPayment(correlationId,amount);
////            }
////        }
//        return Mono.empty();
//    }
//
//    public PaymentSummary getSummary(LocalDateTime from, LocalDateTime to) {
//        return paymentRepository.getSummary(from,to);
//    }
//
//    public List<PaymentRecord> getAllPagamentos() {
//        return paymentRepository.getAllPagamentos();
//    }
//
//    public List<PaymentRecord> getAllPagamentosFallback() {
//        return paymentRepository.getAllPagamentosFallback();
//    }
//
//    public List<PaymentRetry> getAllPagamentosRetry() {
//        return paymentRepository.getAllPagamentosRetry();
//    }
//
//    public Long getCountRetryPagamentos() {
//        return paymentRepository.getCountRetryPagamentos();
//    }
//
//    public void truncateRetryPagamentos() {
//        paymentRepository.truncateRetryPayment();;
//    }
//
////    public PAYMENT_SERVER pegaServidorPagamento() {
////
////        var PH = "/payments/service-health";
////
////        ResponseEntity<PaymentServiceHealth> responsePaymentDefault = restClientPaymentDefault.get()
////                .uri(paymentDefaultServer+PH)
////                .retrieve()
////                .toEntity(PaymentServiceHealth.class);
////
////        log("PH 1 = %s".formatted(responsePaymentDefault));
////
////        ResponseEntity<PaymentServiceHealth> responsePaymentFallback = restClientPaymentFallback.get()
////                .uri(paymentFallbackServer+PH)
////                .retrieve()
////                .toEntity(PaymentServiceHealth.class);
////
////        log("PH 2 = %s".formatted(responsePaymentFallback));
////
////        if (Objects.nonNull(responsePaymentDefault.getBody()) && Objects.nonNull(responsePaymentDefault.getBody().failing())) {
////            return PAYMENT_SERVER.DEFAULT;
////        }
////
////        if (Objects.nonNull(responsePaymentFallback.getBody()) && Objects.nonNull(responsePaymentFallback.getBody().failing())) {
////            return PAYMENT_SERVER.FALLBACK;
////        }
////
////        if (responsePaymentDefault.getBody().minResponseTime()>responsePaymentFallback.getBody().minResponseTime()) {
////            return PAYMENT_SERVER.FALLBACK;
////        }
////
////        return PAYMENT_SERVER.DEFAULT;
////
////    }

}