# ColluraCorrendoAlasotto

## Avvio rapido

### Prerequisiti
- Docker Desktop in esecuzione
- Node.js installato

### Metodo 1: Script Bash (consigliato per Linux/macOS)
```bash
./solution/start-dev.sh
```

### Metodo 2: NPM Scripts (multi-piattaforma)
```bash
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
# 1. Avvia container
docker compose up -d

# 2. Installa dipendenze (prima volta)
cd services/main-server-express
npm install

# 3. Avvia server
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
npm run docker:logs    # Vedi i log dei container
docker compose ps -a   # Status dei container
```