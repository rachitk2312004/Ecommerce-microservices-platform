package com.enterprise.ecommerce.order.exception;

import org.springframework.http.HttpStatus;

public class OrderCancellationException extends AppException {

    public OrderCancellationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
