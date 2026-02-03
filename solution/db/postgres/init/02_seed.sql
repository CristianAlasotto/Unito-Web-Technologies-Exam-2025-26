\set ON_ERROR_STOP on

BEGIN;

TRUNCATE TABLE
    character_nicknames,
  character_anime_works,
  person_alternate_names,
  person_anime_works,
  person_voice_works,
  recommendations,
  characters,
  person_details,
  details,
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

CREATE TEMP TABLE stg_details (
  mal_id TEXT,
  title TEXT,
  title_japanese TEXT,
  url TEXT,
  image_url TEXT,
  type TEXT,
  status TEXT,
  score TEXT,
  scored_by TEXT,
  start_date TEXT,
  end_date TEXT,
  synopsis TEXT,
  rank TEXT,
  popularity TEXT,
  members TEXT,
  favorites TEXT,
  genres TEXT,
  studios TEXT,
  themes TEXT,
  demographics TEXT,
  source TEXT,
  rating TEXT,
  episodes TEXT,
  season TEXT,
  year TEXT,
  producers TEXT,
  explicit_genres TEXT,
  licensors TEXT,
  streaming TEXT
);

CREATE TEMP TABLE stg_characters_raw (
  raw_line TEXT
);

CREATE TEMP TABLE stg_characters (
  character_mal_id TEXT,
  url TEXT DEFAULT NULL,
  name TEXT DEFAULT NULL,
  name_kanji TEXT DEFAULT NULL,
  image TEXT DEFAULT NULL,
  favorites TEXT DEFAULT NULL,
  about TEXT DEFAULT NULL
);

CREATE TEMP TABLE stg_character_nicknames_raw (
  raw_line TEXT
);

CREATE TEMP TABLE stg_character_nicknames (
  character_mal_id TEXT,
  nickname TEXT
);

CREATE TEMP TABLE stg_character_anime_works_raw (
  raw_line TEXT
);

CREATE TEMP TABLE stg_character_anime_works (
  anime_mal_id TEXT,
  character_mal_id TEXT,
  character_name TEXT,
  role TEXT
);

CREATE TEMP TABLE stg_person_details_raw (
  raw_line TEXT
);

CREATE TEMP TABLE stg_person_details (
  person_mal_id TEXT,
  url TEXT,
  website_url TEXT,
  image_url TEXT,
  name TEXT,
  given_name TEXT,
  family_name TEXT,
  birthday TEXT,
  favorites TEXT,
  relevant_location TEXT
);

CREATE TEMP TABLE stg_person_alternate_names_raw (
  raw_line TEXT
);

CREATE TEMP TABLE stg_person_alternate_names (
  person_mal_id TEXT,
  alt_name TEXT
);

CREATE TEMP TABLE stg_person_anime_works_raw (
  raw_line TEXT
);

CREATE TEMP TABLE stg_person_anime_works (
  person_mal_id TEXT,
  position TEXT,
  anime_mal_id TEXT
);

CREATE TEMP TABLE stg_person_voice_works_raw (
  raw_line TEXT
);

CREATE TEMP TABLE stg_person_voice_works (
  person_mal_id TEXT,
  role TEXT,
  anime_mal_id TEXT,
  character_mal_id TEXT,
  language TEXT
);

CREATE TEMP TABLE stg_recommendations_raw (
  raw_line TEXT
);

CREATE TEMP TABLE stg_recommendations (
  mal_id TEXT,
  recommendation_mal_id TEXT
);

---------------------------------------------------
-- LOAD CSV (server-side: i CSV sono montati nel container in /import)
---------------------------------------------------

-- Import profiles
COPY stg_profiles(username, gender, birthday, location, joined, watching, completed, on_hold, dropped, plan_to_watch)
    FROM '/import/profiles.csv'
    DELIMITER ','
    CSV HEADER;

