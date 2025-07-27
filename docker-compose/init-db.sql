-- Create database if not exists
CREATE DATABASE IF NOT EXISTS payment_system;
USE payment_system;

-- Merchants table
CREATE TABLE merchants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) UNIQUE NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Payment intents table
CREATE TABLE payment_intents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_intent_id VARCHAR(100) UNIQUE NOT NULL,
    merchant_id VARCHAR(100) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    description TEXT,
    stripe_payment_intent_id VARCHAR(255),
    status ENUM('CREATED', 'PROCESSING', 'SUCCEEDED', 'FAILED', 'CANCELED') DEFAULT 'CREATED',
    client_secret VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (merchant_id) REFERENCES merchants(merchant_id)
);

-- Payment history table
CREATE TABLE payment_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_intent_id VARCHAR(100) NOT NULL,
    previous_status ENUM('CREATED', 'PROCESSING', 'SUCCEEDED', 'FAILED', 'CANCELED'),
    new_status ENUM('CREATED', 'PROCESSING', 'SUCCEEDED', 'FAILED', 'CANCELED'),
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_intent_id) REFERENCES payment_intents(payment_intent_id)
);

-- Insert sample merchant for testing
INSERT INTO merchants (merchant_id, name, email, api_key) VALUES 
('merchant_001', 'Test Merchant', 'merchant@example.com', 'api_key_12345678901234567890');

-- Create indexes for better performance
CREATE INDEX idx_payment_intents_merchant_id ON payment_intents(merchant_id);
CREATE INDEX idx_payment_intents_status ON payment_intents(status);
CREATE INDEX idx_payment_history_payment_intent_id ON payment_history(payment_intent_id);
CREATE INDEX idx_merchants_api_key ON merchants(api_key);
