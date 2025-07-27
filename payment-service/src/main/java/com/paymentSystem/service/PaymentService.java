package com.paymentSystem.service;

import com.paymentSystem.dto.CreatePaymentIntentRequest;
import com.paymentSystem.dto.PaymentIntentResponse;
import com.paymentSystem.entity.PaymentIntent;
import com.paymentSystem.entity.PaymentStatus;
import com.paymentSystem.repository.PaymentIntentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent as StripePaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {
    
    private final PaymentIntentRepository paymentIntentRepository;
    
    @Value("${stripe.secret-key}")
    private String stripeSecretKey;
    
    public PaymentService(PaymentIntentRepository paymentIntentRepository) {
        this.paymentIntentRepository = paymentIntentRepository;
    }
    
    public PaymentIntentResponse createPaymentIntent(String merchantId, CreatePaymentIntentRequest request) {
        // Initialize Stripe
        Stripe.apiKey = stripeSecretKey;
        
        try {
            // Generate unique payment intent ID
            String paymentIntentId = generatePaymentIntentId();
            
            // Create Stripe Payment Intent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(request.getAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue()) // Convert to cents
                .setCurrency(request.getCurrency().toLowerCase())
                .setDescription(request.getDescription())
                .putMetadata("merchant_id", merchantId)
                .putMetadata("payment_intent_id", paymentIntentId)
                .build();
            
            StripePaymentIntent stripePaymentIntent = StripePaymentIntent.create(params);
            
            // Create local payment intent
            PaymentIntent paymentIntent = new PaymentIntent(
                paymentIntentId,
                merchantId,
                request.getAmount(),
                request.getCurrency(),
                request.getDescription()
            );
            
            paymentIntent.setStripePaymentIntentId(stripePaymentIntent.getId());
            paymentIntent.setClientSecret(stripePaymentIntent.getClientSecret());
            paymentIntent.setStatus(PaymentStatus.CREATED);
            
            paymentIntent = paymentIntentRepository.save(paymentIntent);
            
            return mapToResponse(paymentIntent);
            
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create payment intent: " + e.getMessage(), e);
        }
    }
    
    public PaymentIntentResponse getPaymentIntent(String paymentIntentId) {
        PaymentIntent paymentIntent = paymentIntentRepository.findByPaymentIntentId(paymentIntentId)
            .orElseThrow(() -> new RuntimeException("Payment intent not found: " + paymentIntentId));
        
        return mapToResponse(paymentIntent);
    }
    
    public List<PaymentIntentResponse> getPaymentIntentsByMerchant(String merchantId) {
        List<PaymentIntent> paymentIntents = paymentIntentRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId);
        
        return paymentIntents.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public PaymentIntentResponse updatePaymentStatus(String paymentIntentId, PaymentStatus newStatus) {
        PaymentIntent paymentIntent = paymentIntentRepository.findByPaymentIntentId(paymentIntentId)
            .orElseThrow(() -> new RuntimeException("Payment intent not found: " + paymentIntentId));
        
        PaymentStatus oldStatus = paymentIntent.getStatus();
        paymentIntent.setStatus(newStatus);
        
        paymentIntent = paymentIntentRepository.save(paymentIntent);
        
        // TODO: Send notification to merchant about status change
        
        return mapToResponse(paymentIntent);
    }
    
    public PaymentIntentResponse handleStripeWebhook(String stripePaymentIntentId, String stripeStatus) {
        Optional<PaymentIntent> optionalPaymentIntent = 
            paymentIntentRepository.findByStripePaymentIntentId(stripePaymentIntentId);
        
        if (optionalPaymentIntent.isEmpty()) {
            throw new RuntimeException("Payment intent not found for Stripe ID: " + stripePaymentIntentId);
        }
        
        PaymentIntent paymentIntent = optionalPaymentIntent.get();
        PaymentStatus newStatus = mapStripeStatusToPaymentStatus(stripeStatus);
        
        if (newStatus != paymentIntent.getStatus()) {
            paymentIntent.setStatus(newStatus);
            paymentIntent = paymentIntentRepository.save(paymentIntent);
            
            // TODO: Send notification to merchant about status change
        }
        
        return mapToResponse(paymentIntent);
    }
    
    private String generatePaymentIntentId() {
        String id;
        do {
            id = "pi_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        } while (paymentIntentRepository.existsByPaymentIntentId(id));
        
        return id;
    }
    
    private PaymentStatus mapStripeStatusToPaymentStatus(String stripeStatus) {
        return switch (stripeStatus.toLowerCase()) {
            case "requires_payment_method", "requires_confirmation", "requires_action" -> PaymentStatus.CREATED;
            case "processing" -> PaymentStatus.PROCESSING;
            case "succeeded" -> PaymentStatus.SUCCEEDED;
            case "canceled" -> PaymentStatus.CANCELED;
            default -> PaymentStatus.FAILED;
        };
    }
    
    private PaymentIntentResponse mapToResponse(PaymentIntent paymentIntent) {
        return new PaymentIntentResponse(
            paymentIntent.getPaymentIntentId(),
            paymentIntent.getMerchantId(),
            paymentIntent.getAmount(),
            paymentIntent.getCurrency(),
            paymentIntent.getDescription(),
            paymentIntent.getStatus(),
            paymentIntent.getClientSecret(),
            paymentIntent.getCreatedAt(),
            paymentIntent.getUpdatedAt()
        );
    }
}
