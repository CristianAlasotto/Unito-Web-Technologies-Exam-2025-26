#!/bin/bash

# Configuration
CONTAINER_NAME="solution-mongo-1"
DB_NAME="anime_dynamic"

# ... (previous parts of script) ...

# Create indexes sequentially per collection, but collections in parallel
docker exec -i $CONTAINER_NAME mongosh $DB_NAME --eval "
    db.ratings.createIndex({ anime_id: 1 }, { name: 'idx_anime_id' });
    db.ratings.createIndex({ username: 1 }, { name: 'idx_username' });
" &

docker exec -i $CONTAINER_NAME mongosh $DB_NAME --eval "
    db.favs.createIndex({ id: 1 }, { name: 'idx_id' });
    db.favs.createIndex({ username: 1 }, { name: 'idx_username' });
" &

docker exec -i $CONTAINER_NAME mongosh $DB_NAME --eval "
    db.getCollection('stats').createIndex({ mal_id: 1 }, { name: 'idx_mal_id' });
" &

wait
echo "âœ“ All indexes created!"