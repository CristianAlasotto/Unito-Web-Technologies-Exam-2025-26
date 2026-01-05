-- DATABASE: anime_core
-- This schema matches CSV structure exactly (same number and names of columns)
-- Duplicates will be dropped during import

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
    title VARCHAR(600),
    title_japanese VARCHAR(550),
    url VARCHAR(500),
    image_url VARCHAR(450),
    type VARCHAR(400),
    status VARCHAR(450),
    score DECIMAL(4,2),
    scored_by INT,
    start_date DATE,
    end_date DATE,
    synopsis TEXT,
    rank INT,
    popularity INT,
    members INT,
    favorites INT,
    genres VARCHAR(550),
    studios VARCHAR(500),
    themes VARCHAR(500),
    demographics VARCHAR(400),
    source TEXT,
    rating VARCHAR(420),
    episodes DECIMAL(6,1),
    season TEXT,
    year DECIMAL(6,1),
    producers VARCHAR(600),
    explicit_genres VARCHAR(200),
    licensors VARCHAR(250),
    streaming VARCHAR(200)
);

-- Tabella per i personaggi (EXACT CSV structure - character_mal_id as PRIMARY KEY)
CREATE TABLE characters (
    character_mal_id INT PRIMARY KEY,
    url VARCHAR(200),
    name VARCHAR(150),
    name_kanji VARCHAR(150),
    image VARCHAR(200),
    favorites INT,
    about TEXT
);

CREATE INDEX idx_characters_name ON characters(name);

-- Nicknames (EXACT CSV structure)
CREATE TABLE character_nicknames (
    character_mal_id INT NOT NULL,
    nickname VARCHAR(100),
    PRIMARY KEY (character_mal_id, nickname),
    FOREIGN KEY (character_mal_id) REFERENCES characters(character_mal_id)
);

-- Opere anime dei personaggi (EXACT CSV structure)
CREATE TABLE character_anime_works (
    anime_mal_id INT NOT NULL,
    character_mal_id INT NOT NULL,
    character_name VARCHAR(100),
    role VARCHAR(100),
    PRIMARY KEY (character_mal_id, anime_mal_id),
    FOREIGN KEY (character_mal_id) REFERENCES characters(character_mal_id),
    FOREIGN KEY (anime_mal_id) REFERENCES details(mal_id)
);

-- Dettagli persone (EXACT CSV structure - person_mal_id as PRIMARY KEY)
CREATE TABLE person_details (
    person_mal_id INT PRIMARY KEY,
    url VARCHAR(100),
    website_url VARCHAR(300),
    image_url VARCHAR(100),
    name VARCHAR(100),
    given_name VARCHAR(100),
    family_name VARCHAR(100),
    birthday DATE,
    favorites INT,
    relevant_location VARCHAR(100)
);

CREATE INDEX idx_person_name ON person_details(name);

-- Nomi alternativi (EXACT CSV structure)
CREATE TABLE person_alternate_names (
    person_mal_id INT NOT NULL,
    alt_name VARCHAR(100),
    PRIMARY KEY (person_mal_id, alt_name),
    FOREIGN KEY (person_mal_id) REFERENCES person_details(person_mal_id)
);

-- Opere anime delle persone (EXACT CSV structure)
CREATE TABLE person_anime_works (
    person_mal_id INT NOT NULL,
    position VARCHAR(150),
    anime_mal_id INT NOT NULL,
    PRIMARY KEY (person_mal_id, position, anime_mal_id),
    FOREIGN KEY (person_mal_id) REFERENCES person_details(person_mal_id),
    FOREIGN KEY (anime_mal_id) REFERENCES details(mal_id)
);

-- Lavori di doppiaggio (EXACT CSV structure)
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

-- Raccomandazioni (EXACT CSV structure)
CREATE TABLE recommendations (
    mal_id INT NOT NULL,
    recommendation_mal_id INT NOT NULL,
    PRIMARY KEY (mal_id, recommendation_mal_id),
    FOREIGN KEY (mal_id) REFERENCES details(mal_id),
    FOREIGN KEY (recommendation_mal_id) REFERENCES details(mal_id)
);