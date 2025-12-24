#!/bin/bash

echo "🚀 Avvio ambiente di sviluppo..."

# Controlla se Docker è in esecuzione
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker non è in esecuzione. Avvialo prima di continuare."
    exit 1
fi

# Avvia i container
echo "📦 Avvio container Docker..."
docker compose up -d

# Attendi che i database siano pronti
echo "⏳ Attendo che i database siano pronti..."
sleep 5

# Controlla lo stato dei container
echo "🔍 Verifica stato container..."
docker compose ps

# Naviga nella cartella del server
cd services/main-server-express

# Installa le dipendenze se necessario
if [ ! -d "node_modules" ]; then
    echo "📥 Installazione dipendenze..."
    npm install
fi

# Avvia il server in modalità dev
echo "🌐 Avvio server Express..."
npm run dev