-- DATABASE: anime_core

-- Tabella per gli anime
CREATE TABLE anime (
    anime_id INT PRIMARY KEY,
    title VARCHAR(50),
    title_english VARCHAR(50),
    title_japanese VARCHAR(50),
    type VARCHAR(50),
    episodes INT,
    status VARCHAR(50),
    aired_from DATE,
    aired_to DATE,
    score DECIMAL(4,2),
    scored_by INT,
    rank INT,
    popularity INT,
    members INT,
    favorites INT,
    synopsis TEXT,
    background TEXT,
    premiered VARCHAR(50),
    broadcast VARCHAR(100),
    source VARCHAR(100),
    duration VARCHAR(100),
    rating VARCHAR(50)
);

-- Tabella per i personaggi
CREATE TABLE characters (
    character_id INT PRIMARY KEY,
    url VARCHAR(50) UNIQUE,
    name VARCHAR(50),
    name_kanji VARCHAR(50),
    image_url VARCHAR(50),
    favorites INT,
    about TEXT
);

-- Nicknames
CREATE TABLE character_nicknames (
    character_id INT NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    PRIMARY KEY (character_id, nickname),
    FOREIGN KEY (character_id) REFERENCES characters(character_id)
);

-- Opere anime dei personaggi
CREATE TABLE character_anime_works (
    character_id INT NOT NULL,
    anime_id INT NOT NULL,
    role VARCHAR(100),
    PRIMARY KEY (character_id, anime_id),
    FOREIGN KEY (character_id) REFERENCES characters(character_id),
    FOREIGN KEY (anime_id) REFERENCES anime(anime_id)
);

-- Persone (staff, doppiatori, ecc.)
CREATE TABLE person (
    person_id INT PRIMARY KEY,
    name VARCHAR(50),
    given_name VARCHAR(50),
    family_name VARCHAR(50),
    birthday DATE,
    website VARCHAR(50),
    image_url VARCHAR(50),
    favorites INT
);

-- Nomi alternativi
CREATE TABLE person_alternate_names (
    person_id INT NOT NULL,
    alternate_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (person_id, alternate_name),
    FOREIGN KEY (person_id) REFERENCES person(person_id)
);

-- Dettagli persone
CREATE TABLE person_details (
    person_id INT NOT NULL,
    detail_type VARCHAR(100) NOT NULL,
    detail_value TEXT,
    PRIMARY KEY (person_id, detail_type),
    FOREIGN KEY (person_id) REFERENCES person(person_id)
);

-- Opere anime delle persone
CREATE TABLE person_anime_works (
    person_id INT NOT NULL,
    anime_id INT NOT NULL,
    role VARCHAR(100),
    PRIMARY KEY (person_id, anime_id, role),
    FOREIGN KEY (person_id) REFERENCES person(person_id),
    FOREIGN KEY (anime_id) REFERENCES anime(anime_id)
);

-- Lavori di doppiaggio
CREATE TABLE person_voice_works (
    person_id INT NOT NULL,
    character_id INT NOT NULL,
    anime_id INT NOT NULL,
    PRIMARY KEY (person_id, character_id, anime_id),
    FOREIGN KEY (person_id) REFERENCES person(person_id),
    FOREIGN KEY (character_id) REFERENCES characters(character_id),
    FOREIGN KEY (anime_id) REFERENCES anime(anime_id)
);

-- Raccomandazioni
CREATE TABLE recommendations (
    anime_id INT NOT NULL,
    recommended_anime_id INT NOT NULL,
    recommendation_count INT,
    PRIMARY KEY (anime_id, recommended_anime_id),
    FOREIGN KEY (anime_id) REFERENCES anime(anime_id),
    FOREIGN KEY (recommended_anime_id) REFERENCES anime(anime_id)
);

-- Dettagli generali (resta qui)
CREATE TABLE details (
    entity_id INT,
    entity_type VARCHAR(50), -- anime, character, person
    detail_key VARCHAR(255),
    detail_value TEXT,
    PRIMARY KEY (entity_id, entity_type, detail_key)
);

-- Indici
CREATE INDEX idx_characters_name ON characters(name);
CREATE INDEX idx_anime_title ON anime(title);
CREATE INDEX idx_person_name ON person(name);