package com.enterprise.ecommerce.order.exception;

import org.springframework.http.HttpStatus;

public class ProductUnavailableException extends AppException {

    public ProductUnavailableException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
