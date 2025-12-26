\set ON_ERROR_STOP on

BEGIN;

TRUNCATE TABLE
  profiles,
  character_nicknames,
  character_anime_works,
  characters,
  person_alternate_names,
  person_details,
  person_anime_works,
  person_voice_works,
  person,
  details;

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