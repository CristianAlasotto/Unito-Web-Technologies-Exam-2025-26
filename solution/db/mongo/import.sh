#!/bin/bash

# Configuration
CONTAINER_NAME="solution-mongo-1"
DB_NAME="anime_dynamic"

echo "Starting parallel MongoDB import..."
start_time=$(date +%s)

# Import function in parallel
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
echo "Starting indexing..."

#if it crashes, reduce maxIndexBuildMemoryUsageMegabytes
docker exec -i $CONTAINER_NAME mongosh admin --eval "
    db.adminCommand({ setParameter: 1, maxIndexBuildMemoryUsageMegabytes: 4096 });
" 2>/dev/null || echo "Note: Could not increase index memory (non-critical)"

# Create indexes sequentially per collection, but collections in parallel
docker exec -i $CONTAINER_NAME mongosh $DB_NAME --eval "
    db.ratings.createIndexes([
        { key: { anime_id: 1 }, name: 'idx_anime_id' },
        { key: { username: 1 }, name: 'idx_username' }
    ]);
" &

docker exec -i $CONTAINER_NAME mongosh $DB_NAME --eval "
    db.favs.createIndexes([
        { key: { id: 1 }, name: 'idx_id' },
        { key: { username: 1 }, name: 'idx_username' }
    ]);
" &

docker exec -i $CONTAINER_NAME mongosh $DB_NAME --eval "
    db.getCollection('stats').createIndex({ mal_id: 1 }, { name: 'idx_mal_id' });
" &

wait
echo "✓ All indexes created!"