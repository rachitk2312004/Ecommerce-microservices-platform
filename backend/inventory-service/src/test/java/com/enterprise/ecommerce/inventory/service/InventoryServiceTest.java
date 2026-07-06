package com.enterprise.ecommerce.inventory.service;

import com.enterprise.ecommerce.inventory.dto.CheckAvailabilityRequest;
import com.enterprise.ecommerce.inventory.dto.InventoryOperationRequest;
import com.enterprise.ecommerce.inventory.entity.Inventory;
import com.enterprise.ecommerce.inventory.entity.Reservation;
import com.enterprise.ecommerce.inventory.enums.ReservationStatus;
import com.enterprise.ecommerce.inventory.exception.InsufficientStockException;
import com.enterprise.ecommerce.inventory.repository.InventoryRepository;
import com.enterprise.ecommerce.inventory.repository.ReservationRepository;
import com.enterprise.ecommerce.inventory.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = Inventory.builder()
                .id(1L)
                .productId(1L)
                .availableQuantity(100)
                .reservedQuantity(0)
                .version(1L)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void checkAvailability_whenStockSufficient_returnsAvailable() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        var request = CheckAvailabilityRequest.builder().productId(1L).quantity(10).build();
        var response = inventoryService.checkAvailability(request);

        assertTrue(response.isAvailable());
    }

    @Test
    void checkAvailability_whenStockInsufficient_returnsNotAvailable() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        var request = CheckAvailabilityRequest.builder().productId(1L).quantity(150).build();
        var response = inventoryService.checkAvailability(request);

        assertFalse(response.isAvailable());
    }

    @Test
    void reserve_success() {
        when(reservationRepository.findByOrderIdAndProductId(100L, 1L)).thenReturn(Optional.empty());
        when(inventoryRepository.findByProductIdWithLock(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation reservation = inv.getArgument(0);
            reservation.setId(1L);
            return reservation;
        });

        var request = InventoryOperationRequest.builder()
                .productId(1L)
                .quantity(10)
                .orderId(100L)
                .build();

        var response = inventoryService.reserve(request);

        assertEquals(90, response.getAvailableQuantity());
        assertEquals(10, response.getReservedQuantity());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void reserve_idempotentWhenAlreadyReserved() {
        Reservation existing = Reservation.builder()
                .id(1L)
                .orderId(100L)
                .productId(1L)
                .quantity(10)
                .status(ReservationStatus.RESERVED)
                .build();

        inventory.setAvailableQuantity(90);
        inventory.setReservedQuantity(10);

        when(reservationRepository.findByOrderIdAndProductId(100L, 1L)).thenReturn(Optional.of(existing));
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        var request = InventoryOperationRequest.builder()
                .productId(1L)
                .quantity(10)
                .orderId(100L)
                .build();

        var response = inventoryService.reserve(request);

        assertEquals(90, response.getAvailableQuantity());
        assertEquals(10, response.getReservedQuantity());
        verify(inventoryRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void reserve_insufficientStock() {
        when(reservationRepository.findByOrderIdAndProductId(100L, 1L)).thenReturn(Optional.empty());
        when(inventoryRepository.findByProductIdWithLock(1L)).thenReturn(Optional.of(inventory));

        var request = InventoryOperationRequest.builder()
                .productId(1L)
                .quantity(150)
                .orderId(100L)
                .build();

        assertThrows(InsufficientStockException.class, () -> inventoryService.reserve(request));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void release_success() {
        Reservation reservation = Reservation.builder()
                .id(1L)
                .orderId(100L)
                .productId(1L)
                .quantity(10)
                .status(ReservationStatus.RESERVED)
                .build();

        inventory.setAvailableQuantity(90);
        inventory.setReservedQuantity(10);

        when(reservationRepository.findByOrderIdAndProductId(100L, 1L)).thenReturn(Optional.of(reservation));
        when(inventoryRepository.findByProductIdWithLock(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = InventoryOperationRequest.builder()
                .productId(1L)
                .quantity(10)
                .orderId(100L)
                .build();

        var response = inventoryService.release(request);

        assertEquals(100, response.getAvailableQuantity());
        assertEquals(0, response.getReservedQuantity());
        assertEquals(ReservationStatus.RELEASED, reservation.getStatus());
    }
}