-- Import details (anime)
COPY stg_details(mal_id, title, title_japanese, url, image_url, type, status, score, scored_by, start_date, end_date, synopsis, rank, popularity, members, favorites, genres, studios, themes, demographics, source, rating, episodes, season, year, producers, explicit_genres, licensors, streaming)
    FROM '/import/details.csv'
    DELIMITER ','
    CSV HEADER;

-- Import characters (using sed to remove carriage returns)
COPY stg_characters_raw(raw_line)
    FROM PROGRAM 'sed ''s/\r//g'' /import/characters.csv'
    DELIMITER E'\x01';

-- Remove the header row
DELETE FROM stg_characters_raw WHERE raw_line LIKE 'character_mal_id%';

-- Parse the raw lines into proper columns using split_part
INSERT INTO stg_characters (character_mal_id, url, name, name_kanji, image, favorites, about)
SELECT
    NULLIF(TRIM(split_part(raw_line, ',', 1)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 2)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 3)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 4)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 5)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 6)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 7)), '')
FROM stg_characters_raw
WHERE raw_line IS NOT NULL AND raw_line <> '';

-- Import character nicknames (using sed to remove carriage returns)
COPY stg_character_nicknames_raw(raw_line)
    FROM PROGRAM 'sed ''s/\r//g'' /import/character_nicknames.csv'
    DELIMITER E'\x01';

-- Remove the header row
DELETE FROM stg_character_nicknames_raw WHERE raw_line LIKE 'character_mal_id%';

-- Parse the raw lines into proper columns
INSERT INTO stg_character_nicknames (character_mal_id, nickname)
SELECT
    NULLIF(TRIM(split_part(raw_line, ',', 1)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 2)), '')
FROM stg_character_nicknames_raw
WHERE raw_line IS NOT NULL AND raw_line <> '';

-- Import character anime works (using sed to remove carriage returns)
COPY stg_character_anime_works_raw(raw_line)
    FROM PROGRAM 'sed ''s/\r//g'' /import/character_anime_works.csv'
    DELIMITER E'\x01';

DELETE FROM stg_character_anime_works_raw WHERE raw_line LIKE 'anime_mal_id%';

INSERT INTO stg_character_anime_works (anime_mal_id, character_mal_id, character_name, role)
SELECT
    NULLIF(TRIM(split_part(raw_line, ',', 1)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 2)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 3)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 4)), '')
FROM stg_character_anime_works_raw
WHERE raw_line IS NOT NULL AND raw_line <> '';

-- Import person details (using sed to remove carriage returns)
COPY stg_person_details_raw(raw_line)
    FROM PROGRAM 'sed ''s/\r//g'' /import/person_details.csv'
    DELIMITER E'\x01';

DELETE FROM stg_person_details_raw WHERE raw_line LIKE 'person_mal_id%';

INSERT INTO stg_person_details (person_mal_id, url, website_url, image_url, name, given_name, family_name, birthday, favorites, relevant_location)
SELECT
    NULLIF(TRIM(split_part(raw_line, ',', 1)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 2)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 3)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 4)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 5)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 6)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 7)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 8)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 9)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 10)), '')
FROM stg_person_details_raw
WHERE raw_line IS NOT NULL AND raw_line <> '';

-- Import person alternate names (using sed to remove carriage returns)
COPY stg_person_alternate_names_raw(raw_line)
    FROM PROGRAM 'sed ''s/\r//g'' /import/person_alternate_names.csv'
    DELIMITER E'\x01';

DELETE FROM stg_person_alternate_names_raw WHERE raw_line LIKE 'person_mal_id%';

INSERT INTO stg_person_alternate_names (person_mal_id, alt_name)
SELECT
    NULLIF(TRIM(split_part(raw_line, ',', 1)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 2)), '')
FROM stg_person_alternate_names_raw
WHERE raw_line IS NOT NULL AND raw_line <> '';

-- Import person anime works (using sed to remove carriage returns)
COPY stg_person_anime_works_raw(raw_line)
    FROM PROGRAM 'sed ''s/\r//g'' /import/person_anime_works.csv'
    DELIMITER E'\x01';

