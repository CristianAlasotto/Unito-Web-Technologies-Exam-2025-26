\set ON_ERROR_STOP on

BEGIN;

TRUNCATE TABLE
  character_nicknames,
  character_anime_works,
  person_alternate_names,
  person_details,
  person_anime_works,
  person_voice_works,
  recommendations,
  details,
  characters,
  person,
  anime,
  profiles;

---------------------------------------------------
-- STAGING (TEMP TABLES)
---------------------------------------------------

CREATE TEMP TABLE stg_profiles (
  username TEXT,
  gender TEXT,
  birthday TEXT,
  location TEXT,
  joined TEXT,
  watching TEXT,
  completed TEXT,
  on_hold TEXT,
  dropped TEXT,
  plan_to_watch TEXT
);

CREATE TEMP TABLE stg_anime (
  anime_id TEXT,
  title TEXT,
  title_english TEXT,
  title_japanese TEXT,
  type TEXT,
  episodes TEXT,
  status TEXT,
  aired_from TEXT,
  aired_to TEXT,
  score TEXT,
  scored_by TEXT,
  rank TEXT,
  popularity TEXT,
  members TEXT,
  favorites TEXT,
  synopsis TEXT,
  background TEXT,
  premiered TEXT,
  broadcast TEXT,
  source TEXT,
  duration TEXT,
  rating TEXT
);

CREATE TEMP TABLE stg_characters (
  character_id TEXT,
  url TEXT,
  name TEXT,
  name_kanji TEXT,
  image_url TEXT,
  favorites TEXT,
  about TEXT
);

CREATE TEMP TABLE stg_character_nicknames (
  character_id TEXT,
  nickname TEXT
);

CREATE TEMP TABLE stg_character_anime_works (
  character_id TEXT,
  anime_id TEXT,
  role TEXT
);

CREATE TEMP TABLE stg_person (
  person_id TEXT,
  name TEXT,
  given_name TEXT,
  family_name TEXT,
  birthday TEXT,
  website TEXT,
  image_url TEXT,
  favorites TEXT
);

CREATE TEMP TABLE stg_person_alternate_names (
  person_id TEXT,
  alternate_name TEXT
);

CREATE TEMP TABLE stg_person_details (
  person_id TEXT,
  detail_type TEXT,
  detail_value TEXT
);

CREATE TEMP TABLE stg_person_anime_works (
  person_id TEXT,
  anime_id TEXT,
  role TEXT
);

CREATE TEMP TABLE stg_person_voice_works (
  person_id TEXT,
  character_id TEXT,
  anime_id TEXT
);

CREATE TEMP TABLE stg_recommendations (
  anime_id TEXT,
  recommended_anime_id TEXT,
  recommendation_count TEXT
);

CREATE TEMP TABLE stg_details (
  entity_id TEXT,
  entity_type TEXT,
  detail_key TEXT,
  detail_value TEXT
);

---------------------------------------------------
-- LOAD CSV (server-side: i CSV sono montati nel container in /import)
---------------------------------------------------

-- Import profiles
COPY stg_profiles(username, gender, birthday, location, joined, watching, completed, on_hold, dropped, plan_to_watch)
FROM '/import/profiles.csv'
DELIMITER ','
CSV HEADER;

-- Import anime
COPY stg_anime(anime_id, title, title_english, title_japanese, type, episodes, status, aired_from, aired_to, score, scored_by, rank, popularity, members, favorites, synopsis, background, premiered, broadcast, source, duration, rating)
FROM '/import/anime.csv'
DELIMITER ','
CSV HEADER;

-- Import characters
COPY stg_characters(character_id, url, name, name_kanji, image_url, favorites, about)
FROM '/import/characters.csv'
DELIMITER ','
CSV HEADER;

-- Import character nicknames
COPY stg_character_nicknames(character_id, nickname)
FROM '/import/character_nicknames.csv'
DELIMITER ','
CSV HEADER;

-- Import character anime works
COPY stg_character_anime_works(character_id, anime_id, role)
FROM '/import/character_anime_works.csv'
DELIMITER ','
CSV HEADER;

-- Import person
COPY stg_person(person_id, name, given_name, family_name, birthday, website, image_url, favorites)
FROM '/import/person.csv'
DELIMITER ','
CSV HEADER;

