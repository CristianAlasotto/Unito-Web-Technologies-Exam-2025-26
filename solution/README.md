# Avvio rapido

Insert dataset files in "solution/data"

### Prerequisiti
- Docker Desktop in esecuzione
- Node.js installato
- Dataset in file .csv nella cartella `data/` (fondamentale solo per popolamento iniziale)

## Avvio ambiente di sviluppo Express Server
### Metodo 1: Script Bash Automatizzato (per Linux/MacOS)
```bash
cd solution
./start-dev.sh
```

### Metodo 2: Manuale multi-piattaforma
#### Avvio ambiente di sviluppo Docker
```bash
# 1. Avvia container + popolamento DB (prima volta)
docker compose up -d postgres
docker compose up -d

# 2. Installa dipendenze (prima volta)
cd services/main-server-express
npm install

# 3. Avvia server di sviluppo
npm run dev
```

#### Avvio Express server
```bash
cd solution

# Prima volta
npm run install:all

# Avvio ambiente completo
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

# Accesso servizi

- **Frontend**: http://localhost:3000
- **pgAdmin**: http://localhost:5050
  - Email: `admin@example.com`
  - Password: `admin`
- **Mongo Express**: http://localhost:5051
  - Username: `admin`
  - Password: `admin`

### Connessione database in pgAdmin

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

---

# Debugging
```bash
docker compose ps -a   # Status dei container
```
## Test popolamento DB postgreSQL
```bash
docker compose exec postgres psql -U anime_user -d anime_db -c "SELECT count(*) FROM profiles;"
```

## Eliminazione container Docker
```bash
docker compose down # Se precedentemente attivo
docker compose down -v --rmi all

docker ps -a  # Verifica che non ci siano container attivi
```
