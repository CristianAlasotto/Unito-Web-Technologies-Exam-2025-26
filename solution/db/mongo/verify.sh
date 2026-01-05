#!/bin/bash

CONTAINER_NAME="solution-mongo-1"
DB_NAME="anime_dynamic"

echo "Verifying MongoDB data existence..."
echo "========================================"

# 1. Verifica che il container sia in esecuzione
if ! docker ps | grep -q $CONTAINER_NAME; then
    echo "❌ MongoDB container is not running!"
    exit 1
fi

docker exec -i $CONTAINER_NAME mongosh $DB_NAME --quiet --eval "
  var ratings = db.ratings.estimatedDocumentCount();
  var stats = db.getCollection('stats').estimatedDocumentCount();
  var favs = db.favs.estimatedDocumentCount();

  print('Current Status:');
  print('- Ratings: ' + ratings + ' docs');
  print('- Stats:   ' + stats + ' docs');
  print('- Favs:    ' + favs + ' docs');
  print('');

  var expectedRatings = 124298357;
  var expectedStats = 28955;
  var expectedFavs = 4178747;

  var allGood = true;

  // Verifica soglia minima (10%) per considerare i dati presenti
  if (ratings < expectedRatings * 0.1) {
    print('✗ Ratings collection is empty or incomplete.');
    allGood = false;
  }
  if (stats < expectedStats * 0.1) {
    print('✗ Stats collection is empty or incomplete.');
    allGood = false;
  }
  if (favs < expectedFavs * 0.1) {
    print('✗ Favs collection is empty or incomplete.');
    allGood = false;
  }

  if (allGood) {
    print('\n✅ All data is present and verified.');
    quit(0);
  } else {
    print('\n❌ Verification FAILED: Data is missing.');
    print('Please ensure your Docker volumes are mounted correctly.');
    quit(1);
  }
" 2>/dev/null

VERIFY_RESULT=$?

echo "========================================"

if [ $VERIFY_RESULT -eq 0 ]; then
    echo "Verification complete: System is ready."
    exit 0
else
    echo "Volume is not present"
    exit 1
fi