-- Import person alternate names
COPY stg_person_alternate_names(person_id, alternate_name)
FROM '/import/person_alternate_names.csv'
DELIMITER ','
CSV HEADER;

-- Import person details
COPY stg_person_details(person_id, detail_type, detail_value)
FROM '/import/person_details.csv'
DELIMITER ','
CSV HEADER;

-- Import person anime works
COPY stg_person_anime_works(person_id, anime_id, role)
FROM '/import/person_anime_works.csv'
DELIMITER ','
CSV HEADER;

-- Import person voice works
COPY stg_person_voice_works(person_id, character_id, anime_id)
FROM '/import/person_voice_works.csv'
DELIMITER ','
CSV HEADER;

-- Import recommendations
COPY stg_recommendations(anime_id, recommended_anime_id, recommendation_count)
FROM '/import/recommendations.csv'
DELIMITER ','
CSV HEADER;

-- Import details
COPY stg_details(entity_id, entity_type, detail_key, detail_value)
FROM '/import/details.csv'
DELIMITER ','
CSV HEADER;

---------------------------------------------------
-- INSERTS
---------------------------------------------------

-- Insert profiles
INSERT INTO profiles (username, gender, birthday, location, joined, watching, completed, on_hold, dropped, plan_to_watch)
SELECT
  LEFT(p.username, 255),
  LEFT(p.gender, 50),
  CASE WHEN LEFT(p.birthday,10) ~ '^\d{4}-\d{2}-\d{2}$' THEN LEFT(p.birthday,10)::DATE ELSE NULL END,
  LEFT(p.location, 255),
  CASE WHEN LEFT(p.joined,10) ~ '^\d{4}-\d{2}-\d{2}$' THEN LEFT(p.joined,10)::DATE ELSE NULL END,
  CASE WHEN p.watching ~ '^\d+$' THEN p.watching::INT ELSE NULL END,
  CASE WHEN p.completed ~ '^\d+$' THEN p.completed::INT ELSE NULL END,
  CASE WHEN p.on_hold ~ '^\d+$' THEN p.on_hold::INT ELSE NULL END,
  CASE WHEN p.dropped ~ '^\d+$' THEN p.dropped::INT ELSE NULL END,
  CASE WHEN p.plan_to_watch ~ '^\d+$' THEN p.plan_to_watch::INT ELSE NULL END
FROM stg_profiles p
WHERE p.username IS NOT NULL AND p.username <> '';

-- Insert anime
INSERT INTO anime (anime_id, title, title_english, title_japanese, type, episodes, status, aired_from, aired_to, score, scored_by, rank, popularity, members, favorites, synopsis, background, premiered, broadcast, source, duration, rating)
SELECT
  CASE WHEN a.anime_id ~ '^\d+$' THEN a.anime_id::INT ELSE NULL END,
  LEFT(a.title, 50),
  LEFT(a.title_english, 50),
  LEFT(a.title_japanese, 50),
  LEFT(a.type, 50),
  CASE WHEN a.episodes ~ '^\d+$' THEN a.episodes::INT ELSE NULL END,
  LEFT(a.status, 50),
  CASE WHEN LEFT(a.aired_from,10) ~ '^\d{4}-\d{2}-\d{2}$' THEN LEFT(a.aired_from,10)::DATE ELSE NULL END,
  CASE WHEN LEFT(a.aired_to,10) ~ '^\d{4}-\d{2}-\d{2}$' THEN LEFT(a.aired_to,10)::DATE ELSE NULL END,
  CASE WHEN a.score ~ '^\d+\.?\d*$' THEN a.score::DECIMAL(4,2) ELSE NULL END,
  CASE WHEN a.scored_by ~ '^\d+$' THEN a.scored_by::INT ELSE NULL END,
  CASE WHEN a.rank ~ '^\d+$' THEN a.rank::INT ELSE NULL END,
  CASE WHEN a.popularity ~ '^\d+$' THEN a.popularity::INT ELSE NULL END,
  CASE WHEN a.members ~ '^\d+$' THEN a.members::INT ELSE NULL END,
  CASE WHEN a.favorites ~ '^\d+$' THEN a.favorites::INT ELSE NULL END,
  a.synopsis,
  a.background,
  LEFT(a.premiered, 50),
  LEFT(a.broadcast, 100),
  LEFT(a.source, 100),
  LEFT(a.duration, 100),
  LEFT(a.rating, 50)
