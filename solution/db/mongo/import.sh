#!/bin/bash

# Configuration
CONTAINER_NAME="solution-mongo-1"
DB_NAME="anime_dynamic"

echo "Starting parallel MongoDB import..."
start_time=$(date +%s)

# Import function
import_csv() {
    local collection=$1
    local file=$2

    echo "Importing $collection..."
    docker exec -i $CONTAINER_NAME mongoimport \
        --db=$DB_NAME \
        --collection=$collection \
        --type=csv \
        --headerline \
        --file=/csvdata/$file \
        --numInsertionWorkers=4 \
        --batchSize=25000 &
}

import_csv "stats" "stats.csv"
import_csv "favs" "favs.csv"
import_csv "ratings" "ratings.csv"

# Wait for all background jobs to complete
wait

end_time=$(date +%s)
duration=$((end_time - start_time))

echo ""
echo "========================================"
echo "Import complete!"
echo "Total time: ${duration}s"
echo "========================================"

# Create indexes for better query performance
echo "Creating indexes..."
docker exec -i $CONTAINER_NAME mongosh $DB_NAME --eval '
db.ratings.createIndex({"anime_id": 1});
db.ratings.createIndex({"username": 1});
db.favs.createIndex({"id": 1});
db.favs.createIndex({"username": 1});
'

echo "✓ Indexes created!"