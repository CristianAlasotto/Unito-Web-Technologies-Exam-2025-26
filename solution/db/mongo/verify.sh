#!/bin/bash

CONTAINER_NAME="solution-mongo-1"
DB_NAME="anime_dynamic"

echo "Verifying MongoDB data import..."
echo "========================================"

# Verifica che il container sia in esecuzione
if ! docker ps | grep -q $CONTAINER_NAME; then
    echo "❌ MongoDB container is not running!"
    exit 1
fi

# Conta i documenti in ogni collection
docker exec -i $CONTAINER_NAME mongosh $DB_NAME --quiet --eval "
  var ratings = db.ratings.estimatedDocumentCount();
  var stats = db.getCollection('stats').estimatedDocumentCount();
  var favs = db.favs.estimatedDocumentCount();
  
  print('Ratings: ' + ratings + ' documents');
  print('Stats: ' + stats + ' documents');
  print('Favs: ' + favs + ' documents');
  print('');
  
  // Valori attesi (da README.md)
  var expectedRatings = 124298357;
  var expectedStats = 28955;
  var expectedFavs = 4178747;
  
  var allGood = true;
  
  // Controlla che le collezioni abbiano dati (almeno 10% dei valori attesi)
  if (ratings > expectedRatings * 0.1) {
    print('✓ Ratings: OK (' + ratings + ' documents)');
  } else {
    print('✗ Ratings: Expected ~' + expectedRatings + ', found ' + ratings);
    allGood = false;
  }
  
  if (stats > expectedStats * 0.1) {
    print('✓ Stats: OK (' + stats + ' documents)');
  } else {
    print('✗ Stats: Expected ~' + expectedStats + ', found ' + stats);
    allGood = false;
  }
  
  if (favs > expectedFavs * 0.1) {
    print('✓ Favs: OK (' + favs + ' documents)');
  } else {
    print('✗ Favs: Expected ~' + expectedFavs + ', found ' + favs);
    allGood = false;
  }
  
  print('');
  if (allGood) {
    print('✓ All data imported correctly!');
    quit(0);
  } else {
    print('✗ Data import incomplete or incorrect');
    quit(1);
  }
" 2>/dev/null

# Cattura il codice di uscita del comando precedente
VERIFY_RESULT=$?

echo "========================================"

if [ $VERIFY_RESULT -ne 0 ]; then
    echo ""
    echo "Data is missing or incomplete. Starting automatic import..."
    echo ""
    
    # Richiama import.sh
    if [ -f "./db/mongo/import.sh" ]; then
        ./db/mongo/import.sh
        if [ $? -eq 0 ]; then
            echo ""
            echo "Import completed successfully!"
            echo ""
            
            # Richiama index.sh
            if [ -f "./db/mongo/index.sh" ]; then
                echo "Starting indexing..."
                ./db/mongo/index.sh
                if [ $? -eq 0 ]; then
                    echo ""
                    echo "✓ Automatic setup completed successfully!"
                    exit 0
                else
                    echo ""
                    echo "✗ Indexing failed!"
                    exit 1
                fi
            else
                echo "✗ index.sh not found!"
                exit 1
            fi
        else
            echo ""
            echo "✗ Import failed!"
            exit 1
        fi
    else
        echo "✗ import.sh not found!"
        exit 1
    fi
else
    echo "✓ Database verification passed!"
    exit 0
fi