DELETE FROM stg_person_anime_works_raw WHERE raw_line LIKE 'person_mal_id%';

INSERT INTO stg_person_anime_works (person_mal_id, position, anime_mal_id)
SELECT
    NULLIF(TRIM(split_part(raw_line, ',', 1)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 2)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 3)), '')
FROM stg_person_anime_works_raw
WHERE raw_line IS NOT NULL AND raw_line <> '';

-- Import person voice works (using sed to remove carriage returns)
COPY stg_person_voice_works_raw(raw_line)
    FROM PROGRAM 'sed ''s/\r//g'' /import/person_voice_works.csv'
    DELIMITER E'\x01';

DELETE FROM stg_person_voice_works_raw WHERE raw_line LIKE 'person_mal_id%';

INSERT INTO stg_person_voice_works (person_mal_id, role, anime_mal_id, character_mal_id, language)
SELECT
    NULLIF(TRIM(split_part(raw_line, ',', 1)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 2)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 3)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 4)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 5)), '')
FROM stg_person_voice_works_raw
WHERE raw_line IS NOT NULL AND raw_line <> '';

-- Import recommendations (using sed to remove carriage returns)
COPY stg_recommendations_raw(raw_line)
    FROM PROGRAM 'sed ''s/\r//g'' /import/recommendations.csv'
    DELIMITER E'\x01';

DELETE FROM stg_recommendations_raw WHERE raw_line LIKE 'mal_id,recommendation_mal_id%' OR raw_line LIKE 'mal_id%recommendation%';

INSERT INTO stg_recommendations (mal_id, recommendation_mal_id)
SELECT
    NULLIF(TRIM(split_part(raw_line, ',', 1)), ''),
    NULLIF(TRIM(split_part(raw_line, ',', 2)), '')
FROM stg_recommendations_raw
WHERE raw_line IS NOT NULL AND raw_line <> '';

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

-- Insert details (anime)
INSERT INTO details (mal_id, title, title_japanese, url, image_url, type, status, score, scored_by, start_date, end_date, synopsis, rank, popularity, members, favorites, genres, studios, themes, demographics, source, rating, episodes, season, year, producers, explicit_genres, licensors, streaming)
SELECT
    CASE WHEN d.mal_id ~ '^\d+$' THEN d.mal_id::INT ELSE NULL END,
  LEFT(d.title, 600),
  LEFT(d.title_japanese, 550),
  LEFT(d.url, 500),
  LEFT(d.image_url, 450),
  LEFT(d.type, 400),
  LEFT(d.status, 450),
  CASE WHEN d.score ~ '^\d+\.?\d*$' THEN d.score::DECIMAL(4,2) ELSE NULL END,
  CASE WHEN d.scored_by ~ '^\d+\.?\d*$' THEN d.scored_by::NUMERIC::INT ELSE NULL END,
  CASE WHEN LEFT(d.start_date,10) ~ '^\d{4}-\d{2}-\d{2}$' THEN LEFT(d.start_date,10)::DATE ELSE NULL END,
  CASE WHEN LEFT(d.end_date,10) ~ '^\d{4}-\d{2}-\d{2}$' THEN LEFT(d.end_date,10)::DATE ELSE NULL END,
  d.synopsis,
  CASE WHEN d.rank ~ '^\d+\.?\d*$' THEN d.rank::NUMERIC::INT ELSE NULL END,
  CASE WHEN d.popularity ~ '^\d+$' THEN d.popularity::INT ELSE NULL END,
  CASE WHEN d.members ~ '^\d+$' THEN d.members::INT ELSE NULL END,
  CASE WHEN d.favorites ~ '^\d+$' THEN d.favorites::INT ELSE NULL END,
  LEFT(d.genres, 550),
  LEFT(d.studios, 500),
  LEFT(d.themes, 500),
  LEFT(d.demographics, 400),
  d.source,
  LEFT(d.rating, 420),
  CASE WHEN d.episodes ~ '^\d+\.?\d*$' AND d.episodes::NUMERIC < 100000 THEN d.episodes::DECIMAL(6,1) ELSE NULL END,
  d.season,
  CASE WHEN d.year ~ '^\d+\.?\d*$' AND d.year::NUMERIC < 100000 THEN d.year::DECIMAL(6,1) ELSE NULL END,
  LEFT(d.producers, 600),
  LEFT(d.explicit_genres, 200),
  LEFT(d.licensors, 250),
  LEFT(d.streaming, 200)
