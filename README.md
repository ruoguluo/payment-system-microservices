# Payment System

A microservices-based payment system built with Java Spring Boot, MySQL, Stripe integration, and Docker deployment.

## Features

- **Merchant Management**: API key-based authentication for merchants
- **Payment Intent Creation**: Create payment intents with Stripe integration
- **Card Payments**: Users can pay via credit/debit cards through Stripe
- **Payment Status Tracking**: Real-time payment status updates via webhooks
- **Microservices Architecture**: Scalable and maintainable service-oriented design

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │    │  Payment Service │    │ Merchant Service│
│    (Port 8080)  │────│    (Port 8081)   │────│    (Port 8082)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                        
                       ┌─────────────────┐    ┌─────────────────┐
                       │ Notification    │    │    MySQL        │
                       │ Service (8083)  │    │  Database       │
                       └─────────────────┘    └─────────────────┘
```

## Services

1. **API Gateway (Port 8080)**: Routes requests to appropriate services
2. **Payment Service (Port 8081)**: Handles payment intent creation and Stripe integration
3. **Merchant Service (Port 8082)**: Manages merchant authentication and authorization
4. **Notification Service (Port 8083)**: Handles payment status notifications
5. **MySQL Database**: Stores merchant and payment data

## Prerequisites

- Docker and Docker Compose
- Stripe account (for API keys)
- Java 17+ (for local development)
- Maven (for local development)

## Quick Start

1. **Clone and setup environment**:
   ```bash
   cd payment-system
   cp .env.template .env
   ```

2. **Configure Stripe keys in `.env`**:
   ```bash
   # Get your keys from https://dashboard.stripe.com/apikeys
   STRIPE_SECRET_KEY=sk_test_your_actual_stripe_secret_key
   STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret_here
   ```

3. **Deploy the system**:
   ```bash
   ./deploy.sh
   ```

4. **Test the API**:
   ```bash
   # Create a payment intent
   curl -X POST http://localhost:8080/api/v1/payments/intents \
        -H "Content-Type: application/json" \
        -H "X-API-Key: api_key_12345678901234567890" \
        -H "X-Merchant-ID: merchant_001" \
        -d '{"amount": 10.00, "currency": "USD", "description": "Test payment"}'
   ```

## API Endpoints

### Payment Intents

#### Create Payment Intent
```http
POST /api/v1/payments/intents
Headers:
  Content-Type: application/json
  X-API-Key: {merchant_api_key}
  X-Merchant-ID: {merchant_id}

Body:
{
  "amount": 10.00,
  "currency": "USD",
  "description": "Test payment"
}
```

#### Get Payment Intent
```http
GET /api/v1/payments/intents/{payment_intent_id}
Headers:
  X-API-Key: {merchant_api_key}
  X-Merchant-ID: {merchant_id}
```

#### List Merchant Payment Intents
```http
GET /api/v1/payments/intents
Headers:
  X-API-Key: {merchant_api_key}
  X-Merchant-ID: {merchant_id}
```

#### Stripe Webhook
```http
POST /api/v1/payments/webhooks/stripe
Headers:
  Stripe-Signature: {webhook_signature}
```

## Database Schema

### Merchants Table
```sql
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
```

### Payment Intents Table
```sql
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
```

## Testing with Frontend

To test card payments, you can create a simple HTML page:

```html
<!DOCTYPE html>
<html>
<head>
    <script src="https://js.stripe.com/v3/"></script>
</head>
<body>
    <form id="payment-form">
        <div id="card-element">
            <!-- Stripe Elements will create form elements here -->
        </div>
        <button id="submit">Pay</button>
    </form>

    <script>
        const stripe = Stripe('pk_test_your_publishable_key');
        const elements = stripe.elements();
        const cardElement = elements.create('card');
        cardElement.mount('#card-element');

        const form = document.getElementById('payment-form');
        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            
            // Get client_secret from your payment intent creation API
            const clientSecret = 'pi_xxx_secret_xxx'; // From API response
            
            const {error} = await stripe.confirmCardPayment(clientSecret, {
                payment_method: {
                    card: cardElement,
                }
            });

            if (error) {
                console.error('Payment failed:', error);
            } else {
                console.log('Payment succeeded!');
            }
        });
    </script>
</body>
</html>
```

## Development

### Running Services Locally

1. **Start MySQL**:
   ```bash
   docker run -d --name payment-mysql \
     -e MYSQL_ROOT_PASSWORD=rootpassword \
     -e MYSQL_DATABASE=payment_system \
     -e MYSQL_USER=payment_user \
     -e MYSQL_PASSWORD=payment_password \
     -p 3306:3306 mysql:8.0
   ```

2. **Run Payment Service**:
   ```bash
   cd payment-service
   mvn spring-boot:run
   ```

3. **Run Merchant Service**:
   ```bash
   cd merchant-service
   mvn spring-boot:run
   ```

### Building Individual Services

```bash
# Payment Service
cd payment-service
mvn clean package
docker build -t payment-service .

# Merchant Service
cd merchant-service
mvn clean package
docker build -t merchant-service .
```

## Monitoring and Logs

```bash
# View all service logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f payment-service
docker-compose logs -f mysql

# Check service status
docker-compose ps
```

## Security Considerations

1. **API Key Management**: Implement proper API key rotation and secure storage
2. **Webhook Verification**: Verify Stripe webhook signatures in production
3. **HTTPS**: Use HTTPS in production for all communications
4. **Database Security**: Use strong passwords and connection encryption
5. **Input Validation**: Validate all input data and sanitize outputs

## Production Deployment

For production deployment:

1. Use managed database services (AWS RDS, Google Cloud SQL)
2. Implement proper load balancing
3. Set up monitoring and alerting
4. Use container orchestration (Kubernetes)
5. Implement proper secrets management
6. Set up backup and disaster recovery

## Troubleshooting

### Common Issues

1. **Stripe Connection Errors**: Verify your API keys are correct
2. **Database Connection Failures**: Ensure MySQL is running and credentials are correct
3. **Port Conflicts**: Check if ports 8080-8083, 3306 are available

### Useful Commands

```bash
# Reset the entire system
docker-compose down -v
docker system prune -f
./deploy.sh

# Access MySQL directly
docker exec -it payment-mysql mysql -u payment_user -p payment_system

# Check container logs
docker logs payment-service
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.