FROM stg_anime a
WHERE a.anime_id ~ '^\d+$'
  AND a.anime_id IS NOT NULL
  AND a.anime_id <> '';

-- Insert characters
INSERT INTO characters (character_id, url, name, name_kanji, image_url, favorites, about)
SELECT
  CASE WHEN c.character_id ~ '^\d+$' THEN c.character_id::INT ELSE NULL END,
  LEFT(c.url, 50),
  LEFT(c.name, 50),
  LEFT(c.name_kanji, 50),
  LEFT(c.image_url, 50),
  CASE WHEN c.favorites ~ '^\d+$' THEN c.favorites::INT ELSE NULL END,
  c.about
FROM stg_characters c
WHERE c.character_id ~ '^\d+$'
  AND c.character_id IS NOT NULL
  AND c.character_id <> '';

-- Insert character nicknames
INSERT INTO character_nicknames (character_id, nickname)
SELECT
  CASE WHEN cn.character_id ~ '^\d+$' THEN cn.character_id::INT ELSE NULL END,
  LEFT(cn.nickname, 50)
FROM stg_character_nicknames cn
WHERE cn.character_id ~ '^\d+$'
  AND cn.character_id IS NOT NULL
  AND cn.character_id <> ''
  AND cn.nickname IS NOT NULL
  AND cn.nickname <> ''
  AND EXISTS (SELECT 1 FROM characters WHERE character_id = cn.character_id::INT);

-- Insert character anime works
INSERT INTO character_anime_works (character_id, anime_id, role)
SELECT
  CASE WHEN caw.character_id ~ '^\d+$' THEN caw.character_id::INT ELSE NULL END,
  CASE WHEN caw.anime_id ~ '^\d+$' THEN caw.anime_id::INT ELSE NULL END,
  LEFT(caw.role, 100)
FROM stg_character_anime_works caw
WHERE caw.character_id ~ '^\d+$'
  AND caw.character_id IS NOT NULL
  AND caw.character_id <> ''
  AND caw.anime_id ~ '^\d+$'
  AND caw.anime_id IS NOT NULL
  AND caw.anime_id <> ''
  AND EXISTS (SELECT 1 FROM characters WHERE character_id = caw.character_id::INT)
  AND EXISTS (SELECT 1 FROM anime WHERE anime_id = caw.anime_id::INT);

-- Insert person
INSERT INTO person (person_id, name, given_name, family_name, birthday, website, image_url, favorites)
SELECT
  CASE WHEN p.person_id ~ '^\d+$' THEN p.person_id::INT ELSE NULL END,
  LEFT(p.name, 50),
  LEFT(p.given_name, 50),
  LEFT(p.family_name, 50),
  CASE WHEN LEFT(p.birthday,10) ~ '^\d{4}-\d{2}-\d{2}$' THEN LEFT(p.birthday,10)::DATE ELSE NULL END,
  LEFT(p.website, 50),
  LEFT(p.image_url, 50),
  CASE WHEN p.favorites ~ '^\d+$' THEN p.favorites::INT ELSE NULL END
FROM stg_person p
WHERE p.person_id ~ '^\d+$'
  AND p.person_id IS NOT NULL
  AND p.person_id <> '';

-- Insert person alternate names
INSERT INTO person_alternate_names (person_id, alternate_name)
SELECT
  CASE WHEN pan.person_id ~ '^\d+$' THEN pan.person_id::INT ELSE NULL END,
  LEFT(pan.alternate_name, 50)
FROM stg_person_alternate_names pan
WHERE pan.person_id ~ '^\d+$'
  AND pan.person_id IS NOT NULL
  AND pan.person_id <> ''
  AND pan.alternate_name IS NOT NULL
  AND pan.alternate_name <> ''
  AND EXISTS (SELECT 1 FROM person WHERE person_id = pan.person_id::INT);

-- Insert person details
INSERT INTO person_details (person_id, detail_type, detail_value)
SELECT
  CASE WHEN pd.person_id ~ '^\d+$' THEN pd.person_id::INT ELSE NULL END,
  LEFT(pd.detail_type, 100),
  pd.detail_value