FROM stg_details d
WHERE d.mal_id ~ '^\d+$'
  AND d.mal_id IS NOT NULL
  AND d.mal_id <> '';

-- Insert characters (drop duplicates, keep row with highest favorites)
INSERT INTO characters (character_mal_id, url, name, name_kanji, image, favorites, about)
SELECT DISTINCT ON (character_mal_id)
    CASE WHEN c.character_mal_id ~ '^\d+$' THEN c.character_mal_id::INT ELSE NULL END,
  LEFT(c.url, 200),
  LEFT(c.name, 150),
  LEFT(c.name_kanji, 150),
  LEFT(c.image, 200),
  CASE WHEN c.favorites ~ '^\d+$' THEN c.favorites::INT ELSE NULL END,
  c.about
FROM stg_characters c
WHERE c.character_mal_id ~ '^\d+$'
  AND c.character_mal_id IS NOT NULL
  AND c.character_mal_id <> ''
ORDER BY character_mal_id, c.favorites DESC NULLS LAST;

-- Insert character nicknames (using character_mal_id from CSV directly)
INSERT INTO character_nicknames (character_mal_id, nickname)
SELECT DISTINCT
    CASE WHEN cn.character_mal_id ~ '^\d+$' THEN cn.character_mal_id::INT ELSE NULL END,
  LEFT(cn.nickname, 100)
FROM stg_character_nicknames cn
WHERE cn.character_mal_id ~ '^\d+$'
  AND cn.character_mal_id IS NOT NULL
  AND cn.character_mal_id <> ''
  AND cn.nickname IS NOT NULL
  AND cn.nickname <> ''
  AND EXISTS (SELECT 1 FROM characters WHERE character_mal_id = cn.character_mal_id::INT)
ON CONFLICT (character_mal_id, nickname) DO NOTHING;

-- Insert character anime works (using character_mal_id from CSV directly)
INSERT INTO character_anime_works (anime_mal_id, character_mal_id, character_name, role)
SELECT DISTINCT
    CASE WHEN caw.anime_mal_id ~ '^\d+$' THEN caw.anime_mal_id::INT ELSE NULL END,
  CASE WHEN caw.character_mal_id ~ '^\d+$' THEN caw.character_mal_id::INT ELSE NULL END,
  LEFT(caw.character_name, 100),
  LEFT(caw.role, 100)
FROM stg_character_anime_works caw
WHERE caw.character_mal_id ~ '^\d+$'
  AND caw.character_mal_id IS NOT NULL
  AND caw.character_mal_id <> ''
  AND caw.anime_mal_id ~ '^\d+$'
  AND caw.anime_mal_id IS NOT NULL
  AND caw.anime_mal_id <> ''
  AND EXISTS (SELECT 1 FROM characters WHERE character_mal_id = caw.character_mal_id::INT)
  AND EXISTS (SELECT 1 FROM details WHERE mal_id = caw.anime_mal_id::INT)
ON CONFLICT (character_mal_id, anime_mal_id) DO NOTHING;

