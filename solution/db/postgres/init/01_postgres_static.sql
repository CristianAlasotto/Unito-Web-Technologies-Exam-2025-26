-- DATABASE: anime_core

-- Tabella per i profili utente
CREATE TABLE profiles (
    username VARCHAR(255) PRIMARY KEY,
    gender VARCHAR(50),
    birthday DATE,
    location VARCHAR(255),
    joined DATE,
    watching INT,
    completed INT,
    on_hold INT,
    dropped INT,
    plan_to_watch INT
);

-- Tabella per gli anime
CREATE TABLE details (
    mal_id INT PRIMARY KEY,
    title VARCHAR(50),
    title_japanese VARCHAR(50),
    url VARCHAR(75),
    image_url VARCHAR(75),
    type VARCHAR(50),
    status VARCHAR(50),
    score DECIMAL(4,2),
    scored_by INT,
    start_date DATE,
    end_date DATE,
    synopsis TEXT,
    rank INT,
    popularity INT,
    members INT,
    favorites INT,
    genres VARCHAR(50),
    studios VARCHAR(50),
    themes VARCHAR(50),
    demographics VARCHAR(50),
    source TEXT,
    rating VARCHAR(50),
    episodes DECIMAL(4,1),
    season TEXT,
    year DECIMAL(4,1),
    producers VARCHAR(500),
    explicit_genres VARCHAR(10),
    licensors VARCHAR(50),
    streaming VARCHAR(50)
);

-- Tabella per i personaggi
CREATE TABLE characters (
    character_mal_id INT PRIMARY KEY,
    url VARCHAR(75) UNIQUE,
    name VARCHAR(50),
    name_kanji VARCHAR(50),
    image VARCHAR(75),
    favorites INT,
    about TEXT
);

-- Nicknames
CREATE TABLE character_nicknames (
    character_mal_id INT NOT NULL,
    nickname VARCHAR(50),
    PRIMARY KEY (character_mal_id, nickname),
    FOREIGN KEY (character_mal_id) REFERENCES characters(character_mal_id)
);

-- Opere anime dei personaggi
CREATE TABLE character_anime_works (
    anime_mal_id INT NOT NULL,
    character_mal_id INT NOT NULL,
    characters_name VARCHAR(50),
    role VARCHAR(100),
    PRIMARY KEY (character_mal_id,  anime_mal_id),
    FOREIGN KEY (character_mal_id) REFERENCES characters(character_mal_id),
    FOREIGN KEY (anime_mal_id) REFERENCES details(mal_id)
);

-- Dettagli persone
CREATE TABLE person_details (
    person_mal_id INT PRIMARY KEY,
    url VARCHAR(75),
    website_url VARCHAR(75),
    image_url VARCHAR(75),
    name VARCHAR(50),
    given_name VARCHAR(50),
    family_name VARCHAR(50),
    birthday DATE,
    favorites INT,
    relevant_location VARCHAR(50)
);

-- Nomi alternativi
CREATE TABLE person_alternate_names (
    person_mal_id INT NOT NULL,
    alt_name VARCHAR(50),
    PRIMARY KEY (person_mal_id, alt_name),
    FOREIGN KEY (person_mal_id) REFERENCES person_details(person_mal_id)
);

-- Opere anime delle persone
CREATE TABLE person_anime_works (
    person_mal_id INT NOT NULL,
    position VARCHAR(100),
    anime_mal_id INT NOT NULL,
    PRIMARY KEY (person_mal_id, position, anime_mal_id),
    FOREIGN KEY (person_mal_id) REFERENCES person_details(person_mal_id),
    FOREIGN KEY (anime_mal_id) REFERENCES details(mal_id)
);

-- Lavori di doppiaggio
CREATE TABLE person_voice_works (
    person_mal_id INT NOT NULL,
    role TEXT NOT NULL,
    anime_mal_id INT NOT NULL,
    character_mal_id INT NOT NULL,
    language TEXT,
    PRIMARY KEY (person_mal_id, character_mal_id, anime_mal_id),
    FOREIGN KEY (person_mal_id) REFERENCES person_details(person_mal_id),
    FOREIGN KEY (character_mal_id) REFERENCES characters(character_mal_id),
    FOREIGN KEY (anime_mal_id) REFERENCES details(mal_id)
);

-- Raccomandazioni
CREATE TABLE recommendations (
    mal_id INT NOT NULL,
    recommended_mal_id INT NOT NULL,
    PRIMARY KEY (mal_id, recommended_mal_id),
    FOREIGN KEY (mal_id) REFERENCES details(mal_id),
    FOREIGN KEY (recommended_mal_id) REFERENCES details(mal_id)
);

-- Indici
CREATE INDEX idx_characters_name ON characters(name);
CREATE INDEX idx_anime_title ON details(title);
CREATE INDEX idx_person_name ON person_details(name);