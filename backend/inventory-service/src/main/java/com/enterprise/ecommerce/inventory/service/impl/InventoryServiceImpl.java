package com.enterprise.ecommerce.inventory.service.impl;

import com.enterprise.ecommerce.inventory.dto.*;
import com.enterprise.ecommerce.inventory.entity.Inventory;
import com.enterprise.ecommerce.inventory.entity.Reservation;
import com.enterprise.ecommerce.inventory.enums.ReservationStatus;
import com.enterprise.ecommerce.inventory.exception.BadRequestException;
import com.enterprise.ecommerce.inventory.exception.InsufficientStockException;
import com.enterprise.ecommerce.inventory.exception.ResourceNotFoundException;
import com.enterprise.ecommerce.inventory.repository.InventoryRepository;
import com.enterprise.ecommerce.inventory.repository.ReservationRepository;
import com.enterprise.ecommerce.inventory.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                ReservationRepository reservationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getByProductId(Long productId) {
        return toResponse(findInventory(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public CheckAvailabilityResponse checkAvailability(CheckAvailabilityRequest request) {
        Inventory inventory = findInventory(request.getProductId());
        boolean available = inventory.getAvailableQuantity() >= request.getQuantity();
        return CheckAvailabilityResponse.builder().available(available).build();
    }

    @Override
    @Transactional
    public InventoryResponse reserve(InventoryOperationRequest request) {
        Optional<Reservation> existing = reservationRepository.findByOrderIdAndProductId(
                request.getOrderId(), request.getProductId());

        if (existing.isPresent()) {
            Reservation reservation = existing.get();
            if (reservation.getStatus() == ReservationStatus.RESERVED) {
                validateQuantityMatch(reservation, request.getQuantity());
                return toResponse(findInventory(request.getProductId()));
            }
            if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
                throw new BadRequestException("Reservation already confirmed for this order and product");
            }
            throw new BadRequestException("Reservation was already released for this order and product");
        }

        Inventory inventory = findInventoryWithLock(request.getProductId());
        if (inventory.getAvailableQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock for product " + request.getProductId());
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - request.getQuantity());
        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.getQuantity());
        inventoryRepository.save(inventory);

        reservationRepository.save(Reservation.builder()
                .orderId(request.getOrderId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .status(ReservationStatus.RESERVED)
                .build());

        return toResponse(inventory);
    }

    @Override
    @Transactional
    public InventoryResponse confirm(InventoryOperationRequest request) {
        Reservation reservation = findReservation(request.getOrderId(), request.getProductId());

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            validateQuantityMatch(reservation, request.getQuantity());
            return toResponse(findInventory(request.getProductId()));
        }
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new BadRequestException("Cannot confirm reservation with status " + reservation.getStatus());
        }
        validateQuantityMatch(reservation, request.getQuantity());

        Inventory inventory = findInventoryWithLock(request.getProductId());
        if (inventory.getReservedQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient reserved stock for product " + request.getProductId());
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() - request.getQuantity());
        inventoryRepository.save(inventory);

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        return toResponse(inventory);
    }

    @Override
    @Transactional
    public InventoryResponse release(InventoryOperationRequest request) {
        Reservation reservation = findReservation(request.getOrderId(), request.getProductId());

        if (reservation.getStatus() == ReservationStatus.RELEASED) {
            validateQuantityMatch(reservation, request.getQuantity());
            return toResponse(findInventory(request.getProductId()));
        }
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new BadRequestException("Cannot release reservation with status " + reservation.getStatus());
        }
        validateQuantityMatch(reservation, request.getQuantity());

        Inventory inventory = findInventoryWithLock(request.getProductId());
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + request.getQuantity());
        inventory.setReservedQuantity(inventory.getReservedQuantity() - request.getQuantity());
        inventoryRepository.save(inventory);

        reservation.setStatus(ReservationStatus.RELEASED);
        reservationRepository.save(reservation);

        return toResponse(inventory);
    }

    @Override
    @Transactional
    public InventoryResponse restore(InventoryOperationRequest request) {
        Reservation reservation = findReservation(request.getOrderId(), request.getProductId());

        if (reservation.getStatus() == ReservationStatus.RELEASED) {
            validateQuantityMatch(reservation, request.getQuantity());
            return toResponse(findInventory(request.getProductId()));
        }
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BadRequestException("Cannot restore reservation with status " + reservation.getStatus());
        }
        validateQuantityMatch(reservation, request.getQuantity());

        Inventory inventory = findInventoryWithLock(request.getProductId());
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + request.getQuantity());
        inventoryRepository.save(inventory);

        reservation.setStatus(ReservationStatus.RELEASED);
        reservationRepository.save(reservation);

        return toResponse(inventory);
    }

    @Override
    @Transactional
    public InventoryResponse updateStock(Long productId, UpdateInventoryRequest request) {
        Inventory inventory = findInventoryWithLock(productId);
        inventory.setAvailableQuantity(request.getAvailableQuantity());
        return toResponse(inventoryRepository.save(inventory));
    }

    private Inventory findInventory(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product " + productId));
    }

    private Inventory findInventoryWithLock(Long productId) {
        return inventoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product " + productId));
    }

    private Reservation findReservation(Long orderId, Long productId) {
        return reservationRepository.findByOrderIdAndProductId(orderId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found for order " + orderId + " and product " + productId));
    }

    private void validateQuantityMatch(Reservation reservation, Integer quantity) {
        if (!reservation.getQuantity().equals(quantity)) {
            throw new BadRequestException("Quantity does not match existing reservation");
        }
    }

    private InventoryResponse toResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .availableQuantity(inventory.getAvailableQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .version(inventory.getVersion())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