-- Insert person details (drop duplicates, keep row with highest favorites)
INSERT INTO person_details (person_mal_id, url, website_url, image_url, name, given_name, family_name, birthday, favorites, relevant_location)
SELECT DISTINCT ON (person_mal_id)
    CASE WHEN pd.person_mal_id ~ '^\d+$' THEN pd.person_mal_id::INT ELSE NULL END,
  LEFT(pd.url, 100),
  LEFT(pd.website_url, 300),
  LEFT(pd.image_url, 100),
  LEFT(pd.name, 100),
  LEFT(pd.given_name, 100),
  LEFT(pd.family_name, 100),
  CASE WHEN LEFT(pd.birthday,10) ~ '^\d{4}-\d{2}-\d{2}$' THEN LEFT(pd.birthday,10)::DATE ELSE NULL END,
  CASE WHEN pd.favorites ~ '^\d+$' THEN pd.favorites::INT ELSE NULL END,
  LEFT(pd.relevant_location, 100)
FROM stg_person_details pd
WHERE pd.person_mal_id ~ '^\d+$'
  AND pd.person_mal_id IS NOT NULL
  AND pd.person_mal_id <> ''
ORDER BY person_mal_id, pd.favorites DESC NULLS LAST;

-- Insert person alternate names (using person_mal_id from CSV directly)
INSERT INTO person_alternate_names (person_mal_id, alt_name)
SELECT DISTINCT
    CASE WHEN pan.person_mal_id ~ '^\d+$' THEN pan.person_mal_id::INT ELSE NULL END,
  LEFT(pan.alt_name, 100)
FROM stg_person_alternate_names pan
WHERE pan.person_mal_id ~ '^\d+$'
  AND pan.person_mal_id IS NOT NULL
  AND pan.person_mal_id <> ''
  AND pan.alt_name IS NOT NULL
  AND pan.alt_name <> ''
  AND EXISTS (SELECT 1 FROM person_details WHERE person_mal_id = pan.person_mal_id::INT)
ON CONFLICT (person_mal_id, alt_name) DO NOTHING;

-- Insert person anime works (using person_mal_id from CSV directly)
INSERT INTO person_anime_works (person_mal_id, position, anime_mal_id)
SELECT DISTINCT
    CASE WHEN paw.person_mal_id ~ '^\d+$' THEN paw.person_mal_id::INT ELSE NULL END,
  LEFT(paw.position, 150),
  CASE WHEN paw.anime_mal_id ~ '^\d+$' THEN paw.anime_mal_id::INT ELSE NULL END
FROM stg_person_anime_works paw
WHERE paw.person_mal_id ~ '^\d+$'
  AND paw.person_mal_id IS NOT NULL
  AND paw.person_mal_id <> ''
  AND paw.anime_mal_id ~ '^\d+$'
  AND paw.anime_mal_id IS NOT NULL
  AND paw.anime_mal_id <> ''
  AND paw.position IS NOT NULL
  AND paw.position <> ''
  AND EXISTS (SELECT 1 FROM person_details WHERE person_mal_id = paw.person_mal_id::INT)
  AND EXISTS (SELECT 1 FROM details WHERE mal_id = paw.anime_mal_id::INT)
ON CONFLICT (person_mal_id, position, anime_mal_id) DO NOTHING;

-- Insert person voice works (using mal_id columns from CSV directly)
INSERT INTO person_voice_works (person_mal_id, role, anime_mal_id, character_mal_id, language)
SELECT DISTINCT
    CASE WHEN pvw.person_mal_id ~ '^\d+$' THEN pvw.person_mal_id::INT ELSE NULL END,
  pvw.role,
  CASE WHEN pvw.anime_mal_id ~ '^\d+$' THEN pvw.anime_mal_id::INT ELSE NULL END,
  CASE WHEN pvw.character_mal_id ~ '^\d+$' THEN pvw.character_mal_id::INT ELSE NULL END,
  pvw.language
