# ColluraCorrendoAlasotto

## Start development environment

insert dataset files in "solution/data"

#### Linux
```bash
sudo systemctl start docker
```

#### MacOS
```bash
open -a Docker
```

### Start containers
```bash
docker-compose up -d
```

## In pgAdmin

1. Add New Server
2. Tab **General**
   - Name: `local-postgres`
3. Tab **Connection**
   - Host name/address: `postgres`
     - **Importante**: non `localhost` (pgAdmin è in un container; localhost lì dentro è il container pgAdmin stesso)
   - Port: `5432`
   - Maintenance database: `anime_db`
   - Username: `anime_user`
   - Password: `anime_pass`

### Run reset
```bash
docker compose down -v
docker compose up -d
```

## Debugging
```bash
docker compose ps -a
```

test postgres seeding logs
```bash
docker compose down -v && docker compose up -d postgres && docker compose logs -f postgres
```

## MongoDB Setup

### Data Import

Import the three dynamic collections (ratings, favs, stats) into MongoDB:
```bash
./db/mongo/import.sh
```

**Expected time:**
- Import: ~11 minutes
- Indexing: ~7-8 minutes
- **Total: ~18 minutes**

### Verify Import

Check that all data was imported successfully:
```bash
docker-compose exec mongo mongosh anime_dynamic --quiet --eval "
  print('Ratings: ' + db.ratings.countDocuments() + ' documents');
  print('Stats: ' + db.getCollection('stats').countDocuments() + ' documents');
  print('Favs: ' + db.favs.countDocuments() + ' documents');
"
```

Expected output:
```
Ratings: 124298357 documents
Stats: 28955 documents
Favs: 4178747 documents
```

### Test Query Performance

Verify that indexes are working correctly:
```bash
docker-compose exec mongo mongosh anime_dynamic --eval '
var start = new Date();
var count = db.ratings.find({anime_id: 1}).count();
print("Query speed: " + count + " results in " + (new Date() - start) + "ms");
'
```

**Expected result:** < 100ms (with indexes)  
**Without indexes:** Would take 60,000+ ms for the same query

### Access Mongo Express UI

http://localhost:5051
- Username: `admin`
- Password: `admin`

**Note:** Data persists across restarts. To reset completely, use `docker-compose down -v`