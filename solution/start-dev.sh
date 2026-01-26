#!/bin/bash

echo "🚀 Starting development environment..."

# Load environment variables from .env file
if [ -f ".env" ]; then
    export $(cat .env | grep -v '#' | xargs)
    echo "✅ Environment variables loaded from .env"
else
    echo "⚠️ .env file not found"
    exit
fi

MAIN_EXPRESS_PORT=${MAIN_EXPRESS_PORT:-3000}
DATA_EXPRESS_PORT=${DATA_EXPRESS_PORT:-3001}
DATA_SPRING_PORT=${DATA_SPRING_PORT:-8080}

if ! [[ "$MAIN_EXPRESS_PORT" =~ ^[0-9]+$ ]] || ! [[ "$DATA_EXPRESS_PORT" =~ ^[0-9]+$ ]] || ! [[ "$DATA_SPRING_PORT" =~ ^[0-9]+$ ]]; then
  echo "❌ Invalid port(s). MAIN_EXPRESS_PORT=$MAIN_EXPRESS_PORT, DATA_EXPRESS_PORT=$DATA_EXPRESS_PORT, DATA_SPRING_PORT=$DATA_SPRING_PORT"
  exit 1
fi

echo " MAIN_EXPRESS_PORT=$MAIN_EXPRESS_PORT, DATA_EXPRESS_PORT=$DATA_EXPRESS_PORT, DATA_SPRING_PORT=$DATA_SPRING_PORT"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Start it before continuing."
    exit 1
fi

# Start containers
echo "📦 Starting Docker containers..."
docker compose --profile db up -d

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
        echo "❌ MongoDB setup failed. Please check the logs above."
        exit 1
    fi
    echo "✅ MongoDB is ready!"
else
    echo "❌ verify.sh not found!"
    exit 1
fi

echo ""

# Start MongoDB server in background
echo "🗄️ Starting MongoDB server..."
./services/data-server-mongo/server_start.sh "$DATA_EXPRESS_PORT" &

# Start Spring Boot server in background
echo "🐘 Starting Spring Boot server..."
echo "      NOT ENABLED # !"
#(cd ./services/data-server-springboot && ./spring_start.sh "$POSTGRES_HOST" "$POSTGRES_PORT" "$POSTGRES_DB" "$POSTGRES_USER" "$POSTGRES_PASSWORD" "$DATA_SPRING_PORT") &

# --- Main Express Server folder ---
cd services/main-server-express

echo "📥 Installing dependencies..."
npm_install_output=$(npm install 2>&1)
npm_install_status=$?
echo "$npm_install_output"

if [ $npm_install_status -ne 0 ] || echo "$npm_install_output" | grep -qiE "npm ERR!|vulnerab"; then
    echo "🛠️ Issues detected during npm install, running npm audit fix..."
    npm audit fix --silent || true
fi

# Start server in dev mode
echo "🌐 Starting MAIN Express server... :$MAIN_EXPRESS_PORT"
npm run dev