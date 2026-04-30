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
SPRING_DATASOURCE_PORT=${SPRING_DATASOURCE_PORT:-5432}

if ! [[ "$MAIN_EXPRESS_PORT" =~ ^[0-9]+$ ]] || ! [[ "$DATA_EXPRESS_PORT" =~ ^[0-9]+$ ]] || ! [[ "$DATA_SPRING_PORT" =~ ^[0-9]+$ ]]; then
  echo "❌ Invalid port(s). MAIN_EXPRESS_PORT=$MAIN_EXPRESS_PORT, DATA_EXPRESS_PORT=$DATA_EXPRESS_PORT, DATA_SPRING_PORT=$DATA_SPRING_PORT, SPRING_DATASOURCE_PORT=$SPRING_DATASOURCE_PORT"
  exit 1
fi

echo "MAIN_EXPRESS_PORT=$MAIN_EXPRESS_PORT, DATA_EXPRESS_PORT=$DATA_EXPRESS_PORT, DATA_SPRING_PORT=$DATA_SPRING_PORT, SPRING_DATASOURCE_PORT=$SPRING_DATASOURCE_PORT"

required_csvs=(
    profiles.csv
    details.csv
    characters.csv
    character_nicknames.csv
    character_anime_works.csv
    person_details.csv
    person_alternate_names.csv
    person_anime_works.csv
    person_voice_works.csv
    recommendations.csv
    ratings.csv
    favs.csv
    stats.csv
)

missing_csvs=()
for csv in "${required_csvs[@]}"; do
    if [ ! -s "./data/$csv" ]; then
        missing_csvs+=("$csv")
    fi
done

if [ ${#missing_csvs[@]} -ne 0 ]; then
    echo "❌ Missing dataset file(s) in ./data: ${missing_csvs[*]}"
    echo "   PostgreSQL/MongoDB cannot be populated without these files."
    exit 1
fi

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Start it before continuing."
    exit 1
fi

# Start containers
echo "📦 Starting Docker containers..."
docker compose up -d postgres mongo pgadmin mongoexpress dozzle

# Wait for databases to be ready
echo "⏳ Waiting for databases to be ready..."
sleep 10

# Check container status
echo "🔍 Checking container status..."
docker compose ps

echo ""
echo "🐘 Verifying PostgreSQL population..."
postgres_counts=$(docker compose exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc \
    "SELECT (SELECT count(*) FROM profiles) || ',' || (SELECT count(*) FROM details);" 2>/dev/null || true)

profiles_count=${postgres_counts%%,*}
details_count=${postgres_counts##*,}

if ! [[ "$profiles_count" =~ ^[0-9]+$ ]] || ! [[ "$details_count" =~ ^[0-9]+$ ]] || [ "$profiles_count" -eq 0 ] || [ "$details_count" -eq 0 ]; then
    echo "❌ PostgreSQL is reachable but the static dataset is not populated."
    echo "   profiles=$profiles_count details=$details_count"
    echo "   If the pgdata volume was created before the CSV files were present, run:"
    echo "   docker compose down -v"
    echo "   ./start-dev.sh"
    exit 1
fi

echo "✅ PostgreSQL is ready! profiles=$profiles_count details=$details_count"

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
(cd ./services/data-server-springboot && ./spring_start.sh "localhost" "$SPRING_DATASOURCE_PORT" "$POSTGRES_DB" "$POSTGRES_USER" "$POSTGRES_PASSWORD" "$DATA_SPRING_PORT") &

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
PORT=$MAIN_EXPRESS_PORT npm run dev
