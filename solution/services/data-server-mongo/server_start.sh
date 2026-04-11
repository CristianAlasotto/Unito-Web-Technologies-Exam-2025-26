#!/bin/bash

DATA_EXPRESS_PORT=$1

# --- Mongo Expres Server folder ---
cd services/data-server-mongo

echo "📥 Installing dependencies..."
npm_install_output=$(npm install 2>&1)
npm_install_status=$?
echo "$npm_install_output"

if [ $npm_install_status -ne 0 ] || echo "$npm_install_output" | grep -qiE "npm ERR!|vulnerab"; then
    echo "🛠️ Issues detected during npm install, running npm audit fix..."
    npm audit fix --silent || true
fi

# Start server in dev mode
echo "🌐 Starting MONGO Express server... :$DATA_EXPRESS_PORT"
DATA_EXPRESS_PORT=$DATA_EXPRESS_PORT npm start