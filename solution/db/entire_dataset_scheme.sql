-- Tabella per i profili utente
CREATE TABLE profiles (
    username VARCHAR(255) PRIMARY KEY,
    join_date DATE,
    location VARCHAR(255),
    birth_date DATE,
    gender VARCHAR(50)
);

-- Tabella per gli anime
CREATE TABLE anime (
    anime_id INT PRIMARY KEY,
    title VARCHAR(500),
    title_english VARCHAR(500),
    title_japanese VARCHAR(500),
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
    character_id INT,
    url VARCHAR(500),
    name VARCHAR(500),
    name_kanji VARCHAR(500),
    image_url VARCHAR(500),
    favorites INT,
    about TEXT,
    PRIMARY KEY (character_id, url)
);

-- Tabella per i soprannomi dei personaggi
CREATE TABLE character_nicknames (
    character_id INT,
    nickname VARCHAR(500),
    FOREIGN KEY (character_id) REFERENCES characters(character_id)
);

-- Tabella per le opere anime dei personaggi
CREATE TABLE character_anime_works (
    character_id INT,
    anime_id INT,
    role VARCHAR(100),
    FOREIGN KEY (character_id) REFERENCES characters(character_id),
    FOREIGN KEY (anime_id) REFERENCES anime(anime_id)
);

-- Tabella per le persone (staff, doppiatori, ecc.)
CREATE TABLE person (
    person_id INT PRIMARY KEY,
    name VARCHAR(500),
    given_name VARCHAR(500),
    family_name VARCHAR(500),
    birthday DATE,
    website VARCHAR(500),
    image_url VARCHAR(500),
    favorites INT
);

-- Tabella per i nomi alternativi delle persone
CREATE TABLE person_alternate_names (
    person_id INT,
    alternate_name VARCHAR(500),
    FOREIGN KEY (person_id) REFERENCES person(person_id)
);

-- Tabella per i dettagli delle persone
CREATE TABLE person_details (
    person_id INT,
    detail_type VARCHAR(100),
    detail_value TEXT,
    FOREIGN KEY (person_id) REFERENCES person(person_id)
);

-- Tabella per le opere anime delle persone
CREATE TABLE person_anime_works (
    person_id INT,
    anime_id INT,
    role VARCHAR(100),
    FOREIGN KEY (person_id) REFERENCES person(person_id),
    FOREIGN KEY (anime_id) REFERENCES anime(anime_id)
);

-- Tabella per i lavori di doppiaggio
CREATE TABLE person_voice_works (
    person_id INT,
    character_id INT,
    anime_id INT,
    FOREIGN KEY (person_id) REFERENCES person(person_id),
    FOREIGN KEY (character_id) REFERENCES characters(character_id),
    FOREIGN KEY (anime_id) REFERENCES anime(anime_id)
);

-- Tabella per le valutazioni degli utenti
CREATE TABLE ratings (
    username VARCHAR(255),
    anime_id INT,
    rating INT,
    FOREIGN KEY (username) REFERENCES profiles(username),
    FOREIGN KEY (anime_id) REFERENCES anime(anime_id),
    PRIMARY KEY (username, anime_id)
);

-- Tabella per i preferiti degli utenti
CREATE TABLE favs (
    username VARCHAR(255),
    fav_type VARCHAR(50), -- anime, character, people, company
    fav_id INT,
    FOREIGN KEY (username) REFERENCES profiles(username),
    PRIMARY KEY (username, fav_type, fav_id)
);

-- Tabella per le raccomandazioni
CREATE TABLE recommendations (
    anime_id INT,
    recommended_anime_id INT,
    recommendation_count INT,
    FOREIGN KEY (anime_id) REFERENCES anime(anime_id),
    FOREIGN KEY (recommended_anime_id) REFERENCES anime(anime_id),
    PRIMARY KEY (anime_id, recommended_anime_id)
);

-- Tabella per le statistiche
CREATE TABLE stats (
    anime_id INT,
    watching INT,
    completed INT,
    on_hold INT,
    dropped INT,
    plan_to_watch INT,
    FOREIGN KEY (anime_id) REFERENCES anime(anime_id),
    PRIMARY KEY (anime_id)
);

-- Tabella per i dettagli generali
CREATE TABLE details (
    entity_id INT,
    entity_type VARCHAR(50), -- anime, character, person
    detail_key VARCHAR(255),
    detail_value TEXT,
    PRIMARY KEY (entity_id, entity_type, detail_key)
);

-- Indici per migliorare le performance
CREATE INDEX idx_characters_name ON characters(name);
CREATE INDEX idx_anime_title ON anime(title);
CREATE INDEX idx_person_name ON person(name);
CREATE INDEX idx_ratings_username ON ratings(username);
CREATE INDEX idx_favs_username ON favs(username);
CREATE INDEX idx_favs_type ON favs(fav_type);