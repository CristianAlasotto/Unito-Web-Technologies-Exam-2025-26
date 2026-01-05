#!/bin/bash

echo "🚀 Starting development environment..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Start it before continuing."
    exit 1
fi

# Start containers
echo "📦 Starting Docker containers..."
docker compose up -d postgres
docker compose up -d

# Wait for databases to be ready
echo "⏳ Waiting for databases to be ready..."
sleep 10

# Check container status
echo "🔍 Checking container status..."
docker compose ps

# Verify and populate MongoDB
echo ""
echo "🗄️ Verifying and populating MongoDB..."
if [ -f "./db/mongo/verify.sh" ]; then
    chmod +x ./db/mongo/verify.sh
    ./db/mongo/verify.sh
    if [ $? -ne 0 ]; then
        echo "⚠️ Warning: MongoDB verification not completed. Continuing anyway..."
    fi
else
    echo "❌ verify.sh not found!"
fi

echo ""

# Navigate to server folder
cd services/main-server-express

# Install dependencies if needed
if [ ! -d "node_modules" ]; then
    echo "📥 Installing dependencies..."
    npm install
fi

# Start server in dev mode
echo "🌐 Starting Express server..."
npm run dev