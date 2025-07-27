package com.paymentSystem.repository;

import com.paymentSystem.entity.PaymentIntent;
import com.paymentSystem.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Long> {
    
    Optional<PaymentIntent> findByPaymentIntentId(String paymentIntentId);
    
    Optional<PaymentIntent> findByStripePaymentIntentId(String stripePaymentIntentId);
    
    List<PaymentIntent> findByMerchantId(String merchantId);
    
    List<PaymentIntent> findByMerchantIdAndStatus(String merchantId, PaymentStatus status);
    
    @Query("SELECT p FROM PaymentIntent p WHERE p.merchantId = :merchantId ORDER BY p.createdAt DESC")
    List<PaymentIntent> findByMerchantIdOrderByCreatedAtDesc(@Param("merchantId") String merchantId);
    
    boolean existsByPaymentIntentId(String paymentIntentId);
}