FROM stg_person_voice_works pvw
WHERE pvw.person_mal_id ~ '^\d+$'
  AND pvw.person_mal_id IS NOT NULL
  AND pvw.person_mal_id <> ''
  AND pvw.character_mal_id ~ '^\d+$'
  AND pvw.character_mal_id IS NOT NULL
  AND pvw.character_mal_id <> ''
  AND pvw.anime_mal_id ~ '^\d+$'
  AND pvw.anime_mal_id IS NOT NULL
  AND pvw.anime_mal_id <> ''
  AND pvw.role IS NOT NULL
  AND pvw.role <> ''
  AND EXISTS (SELECT 1 FROM person_details WHERE person_mal_id = pvw.person_mal_id::INT)
  AND EXISTS (SELECT 1 FROM characters WHERE character_mal_id = pvw.character_mal_id::INT)
  AND EXISTS (SELECT 1 FROM details WHERE mal_id = pvw.anime_mal_id::INT)
ON CONFLICT (person_mal_id, character_mal_id, anime_mal_id) DO NOTHING;

-- Insert recommendations
INSERT INTO recommendations (mal_id, recommendation_mal_id)
SELECT
    CASE WHEN r.mal_id ~ '^\d+$' THEN r.mal_id::INT ELSE NULL END,
  CASE WHEN r.recommendation_mal_id ~ '^\d+$' THEN r.recommendation_mal_id::INT ELSE NULL END
FROM stg_recommendations r
WHERE r.mal_id ~ '^\d+$'
  AND r.mal_id IS NOT NULL
  AND r.mal_id <> ''
  AND r.recommendation_mal_id ~ '^\d+$'
  AND r.recommendation_mal_id IS NOT NULL
  AND r.recommendation_mal_id <> ''
  AND EXISTS (SELECT 1 FROM details WHERE mal_id = r.mal_id::INT)
  AND EXISTS (SELECT 1 FROM details WHERE mal_id = r.recommendation_mal_id::INT)
ON CONFLICT (mal_id, recommendation_mal_id) DO NOTHING;

COMMIT;

---------------------------------------------------
-- TEST QUERIES (20 queries to test all tables and keys)
---------------------------------------------------

-- Query 1: Test profiles table - count all users
SELECT 'Query 1: Total users' AS test_name, COUNT(*) AS result FROM profiles;

-- Query 2: Test details table - count all anime
SELECT 'Query 2: Total anime' AS test_name, COUNT(*) AS result FROM details;

-- Query 3: Test characters table - count all characters
SELECT 'Query 3: Total characters' AS test_name, COUNT(*) AS result FROM characters;

-- Query 4: Test character_nicknames table
SELECT 'Query 4: Characters with nicknames' AS test_name, COUNT(DISTINCT character_mal_id) AS result
FROM character_nicknames;

-- Query 5: Test character_anime_works table and FK to both characters and details
SELECT 'Query 5: Character-Anime relationships' AS test_name, COUNT(*) AS result
FROM character_anime_works caw
WHERE EXISTS (SELECT 1 FROM characters WHERE character_mal_id = caw.character_mal_id)
  AND EXISTS (SELECT 1 FROM details WHERE mal_id = caw.anime_mal_id);

-- Query 6: Test person_details table - count all persons
SELECT 'Query 6: Total persons' AS test_name, COUNT(*) AS result FROM person_details;

-- Query 7: Test person_alternate_names table
SELECT 'Query 7: Persons with alternate names' AS test_name, COUNT(DISTINCT person_mal_id) AS result
FROM person_alternate_names;

-- Query 8: Test person_anime_works table and FK to person_details and details
SELECT 'Query 8: Person-Anime work relationships' AS test_name, COUNT(*) AS result
FROM person_anime_works paw
WHERE EXISTS (SELECT 1 FROM person_details WHERE person_mal_id = paw.person_mal_id)
  AND EXISTS (SELECT 1 FROM details WHERE mal_id = paw.anime_mal_id);

