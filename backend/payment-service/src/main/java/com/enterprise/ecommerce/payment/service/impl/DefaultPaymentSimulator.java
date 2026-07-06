package com.enterprise.ecommerce.payment.service.impl;

import com.enterprise.ecommerce.payment.service.PaymentSimulator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class DefaultPaymentSimulator implements PaymentSimulator {

    private static final double SUCCESS_THRESHOLD = 0.9;

    private final Random random;

    @Value("${app.payment.force-fail:false}")
    private boolean forceFail;

    public DefaultPaymentSimulator(Random random) {
        this.random = random;
    }

    @Override
    public boolean simulateSuccess() {
        return !forceFail && random.nextDouble() < SUCCESS_THRESHOLD;
    }
}
