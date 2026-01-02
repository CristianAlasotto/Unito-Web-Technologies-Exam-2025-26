#!/bin/bash

# Configuration
CONTAINER_NAME="solution-mongo-1"
DB_NAME="anime_dynamic"

# Variabili di default
COLLECTIONS_TO_IMPORT=("stats" "favs" "ratings")

# Se passati argomenti, usali
if [ $# -gt 0 ]; then
    COLLECTIONS_TO_IMPORT=("$@")
fi

echo "Starting optimized MongoDB import for large dataset..."
echo "Collections to import: ${COLLECTIONS_TO_IMPORT[@]}"
start_time=$(date +%s)

# Ottimizzazioni pre-import per file giganti
echo "Configuring MongoDB for bulk import..."
docker exec -i $CONTAINER_NAME mongosh admin --eval "
    db.adminCommand({ setParameter: 1, syncdelay: 300 });
    db.adminCommand({ setParameter: 1, journalCommitInterval: 300 });
" 2>/dev/null

# Import function ottimizzata per file grandi
import_csv() {
    local collection=$1
    local file=$2

    echo "Importing $collection from $file..."
    docker exec -i $CONTAINER_NAME mongoimport \
        --db=$DB_NAME \
        --collection=$collection \
        --type=csv \
        --headerline \
        --file=/csvdata/$file \
        --numInsertionWorkers=12 \
        --batchSize=100000 \
        --bypassDocumentValidation \
        --writeConcern="{w:0}" &
}

# Importa solo le collezioni richieste
for collection in "${COLLECTIONS_TO_IMPORT[@]}"; do
    case $collection in
        "stats")
            import_csv "stats" "stats.csv"
            ;;
        "favs")
            import_csv "favs" "favs.csv"
            ;;
        "ratings")
            import_csv "ratings" "ratings.csv"
            ;;
    esac
done

# Wait for all imports
wait

end_time=$(date +%s)
duration=$((end_time - start_time))

echo ""
echo "========================================"
echo "Import complete!"
echo "Total time: ${duration}s ($(($duration / 60)) minutes)"
echo "========================================"

# Ripristina impostazioni normali
docker exec -i $CONTAINER_NAME mongosh admin --eval "
    db.adminCommand({ setParameter: 1, syncdelay: 60 });
    db.adminCommand({ setParameter: 1, journalCommitInterval: 100 });
" 2>/dev/null

echo "MongoDB settings restored to normal"