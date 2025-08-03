package com.boaglio.player456.old;

import com.boaglio.player456.PaymentRedisRepository;
import com.boaglio.player456.PaymentRepository;
import com.boaglio.player456.dto.PaymentRetry;
import com.boaglio.player456.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
//@EnableScheduling
public class RetryPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(RetryPaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public RetryPaymentService(PaymentRedisRepository paymentRepository, PaymentService paymentService) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    @Value("${ENABLE_SCHEDULING:false}")
    private boolean enableScheduling;

//    @Scheduled(fixedRate = RETRY_RATE_IN_MS)
    @Transactional
    public void checkPaymentsForRetry() {

        if (!enableScheduling) {
            return;
        }
//        var timer = new TimerUtil();
        List<PaymentRetry> retry = paymentRepository.getPaymentForRetry();
        if (Objects.nonNull(retry)&&!retry.isEmpty()) {

            retry.forEach( p -> {
         //     logger.info("{} - Payment for retry: id={}, correlationId={}, amount={}", retryCount++, p.id(), p.correlationId(), p.amount());
                paymentService.processaPagamento(p.correlationId(), p.amount());
            });

//            timer.logElapsedTime("Payment Summary");

        }
    }
}