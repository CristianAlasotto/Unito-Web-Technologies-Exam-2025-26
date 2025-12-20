-- DATABASE: user_domain

-- Profili
CREATE TABLE profiles (
    username VARCHAR(255) PRIMARY KEY,
    join_date DATE,
    location VARCHAR(255),
    birth_date DATE,
    gender VARCHAR(50)
);

-- Preferiti
CREATE TABLE favs (
    username VARCHAR(255) NOT NULL,
    fav_type VARCHAR(50) NOT NULL, -- anime, character, people, company
    fav_id INT NOT NULL,
    PRIMARY KEY (username, fav_type, fav_id),
    FOREIGN KEY (username) REFERENCES profiles(username)
);

CREATE INDEX idx_favs_username ON favs(username);
CREATE INDEX idx_favs_type ON favs(fav_type);

-- Statistiche per anime (reference esterna)
CREATE TABLE stats (
    anime_id INT PRIMARY KEY,  -- riferimento a anime_core.anime(anime_id)
    watching INT,
    completed INT,
    on_hold INT,
    dropped INT,
    plan_to_watch INT
);