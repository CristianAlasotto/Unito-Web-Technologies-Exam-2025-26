#!/bin/bash

echo "========================================"
echo "Starting full database setup..."
echo "========================================"
echo ""

# Esegui import
echo "Step 1/2: Running import..."
./db/mongo/import.sh

# Controlla se l'import è andato a buon fine
if [ $? -eq 0 ]; then
    echo ""
    echo "Import completed successfully!"
    echo ""

    # Esegui indexing
    echo "Step 2/2: Running indexing..."

    CONTAINER_NAME="solution-mongo-1"
    DB_NAME="anime_dynamic"

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

    echo ""
    echo "========================================"
    echo "✓ Database setup completed successfully!"
    echo "========================================"
else
    echo ""
    echo "========================================"
    echo "✗ Import failed! Skipping indexing."
    echo "========================================"
    exit 1
fi