# Quick Start

Insert dataset files in "solution/data"

## Prerequisites

- Docker Desktop running
- Node.js installed
- Dataset in .csv files in the `data/` folder (essential only for initial population)

## Starting Complete Development Environment (for Linux/MacOS)

### Automated Bash Script

```bash
cd solution
./start-dev.sh
```

## Environment Variables Configuration (Global)

- Global file: `.env` in the `solution/` folder (`.env.example` also exists).
- Automatically loaded by:
   - Docker Compose (for variable interpolation)
   - Express Server (via dotenv) for `LOG_HTTP_ENABLED`, `LOG_API_ENABLED`, `DATA_*`, `PORT`.

To disable HTTP/API logs:

```env
LOG_HTTP_ENABLED=false
LOG_API_ENABLED=false
```

To customize service URLs:

```env
DATA_EXPRESS_URL=http://localhost:3001
DATA_SPRING_URL=http://localhost:8080
```

## Starting Manual Multi-Platform Development Environment

### Starting Docker + PostgreSQL Database Population

```bash
# 1. Start container + DB population (first time)
docker compose up -d postgres
docker compose up -d

# 2. Install dependencies (first time)
cd services/main-server-express
npm install

# 3. Start development server
npm run dev
```

> Tip: copy `.env.example` to `.env` and adapt the values.

### MongoDB Population/Verification

```bash
cd ../../db/mongo/verify.sh
```

### Starting Express Server

```bash
cd solution

# First time
npm run install:all

# Start complete environment
npm run dev

# Stop
npm run stop
```

---

## MongoDB Setup

**Expected time:**
- Import: ~10 minutes
- Indexing: ~7-8 minutes
- **Total: ~17-18 minutes**

### Automatic Data Import/Verification

Import and verify data with:

```bash
./db/mongo/verify.sh
```

### Manual Data Import

Import the three dynamic collections (ratings, favs, stats) and the indexes into MongoDB:

```bash
./db/mongo/import.sh
./db/mongo/index.sh
```

### Verify Import

Check that all data was imported successfully:

```bash
docker exec -i solution-mongo-1 mongosh anime_dynamic --quiet --eval "print('Ratings:', db.ratings.estimatedDocumentCount(), '| Stats:', db.getCollection('stats').estimatedDocumentCount(), '| Favs:', db.favs.estimatedDocumentCount())"
```

Expected output:

```
Ratings: 248596714 | Stats: 57910 | Favs: 8357494
```

---

## Service Access

- **Frontend**: http://localhost:3000
- **pgAdmin**: http://localhost:5050
   - Email: `admin@example.com`
   - Password: `admin`
- **Mongo Express**: http://localhost:5051
   - Username: `admin`
   - Password: `admin`
- **(EXTRA) Dozzle logs**: http://localhost:9999

### Database Connection in pgAdmin

1. Add New Server
2. **General** Tab
    - Name: `local-postgres`
3. **Connection** Tab
    - Host name/address: `postgres`
         - **Important**: not `localhost` (pgAdmin is in a container; localhost there is the pgAdmin container itself)
    - Port: `5432`
    - Maintenance database: `anime_db`
    - Username: `anime_user`
    - Password: `anime_pass`

---

## Debugging

```bash
docker compose ps -a   # Container status
```

### Test PostgreSQL Database Population

```bash
docker compose exec postgres psql -U anime_user -d anime_db -c "SELECT count(*) FROM profiles;"
```

### Docker Container Removal

```bash
docker compose down # If previously active
docker compose down -v --rmi all

docker ps -a  # Verify no active containers remain
```