-- Query 9: Test person_voice_works table and all 3 FKs
SELECT 'Query 9: Voice acting relationships' AS test_name, COUNT(*) AS result
FROM person_voice_works pvw
WHERE EXISTS (SELECT 1 FROM person_details WHERE person_mal_id = pvw.person_mal_id)
  AND EXISTS (SELECT 1 FROM characters WHERE character_mal_id = pvw.character_mal_id)
  AND EXISTS (SELECT 1 FROM details WHERE mal_id = pvw.anime_mal_id);

-- Query 10: Test recommendations table and FK to details (both columns)
SELECT 'Query 10: Valid recommendations' AS test_name, COUNT(*) AS result
FROM recommendations r
INNER JOIN details d1 ON r.mal_id = d1.mal_id
INNER JOIN details d2 ON r.recommendation_mal_id = d2.mal_id;

-- Query 11: Test PRIMARY KEY on profiles
SELECT 'Query 11: Duplicate usernames check' AS test_name,
       CASE WHEN COUNT(*) = COUNT(DISTINCT username) THEN 'PASS' ELSE 'FAIL' END AS result
FROM profiles;

-- Query 12: Test PRIMARY KEY on details
SELECT 'Query 12: Duplicate mal_id check' AS test_name,
       CASE WHEN COUNT(*) = COUNT(DISTINCT mal_id) THEN 'PASS' ELSE 'FAIL' END AS result
FROM details;

-- Query 13: Test PRIMARY KEY on characters (character_mal_id)
SELECT 'Query 13: Duplicate character_mal_id check' AS test_name,
       CASE WHEN COUNT(*) = COUNT(DISTINCT character_mal_id) THEN 'PASS' ELSE 'FAIL' END AS result
FROM characters;

-- Query 14: Test PRIMARY KEY on person_details (person_mal_id)
SELECT 'Query 14: Duplicate person_mal_id check' AS test_name,
       CASE WHEN COUNT(*) = COUNT(DISTINCT person_mal_id) THEN 'PASS' ELSE 'FAIL' END AS result
FROM person_details;

-- Query 15: Test composite PRIMARY KEY on character_nicknames
SELECT 'Query 15: Duplicate character nickname pairs check' AS test_name,
       CASE WHEN COUNT(*) = COUNT(DISTINCT (character_mal_id, nickname)) THEN 'PASS' ELSE 'FAIL' END AS result
FROM character_nicknames;

-- Query 16: Test composite PRIMARY KEY on character_anime_works
SELECT 'Query 16: Duplicate character-anime pairs check' AS test_name,
       CASE WHEN COUNT(*) = COUNT(DISTINCT (character_mal_id, anime_mal_id)) THEN 'PASS' ELSE 'FAIL' END AS result
FROM character_anime_works;

-- Query 17: Test composite PRIMARY KEY on person_anime_works
SELECT 'Query 17: Duplicate person-position-anime triples check' AS test_name,
       CASE WHEN COUNT(*) = COUNT(DISTINCT (person_mal_id, position, anime_mal_id)) THEN 'PASS' ELSE 'FAIL' END AS result
FROM person_anime_works;

-- Query 18: Test top 5 anime by score
SELECT 'Query 18: Top 5 anime by score' AS test_name, title, score
FROM details
WHERE score IS NOT NULL
ORDER BY score DESC
LIMIT 5;

-- Query 19: Test orphaned foreign keys in character_anime_works (should return 0)
SELECT 'Query 19: Orphaned character_anime_works check' AS test_name, COUNT(*) AS result
FROM character_anime_works caw
WHERE NOT EXISTS (SELECT 1 FROM characters WHERE character_mal_id = caw.character_mal_id)
   OR NOT EXISTS (SELECT 1 FROM details WHERE mal_id = caw.anime_mal_id);

-- Query 20: Test complex join - anime with characters and voice actors
SELECT 'Query 20: Anime with full cast info' AS test_name, COUNT(DISTINCT d.mal_id) AS result
FROM details d
INNER JOIN character_anime_works caw ON d.mal_id = caw.anime_mal_id
INNER JOIN characters c ON caw.character_mal_id = c.character_mal_id
INNER JOIN person_voice_works pvw ON c.character_mal_id = pvw.character_mal_id AND d.mal_id = pvw.anime_mal_id
INNER JOIN person_details pd ON pvw.person_mal_id = pd.person_mal_id;

