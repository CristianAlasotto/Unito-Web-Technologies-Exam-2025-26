# ColluraCorrendoAlasotto

## Avvio rapido

### Prerequisiti
- Docker Desktop in esecuzione
- Node.js installato
- Dataset in file .csv nella cartella `data/` (fondamentale solo per popolamento iniziale)

### Metodo 1: Script Bash (consigliato per Linux/MacOS)
```bash
cd solution
./start-dev.sh
```

### Metodo 2: NPM Scripts (multi-piattaforma)
```bash
cd solution

# Prima volta
npm run install:all

# Avvio ambiente completo
npm run dev

# Stop
npm run stop

# Reset database
npm run docker:reset
```

### Metodo manuale
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

## Accesso servizi

- **Frontend**: http://localhost:3000
- **pgAdmin**: http://localhost:5050
  - Email: `admin@example.com`
  - Password: `admin`
- **Mongo Express**: http://localhost:5051
  - Username: `admin`
  - Password: `admin`

## In pgAdmin

1. Add New Server
2. Tab **General**
   - Name: `local-postgres`
3. Tab **Connection**
   - Host name/address: `postgres`
   - Port: `5432`
   - Maintenance database: `anime_db`
   - Username: `anime_user`
   - Password: `anime_pass`

## Comandi utili

```bash
docker compose ps -a   # Status dei container
```

## Eliminazione container Docker
```bash
docker compose down # Se precedentemente attivo
docker rm -f $(docker ps -aq) # Elimina tutti i container
docker rmi -f $(docker images -aq) # Elimina tutte le immagini

docker ps -a  # Verifica che non ci siano container attivi
```

## Test popolamento DB
```bash
docker compose exec postgres psql -U anime_user -d anime_db -c "SELECT count(*) FROM profiles;"
```