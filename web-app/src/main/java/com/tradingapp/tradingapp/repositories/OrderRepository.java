package com.tradingapp.tradingapp.repositories;

import com.tradingapp.tradingapp.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findAllByOrderByCreatedOnDesc();

    List<Order> findByCreatedBy_UsernameOrderByCreatedOnDesc(String username);

    boolean existsByCustomer_Id(UUID id);
}

