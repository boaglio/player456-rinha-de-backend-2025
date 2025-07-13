package com.boaglio.player456;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@EnableScheduling
public class RetryPaymentService {

    private static long retryCount = 1;
    private static final Logger logger = LoggerFactory.getLogger(RetryPaymentService.class);
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public RetryPaymentService(PaymentRepository paymentRepository, PaymentService paymentService) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    @Scheduled(fixedRate = 100)
    public void checkPaymentsForRetry() {

//        var timer = new TimerUtil();
        List<PaymentRetry> retry = paymentRepository.getPaymentForRetry();
        if (Objects.nonNull(retry)&&!retry.isEmpty()) {

            retry.forEach( p -> {
           //     logger.info("{} - Payment for retry: id={}, correlationId={}, amount={}", retryCount++, p.id(), p.correlationId(), p.amount());
                paymentService.processaPagamento(p.correlationId(), p.amount());
            });

//            timer.logElapsedTime("Payment Summary");

        }
//        else {
//            logger.info("No payments available for retry");
//        }
    }
}