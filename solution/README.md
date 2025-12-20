# ColluraCorrendoAlasotto

## Start development environment

### Linux
sudo systemctl start docker

### MacOS
open -a Docker

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