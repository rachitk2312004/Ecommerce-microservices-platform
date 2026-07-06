package com.enterprise.ecommerce.inventory.repository;

import com.enterprise.ecommerce.inventory.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByOrderIdAndProductId(Long orderId, Long productId);
}
