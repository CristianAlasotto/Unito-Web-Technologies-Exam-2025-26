# MongoDB Data Server

A Node.js service that provides access to MongoDB collections for favorites, ratings, and statistics.

## Location
```
solution/services/data-server-mongo/
```

## Setup

Install dependencies:
```bash
npm install
```

Configure MongoDB connection in your environment or configuration file.

## Running the Server

Start the server:
```bash
npm start
```

Expected output:
```
MongoDB Connected successfully
```

The server runs on `http://localhost:3000`

## Testing API Endpoints

### Get Favorites
```bash
curl http://localhost:3001/getfavs
```

### Get Ratings
```bash
curl http://localhost:3001/getratings
```

### Get Statistics
```bash
curl http://localhost:3001/getstats
```

## Requirements

- Node.js
- MongoDB instance (local or remote)
- npm packages: `express`, `mongodb` (or `mongoose`)

## Troubleshooting

If the server fails to connect:

- Verify MongoDB is running
- Check connection string in configuration
- Ensure network access to MongoDB instance
- Verify required collections exist in the database