#!/bin/bash

# Payment System Deployment Script

echo "🚀 Starting Payment System Deployment..."

# Check if .env file exists
if [ ! -f .env ]; then
    echo "❌ .env file not found. Please copy .env.template to .env and configure your settings."
    echo "   cp .env.template .env"
    echo "   # Then edit .env with your Stripe keys and other configurations"
    exit 1
fi

# Load environment variables
source .env

# Validate required environment variables
if [ -z "$STRIPE_SECRET_KEY" ] || [ "$STRIPE_SECRET_KEY" = "sk_test_your_stripe_secret_key_here" ]; then
    echo "❌ Please set your STRIPE_SECRET_KEY in the .env file"
    exit 1
fi

echo "📦 Building Docker images..."

# Build services
echo "Building Payment Service..."
docker build -t payment-service ./payment-service

echo "Building Merchant Service..."
docker build -t merchant-service ./merchant-service

# For now, we'll create placeholder services for gateway and notification
echo "Building Gateway Service..."
mkdir -p gateway-service
echo "FROM nginx:alpine" > gateway-service/Dockerfile
echo "COPY default.conf /etc/nginx/conf.d/" >> gateway-service/Dockerfile
echo "EXPOSE 8080" >> gateway-service/Dockerfile

cat > gateway-service/default.conf << 'EOF'
server {
    listen 8080;
    
    location /api/v1/payments {
        proxy_pass http://payment-service:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location /api/v1/merchants {
        proxy_pass http://merchant-service:8082;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
EOF

docker build -t gateway-service ./gateway-service

echo "Building Notification Service..."
mkdir -p notification-service
echo "FROM nginx:alpine" > notification-service/Dockerfile
echo "EXPOSE 8083" >> notification-service/Dockerfile
docker build -t notification-service ./notification-service

echo "🗄️ Starting database..."
docker-compose up -d mysql

echo "⏳ Waiting for database to be ready..."
sleep 30

echo "🚀 Starting all services..."
docker-compose up -d

echo "✅ Deployment complete!"
echo ""
echo "🌐 Services are available at:"
echo "   API Gateway: http://localhost:8080"
echo "   Payment Service: http://localhost:8081"
echo "   Merchant Service: http://localhost:8082"
echo "   Notification Service: http://localhost:8083"
echo "   MySQL Database: localhost:3306"
echo ""
echo "📋 Test the API:"
echo "   # Create a payment intent"
echo '   curl -X POST http://localhost:8080/api/v1/payments/intents \'
echo '        -H "Content-Type: application/json" \'
echo '        -H "X-API-Key: api_key_12345678901234567890" \'
echo '        -H "X-Merchant-ID: merchant_001" \'
echo '        -d "{\"amount\": 10.00, \"currency\": \"USD\", \"description\": \"Test payment\"}"'
echo ""
echo "   # Get payment intents for merchant"
echo '   curl -X GET http://localhost:8080/api/v1/payments/intents \'
echo '        -H "X-API-Key: api_key_12345678901234567890" \'
echo '        -H "X-Merchant-ID: merchant_001"'
