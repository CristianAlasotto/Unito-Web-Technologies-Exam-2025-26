#!/bin/bash

# Define paths relative to this script
BASE_DIR="./db/mongo"

echo "========================================"
echo "Starting full database setup..."
echo "========================================"

# 1. Run Import
echo "Step 1/2: Running import..."
bash "$BASE_DIR/import.sh"

# 2. Check Import Success and Run Indexing
if [ $? -eq 0 ]; then
    echo "Import completed successfully!"
    echo "Step 2/2: Running indexing..."

    # Better to call your existing index script than duplicate code
    bash "$BASE_DIR/index.sh"

    if [ $? -eq 0 ]; then
        echo "✓ Full setup completed successfully!"
    else
        echo "✗ Indexing failed!"
        exit 1
    fi
else
    echo "✗ Import failed!"
    exit 1
fi