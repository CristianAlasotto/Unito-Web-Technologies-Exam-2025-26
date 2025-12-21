#!/bin/bash

echo "🚀 Importing CSV files to MongoDB..."

# Trova il nome del container mongo
MONGO_CONTAINER=$(docker ps --filter "ancestor=mongo:7" --format "{{.Names}}" | head -1)

if [ -z "$MONGO_CONTAINER" ]; then
    echo "❌ MongoDB container not running. Starting it..."
    cd /home/davide/anno_III/TWEB/project/ColluraCorrendoAlasotto
    sudo docker-compose up -d mongo
    sleep 5
    MONGO_CONTAINER=$(docker ps --filter "ancestor=mongo:7" --format "{{.Names}}" | head -1)
fi

echo "✅ Using MongoDB container: $MONGO_CONTAINER"

# Path corretto ai CSV
DATA_PATH="/home/davide/anno_III/TWEB/project/ColluraCorrendoAlasotto/solution/data"

# Copia i file nel container
echo "📦 Copying files..."
sudo docker cp "$DATA_PATH/ratings.csv" $MONGO_CONTAINER:/tmp/
sudo docker cp "$DATA_PATH/stats.csv" $MONGO_CONTAINER:/tmp/
sudo docker cp "$DATA_PATH/favs.csv" $MONGO_CONTAINER:/tmp/

# Importa ratings
echo "📊 Importing ratings (this may take several minutes)..."
sudo docker exec $MONGO_CONTAINER mongoimport \
  --db anime_dynamic \
  --collection ratings \
  --type csv \
  --headerline \
  --drop \
  --file /tmp/ratings.csv

# Importa stats
echo "📊 Importing stats..."
sudo docker exec $MONGO_CONTAINER mongoimport \
  --db anime_dynamic \
  --collection stats \
  --type csv \
  --headerline \
  --drop \
  --file /tmp/stats.csv

# Importa favs
echo "📊 Importing favs..."
sudo docker exec $MONGO_CONTAINER mongoimport \
  --db anime_dynamic \
  --collection favs \
  --type csv \
  --headerline \
  --drop \
  --file /tmp/favs.csv

echo "✅ Import complete!"

# Verifica
echo ""
echo "📊 Record counts:"
sudo docker exec $MONGO_CONTAINER mongosh anime_dynamic --quiet --eval "
  print('Ratings: ' + db.ratings.countDocuments());
  print('Stats: ' + db.stats.countDocuments());
  print('Favs: ' + db.favs.countDocuments());
"