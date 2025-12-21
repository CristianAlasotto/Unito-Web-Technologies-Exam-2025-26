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

### Initial data import (first time only - takes 20-30 minutes)

Start MongoDB:
```bash
docker-compose up -d mongo
```

Import collections:
```bash
# Ratings (~4.2GB - 15-20 minutes)
docker exec solution-mongo-1 mongoimport \
  --db anime_dynamic \
  --collection ratings \
  --type csv \
  --headerline \
  --drop \
  --file /data/csv/ratings.csv

# Stats (~3.5MB - 10 seconds)
docker exec solution-mongo-1 mongoimport \
  --db anime_dynamic \
  --collection stats \
  --type csv \
  --headerline \
  --drop \
  --file /data/csv/stats.csv

# Favs (~98MB - 1 minute)
docker exec solution-mongo-1 mongoimport \
  --db anime_dynamic \
  --collection favs \
  --type csv \
  --headerline \
  --drop \
  --file /data/csv/favs.csv
```

Verify import:
```bash
docker exec solution-mongo-1 mongosh anime_dynamic --quiet --eval "
  print('Ratings: ' + db.ratings.countDocuments());
  print('Stats: ' + db.getCollection('stats').countDocuments());
  print('Favs: ' + db.favs.countDocuments());
"
```

**Note**: Data persists across restarts.

### Access Mongo Express UI
http://localhost:5051
- Username: `admin`
- Password: `admin`