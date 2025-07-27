package com.paymentSystem.controller;

import com.paymentSystem.dto.CreatePaymentIntentRequest;
import com.paymentSystem.dto.PaymentIntentResponse;
import com.paymentSystem.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @PostMapping("/intents")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestHeader("X-Merchant-ID") String merchantId,
            @Valid @RequestBody CreatePaymentIntentRequest request) {
        
        try {
            // TODO: Validate API key and merchant ID
            PaymentIntentResponse response = paymentService.createPaymentIntent(merchantId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create payment intent", e);
        }
    }
    
    @GetMapping("/intents/{paymentIntentId}")
    public ResponseEntity<PaymentIntentResponse> getPaymentIntent(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestHeader("X-Merchant-ID") String merchantId,
            @PathVariable String paymentIntentId) {
        
        try {
            // TODO: Validate API key and merchant ID
            PaymentIntentResponse response = paymentService.getPaymentIntent(paymentIntentId);
            
            // Ensure merchant can only access their own payment intents
            if (!response.getMerchantId().equals(merchantId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }
    
    @GetMapping("/intents")
    public ResponseEntity<List<PaymentIntentResponse>> getPaymentIntents(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestHeader("X-Merchant-ID") String merchantId) {
        
        try {
            // TODO: Validate API key and merchant ID
            List<PaymentIntentResponse> responses = paymentService.getPaymentIntentsByMerchant(merchantId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve payment intents", e);
        }
    }
    
    @PostMapping("/webhooks/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader("Stripe-Signature") String signature) {
        
        try {
            // TODO: Verify webhook signature
            
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            Map<String, Object> object = (Map<String, Object>) data.get("object");
            
            String stripePaymentIntentId = (String) object.get("id");
            String status = (String) object.get("status");
            
            paymentService.handleStripeWebhook(stripePaymentIntentId, status);
            
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Webhook processing failed: " + e.getMessage());
        }
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", e.getMessage()));
    }
}