FROM stg_person_details pd
WHERE pd.person_id ~ '^\d+$'
  AND pd.person_id IS NOT NULL
  AND pd.person_id <> ''
  AND pd.detail_type IS NOT NULL
  AND pd.detail_type <> ''
  AND EXISTS (SELECT 1 FROM person WHERE person_id = pd.person_id::INT);

-- Insert person anime works
INSERT INTO person_anime_works (person_id, anime_id, role)
SELECT
  CASE WHEN paw.person_id ~ '^\d+$' THEN paw.person_id::INT ELSE NULL END,
  CASE WHEN paw.anime_id ~ '^\d+$' THEN paw.anime_id::INT ELSE NULL END,
  LEFT(paw.role, 100)
FROM stg_person_anime_works paw
WHERE paw.person_id ~ '^\d+$'
  AND paw.person_id IS NOT NULL
  AND paw.person_id <> ''
  AND paw.anime_id ~ '^\d+$'
  AND paw.anime_id IS NOT NULL
  AND paw.anime_id <> ''
  AND paw.role IS NOT NULL
  AND paw.role <> ''
  AND EXISTS (SELECT 1 FROM person WHERE person_id = paw.person_id::INT)
  AND EXISTS (SELECT 1 FROM anime WHERE anime_id = paw.anime_id::INT);

-- Insert person voice works
INSERT INTO person_voice_works (person_id, character_id, anime_id)
SELECT
  CASE WHEN pvw.person_id ~ '^\d+$' THEN pvw.person_id::INT ELSE NULL END,
  CASE WHEN pvw.character_id ~ '^\d+$' THEN pvw.character_id::INT ELSE NULL END,
  CASE WHEN pvw.anime_id ~ '^\d+$' THEN pvw.anime_id::INT ELSE NULL END
FROM stg_person_voice_works pvw
WHERE pvw.person_id ~ '^\d+$'
  AND pvw.person_id IS NOT NULL
  AND pvw.person_id <> ''
  AND pvw.character_id ~ '^\d+$'
  AND pvw.character_id IS NOT NULL
  AND pvw.character_id <> ''
  AND pvw.anime_id ~ '^\d+$'
  AND pvw.anime_id IS NOT NULL
  AND pvw.anime_id <> ''
  AND EXISTS (SELECT 1 FROM person WHERE person_id = pvw.person_id::INT)
  AND EXISTS (SELECT 1 FROM characters WHERE character_id = pvw.character_id::INT)
  AND EXISTS (SELECT 1 FROM anime WHERE anime_id = pvw.anime_id::INT);

-- Insert recommendations
INSERT INTO recommendations (anime_id, recommended_anime_id, recommendation_count)
SELECT
  CASE WHEN r.anime_id ~ '^\d+$' THEN r.anime_id::INT ELSE NULL END,
  CASE WHEN r.recommended_anime_id ~ '^\d+$' THEN r.recommended_anime_id::INT ELSE NULL END,
  CASE WHEN r.recommendation_count ~ '^\d+$' THEN r.recommendation_count::INT ELSE NULL END
FROM stg_recommendations r
WHERE r.anime_id ~ '^\d+$'
  AND r.anime_id IS NOT NULL
  AND r.anime_id <> ''
  AND r.recommended_anime_id ~ '^\d+$'
  AND r.recommended_anime_id IS NOT NULL
  AND r.recommended_anime_id <> ''
  AND EXISTS (SELECT 1 FROM anime WHERE anime_id = r.anime_id::INT)
  AND EXISTS (SELECT 1 FROM anime WHERE anime_id = r.recommended_anime_id::INT);

-- Insert details
INSERT INTO details (entity_id, entity_type, detail_key, detail_value)
SELECT
  CASE WHEN d.entity_id ~ '^\d+$' THEN d.entity_id::INT ELSE NULL END,
  LEFT(d.entity_type, 50),
  LEFT(d.detail_key, 255),
  d.detail_value
FROM stg_details d
WHERE d.entity_id ~ '^\d+$'
  AND d.entity_id IS NOT NULL
  AND d.entity_id <> ''
  AND d.entity_type IS NOT NULL
  AND d.entity_type <> ''
  AND d.detail_key IS NOT NULL
  AND d.detail_key <> '';

COMMIT;