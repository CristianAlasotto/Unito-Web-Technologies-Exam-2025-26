# Quick Start

## For Run

### From terminal

```bash
# Command diagram
./spring_start.sh <DB_HOST> <DB_PORT> <DB_NAME> <DB_USERNAME> <DB_PASSWORD> <SERVER_PORT>

# Command example
./spring_start.sh localhost 5432 anime_db anime_user ******* 8080
```

## For Test

### Prerequisite for terminal testing with curl

Install the jq packets for more understandable output:

#### For Linux Debian based:

```bash
sudo apt install jq
```

#### For Mac is pre-installed, but as the case may be:
```bash
brew install jq
```

### Curl command example:
```bash
curl "http://localhost:8080/api/details" | jq
```./spring_start.sh localhost 5432 anime_db anime_user anime_pass 8080./spring_start.sh localhost 5432 anime_db anime_user anime_pass 8080./spring_start.sh localhost 5432 anime_db anime_user anime_pass 8080

### It's possible to see the JSON returned in an understandable way from browser.