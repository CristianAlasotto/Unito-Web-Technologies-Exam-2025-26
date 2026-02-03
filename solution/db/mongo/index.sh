#!/bin/bash

# Configuration
CONTAINER_NAME="solution-mongo-1"
DB_NAME="anime_dynamic"

echo "========================================"
echo "Creating indexes for optimal performance..."
echo "========================================"

# Create indexes sequentially per collection, but collections in parallel
echo "Creating indexes for ratings collection..."
docker exec -i $CONTAINER_NAME mongosh $DB_NAME --eval "
    print('Creating anime_id index...');
    db.ratings.createIndex({ anime_id: 1 }, { name: 'idx_anime_id', background: true });
    print('Creating username index...');
    db.ratings.createIndex({ username: 1 }, { name: 'idx_username', background: true });
    print('Creating status index...');
    db.ratings.createIndex({ status: 1 }, { name: 'idx_status', background: true });
    print('Creating score index...');
    db.ratings.createIndex({ score: 1 }, { name: 'idx_score', background: true });
    print('✓ Ratings indexes created!');
" &

echo "Creating indexes for favs collection..."
docker exec -i $CONTAINER_NAME mongosh $DB_NAME --eval "
    print('Creating id index...');
    db.favs.createIndex({ id: 1 }, { name: 'idx_id', background: true });
    print('Creating username index...');
    db.favs.createIndex({ username: 1 }, { name: 'idx_username', background: true });
    print('✓ Favs indexes created!');
" &

echo "Creating indexes for stats collection..."
docker exec -i $CONTAINER_NAME mongosh $DB_NAME --eval "
    print('Creating mal_id index...');
    db.getCollection('stats').createIndex({ mal_id: 1 }, { name: 'idx_mal_id', background: true });
    print('✓ Stats indexes created!');
" &

wait
echo ""
echo "========================================"
echo "✓ All indexes created successfully!"
echo "========================================"
echo ""
echo "Note: Large indexes (status, score) will build in background."
echo "You can check progress with:"
echo "  docker exec -i $CONTAINER_NAME mongosh $DB_NAME --eval 'db.currentOp()'"