-- ========================================
-- ADDITIONAL STABILITY AND INTEGRITY TESTS
-- ========================================

-- Query 21: Test data completeness - characters without any anime works
SELECT 'Query 21: Orphaned characters (no anime works)' AS test_name, COUNT(*) AS result
FROM characters c
WHERE NOT EXISTS (SELECT 1 FROM character_anime_works WHERE character_mal_id = c.character_mal_id);

-- Query 22: Test data completeness - persons without any works
SELECT 'Query 22: Orphaned persons (no anime or voice works)' AS test_name, COUNT(*) AS result
FROM person_details pd
WHERE NOT EXISTS (SELECT 1 FROM person_anime_works WHERE person_mal_id = pd.person_mal_id)
  AND NOT EXISTS (SELECT 1 FROM person_voice_works WHERE person_mal_id = pd.person_mal_id);

-- Query 23: Test referential integrity - voice works with missing character
SELECT 'Query 23: Broken FK in person_voice_works (character)' AS test_name, COUNT(*) AS result
FROM person_voice_works pvw
WHERE NOT EXISTS (SELECT 1 FROM characters WHERE character_mal_id = pvw.character_mal_id);

-- Query 24: Test referential integrity - voice works with missing person
SELECT 'Query 24: Broken FK in person_voice_works (person)' AS test_name, COUNT(*) AS result
FROM person_voice_works pvw
WHERE NOT EXISTS (SELECT 1 FROM person_details WHERE person_mal_id = pvw.person_mal_id);

-- Query 25: Test referential integrity - voice works with missing anime
SELECT 'Query 25: Broken FK in person_voice_works (anime)' AS test_name, COUNT(*) AS result
FROM person_voice_works pvw
WHERE NOT EXISTS (SELECT 1 FROM details WHERE mal_id = pvw.anime_mal_id);

-- Query 26: Test data quality - anime with NULL or invalid scores
SELECT 'Query 26: Anime with invalid scores' AS test_name, COUNT(*) AS result
FROM details
WHERE score IS NOT NULL AND (score < 0 OR score > 10);

-- Query 27: Test data quality - anime with future dates
SELECT 'Query 27: Anime with future start dates' AS test_name, COUNT(*) AS result
FROM details
WHERE start_date > CURRENT_DATE;

-- Query 28: Test data distribution - most prolific voice actors (top 5)
SELECT 'Query 28: Top 5 voice actors by work count' AS test_name, pd.name, COUNT(*) as work_count
FROM person_voice_works pvw
INNER JOIN person_details pd ON pvw.person_mal_id = pd.person_mal_id
GROUP BY pd.person_mal_id, pd.name
ORDER BY work_count DESC
LIMIT 5;

-- Query 29: Test data distribution - anime with most characters (top 5)
SELECT 'Query 29: Top 5 anime by character count' AS test_name, d.title, COUNT(DISTINCT caw.character_mal_id) as character_count
FROM details d
INNER JOIN character_anime_works caw ON d.mal_id = caw.anime_mal_id
GROUP BY d.mal_id, d.title
ORDER BY character_count DESC
LIMIT 5;

-- Query 30: Test cross-table consistency - total relationships count
SELECT 'Query 30: Total relationship records across all tables' AS test_name,
       (SELECT COUNT(*) FROM character_nicknames) +
       (SELECT COUNT(*) FROM character_anime_works) +
       (SELECT COUNT(*) FROM person_alternate_names) +
       (SELECT COUNT(*) FROM person_anime_works) +
       (SELECT COUNT(*) FROM person_voice_works) +
       (SELECT COUNT(*) FROM recommendations) AS total_relationships;