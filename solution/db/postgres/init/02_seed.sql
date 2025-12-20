\set ON_ERROR_STOP on

BEGIN;

TRUNCATE TABLE
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

---------------------------------------------------
-- LOAD CSV (server-side: i CSV sono montati nel container in /import)
---------------------------------------------------
-- Import data from CSV files
COPY stg_profiles(username, gender, birthday, location, joined, watching, completed, on_hold, dropped, plan_to_watch)
FROM '/import/profiles.csv'
DELIMITER ','
CSV HEADER;

---------------------------------------------------
-- INSERTS
---------------------------------------------------

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

COMMIT;