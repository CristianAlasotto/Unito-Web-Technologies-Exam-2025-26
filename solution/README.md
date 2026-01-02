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

Import the three dynamic collections (ratings, favs, stats) and the indexes into MongoDB:
```bash
./db/mongo/import.sh
./db/mongo/index.sh
```

**Expected time:**
- Import: ~10 minutes
- Indexing: ~7-8 minutes
- **Total: ~17-18 minutes**

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

**Basic anime lookup:**
```bash
docker-compose exec mongo mongosh anime_dynamic --eval '
var start = new Date();
var count = db.ratings.find({anime_id: 1}).count();
print("Query speed: " + count + " results in " + (new Date() - start) + "ms");
'
```
Expected: < 100ms

**Sorted query (anime ratings sorted by username):**
```bash
docker-compose exec mongo mongosh anime_dynamic --eval '
var start = new Date();
var docs = db.ratings.find({anime_id: 1}).sort({username: 1}).limit(10).toArray();
print("Query speed (Sorted Query): " + docs.length + " results in " + (new Date() - start) + "ms");
'
```
Expected: < 100ms

**Stats lookup:**
```bash
docker-compose exec mongo mongosh anime_dynamic --eval '
var start = new Date();
var doc = db.getCollection("stats").find({mal_id: 1}).hint("idx_mal_id").limit(1).toArray();
print("Query speed (Stats Lookup): Found " + doc.length + " doc in " + (new Date() - start) + "ms");
'
```
Expected: < 50ms

**Favs by ID:**
```bash
docker-compose exec mongo mongosh anime_dynamic --eval '
var start = new Date();
var count = db.favs.find({id: 1}).hint("idx_id").count();
print("Query speed (Favs by ID): " + count + " results in " + (new Date() - start) + "ms");
'
```
Expected: < 50ms

**User ratings lookup:**
```bash
docker-compose exec mongo mongosh anime_dynamic --eval '
var sample = db.ratings.findOne();
var targetUser = sample ? sample.username : "unknown";
print("Testing query for user: " + targetUser);

var start = new Date();
var count = db.ratings.find({username: targetUser}).hint("idx_username").count(); 
print("Query speed (User Ratings): " + count + " results in " + (new Date() - start) + "ms");
'
```
Expected: < 10ms

**Without indexes:** These queries would take 60,000+ ms

### Access Mongo Express UI

http://localhost:5051
- Username: `admin`
- Password: `admin`

**Note:** Data persists across restarts. To reset completely, use `docker-compose down -v`   