-- ==================================================
-- Villa Vredestein — seed data
-- ==================================================

BEGIN;

-- ==================================================
-- USERS
-- ==================================================

INSERT INTO users (email, username, password, role, status_toggle)
VALUES
  ('villavredestein@gmail.com', 'Admin', '$2b$08$V8tpGDrFALoLlA38ZYbmPOvi3zB9YsdGJ49xOMsL69ZY.Nla8/Anq', 'ADMIN', true)
ON CONFLICT (email) DO UPDATE SET password = EXCLUDED.password;

INSERT INTO users (email, username, password, role, status_toggle)
VALUES
  ('desmondstaal@gmail.com',  'Desmond', '$2b$08$ie6hcbOAKgx1XBbLjqVz0eLzHy3xp7TO5Q/1gYp5Z4fTIoSZxQZQe', 'STUDENT', true),
  ('medocstaal@gmail.com',    'Medoc',   '$2b$08$ie6hcbOAKgx1XBbLjqVz0eLzHy3xp7TO5Q/1gYp5Z4fTIoSZxQZQe', 'STUDENT', true),
  ('simontalsma2@gmail.com',  'Simon',   '$2b$08$ie6hcbOAKgx1XBbLjqVz0eLzHy3xp7TO5Q/1gYp5Z4fTIoSZxQZQe', 'STUDENT', true),
  ('arwenleonor@gmail.com',   'Arwen',   '$2b$08$ie6hcbOAKgx1XBbLjqVz0eLzHy3xp7TO5Q/1gYp5Z4fTIoSZxQZQe', 'STUDENT', true)
ON CONFLICT (email) DO NOTHING;

UPDATE users SET rent_amount = 350.00 WHERE email = 'desmondstaal@gmail.com';
UPDATE users SET rent_amount = 350.00 WHERE email = 'medocstaal@gmail.com';
UPDATE users SET rent_amount = 550.00 WHERE email = 'simontalsma2@gmail.com';
UPDATE users SET rent_amount = 350.00 WHERE email = 'arwenleonor@gmail.com';

UPDATE users
SET contract_file = 'Woonafspraken 2023-Frankrijk-Medoc.pdf'
WHERE email = 'medocstaal@gmail.com'
  AND (contract_file IS NULL OR contract_file != 'Woonafspraken 2023-Frankrijk-Medoc.pdf');

UPDATE users
SET contract_file = 'Woonafspraken 2023-Italie-Arwen.pdf'
WHERE email = 'arwenleonor@gmail.com'
  AND (contract_file IS NULL OR contract_file != 'Woonafspraken 2023-Italie-Arwen.pdf');

INSERT INTO users (email, username, password, role, status_toggle)
VALUES
  ('cleaner@villavredestein.com', 'Cleaner', '$2b$08$I9JPSEPfvziWzcURaTxxnOabQfREUqEHEaYflGrxWOX7rYO1Bwvty', 'CLEANER', true)
ON CONFLICT (email) DO UPDATE SET password = EXCLUDED.password;

-- ==================================================
-- ROOMS
-- ==================================================

INSERT INTO rooms (name, occupant_id)
VALUES
  ('Japan',       NULL),
  ('Argentinië',  NULL),
  ('Thailand',    NULL),
  ('Frankrijk',   NULL),
  ('Italië',      NULL)
ON CONFLICT (name) DO NOTHING;

UPDATE rooms SET occupant_id = NULL WHERE name IN ('Oekraïne');
DELETE FROM rooms WHERE name IN ('Oekraïne')
  AND occupant_id IS NULL;

UPDATE rooms
SET occupant_id = (SELECT id FROM users WHERE email = 'desmondstaal@gmail.com')
WHERE name = 'Thailand'
  AND (occupant_id IS DISTINCT FROM (SELECT id FROM users WHERE email = 'desmondstaal@gmail.com'));

UPDATE rooms
SET occupant_id = (SELECT id FROM users WHERE email = 'medocstaal@gmail.com')
WHERE name = 'Frankrijk'
  AND (occupant_id IS DISTINCT FROM (SELECT id FROM users WHERE email = 'medocstaal@gmail.com'));

UPDATE rooms
SET occupant_id = (SELECT id FROM users WHERE email = 'simontalsma2@gmail.com')
WHERE name = 'Argentinië'
  AND (occupant_id IS DISTINCT FROM (SELECT id FROM users WHERE email = 'simontalsma2@gmail.com'));

UPDATE rooms
SET occupant_id = (SELECT id FROM users WHERE email = 'arwenleonor@gmail.com')
WHERE name = 'Italië'
  AND (occupant_id IS DISTINCT FROM (SELECT id FROM users WHERE email = 'arwenleonor@gmail.com'));

UPDATE rooms SET occupant_id = NULL WHERE name = 'Japan';

-- ==================================================
-- DOCUMENTS
-- ==================================================

INSERT INTO documents (title, description, storage_path, role_access, uploaded_by_id)
VALUES
  ('Huisregels Villa Vredestein', 'Overview of house rules and code of conduct.', 'uploads/Huisregels.pdf', 'ALL',
   (SELECT id FROM users WHERE email = 'villavredestein@gmail.com')),
  ('Veiligheidsinstructies', 'Fire safety and emergency procedures.', 'uploads/Veiligheid.pdf', 'ALL',
   (SELECT id FROM users WHERE email = 'villavredestein@gmail.com')),
  ('Pensionovereenkomst', 'Rental agreement template.', 'uploads/Pensionovereenkomst.pdf', 'STUDENT',
   (SELECT id FROM users WHERE email = 'villavredestein@gmail.com'))
ON CONFLICT (storage_path) DO NOTHING;

-- ==================================================
-- INVOICES
-- ==================================================

INSERT INTO invoices (title, description, amount, issue_date, due_date, invoice_month, invoice_year, reminder_count, status, student_id)
SELECT
  'Huur huidige maand - Desmond', 'Maandelijkse huur - Desmond', 350.00,
  CURRENT_DATE, CURRENT_DATE + INTERVAL '7 day',
  CAST(EXTRACT(MONTH FROM CURRENT_DATE) AS int), CAST(EXTRACT(YEAR FROM CURRENT_DATE) AS int),
  0, 'OPEN',
  (SELECT id FROM users WHERE email = 'desmondstaal@gmail.com')
WHERE NOT EXISTS (
  SELECT 1 FROM invoices
  WHERE student_id = (SELECT id FROM users WHERE email = 'desmondstaal@gmail.com')
    AND invoice_month = CAST(EXTRACT(MONTH FROM CURRENT_DATE) AS int)
    AND invoice_year  = CAST(EXTRACT(YEAR  FROM CURRENT_DATE) AS int)
);

INSERT INTO invoices (title, description, amount, issue_date, due_date, invoice_month, invoice_year, reminder_count, status, student_id)
SELECT
  'Huur huidige maand - Medoc', 'Maandelijkse huur - Medoc', 350.00,
  CURRENT_DATE, CURRENT_DATE + INTERVAL '7 day',
  CAST(EXTRACT(MONTH FROM CURRENT_DATE) AS int), CAST(EXTRACT(YEAR FROM CURRENT_DATE) AS int),
  0, 'OPEN',
  (SELECT id FROM users WHERE email = 'medocstaal@gmail.com')
WHERE NOT EXISTS (
  SELECT 1 FROM invoices
  WHERE student_id = (SELECT id FROM users WHERE email = 'medocstaal@gmail.com')
    AND invoice_month = CAST(EXTRACT(MONTH FROM CURRENT_DATE) AS int)
    AND invoice_year  = CAST(EXTRACT(YEAR  FROM CURRENT_DATE) AS int)
);

INSERT INTO invoices (title, description, amount, issue_date, due_date, invoice_month, invoice_year, reminder_count, status, student_id)
SELECT
  'Huur huidige maand - Simon', 'Maandelijkse huur - Simon', 550.00,
  CURRENT_DATE, CURRENT_DATE + INTERVAL '7 day',
  CAST(EXTRACT(MONTH FROM CURRENT_DATE) AS int), CAST(EXTRACT(YEAR FROM CURRENT_DATE) AS int),
  0, 'OPEN',
  (SELECT id FROM users WHERE email = 'simontalsma2@gmail.com')
WHERE NOT EXISTS (
  SELECT 1 FROM invoices
  WHERE student_id = (SELECT id FROM users WHERE email = 'simontalsma2@gmail.com')
    AND invoice_month = CAST(EXTRACT(MONTH FROM CURRENT_DATE) AS int)
    AND invoice_year  = CAST(EXTRACT(YEAR  FROM CURRENT_DATE) AS int)
);

INSERT INTO invoices (title, description, amount, issue_date, due_date, invoice_month, invoice_year, reminder_count, status, student_id)
SELECT
  'Huur huidige maand - Arwen', 'Maandelijkse huur - Arwen', 350.00,
  CURRENT_DATE, CURRENT_DATE + INTERVAL '7 day',
  CAST(EXTRACT(MONTH FROM CURRENT_DATE) AS int), CAST(EXTRACT(YEAR FROM CURRENT_DATE) AS int),
  0, 'OPEN',
  (SELECT id FROM users WHERE email = 'arwenleonor@gmail.com')
WHERE NOT EXISTS (
  SELECT 1 FROM invoices
  WHERE student_id = (SELECT id FROM users WHERE email = 'arwenleonor@gmail.com')
    AND invoice_month = CAST(EXTRACT(MONTH FROM CURRENT_DATE) AS int)
    AND invoice_year  = CAST(EXTRACT(YEAR  FROM CURRENT_DATE) AS int)
);

-- ==================================================
-- CLEANING TASKS
-- ==================================================

DELETE FROM cleaning_tasks;

-- ── Week 1 ────────────────────────────────────────
INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access) VALUES
  (1, 'Keuken & vaatwasser', 'Vaatwasser leegmaken, aanrecht, oven en inductieplaat schoonmaken.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'simontalsma2@gmail.com'), 'ALL'),
  (1, 'Badkamer & toilet',   'Wastafel, douche, spiegel en toilet grondig schoonmaken en droogvegen.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'desmondstaal@gmail.com'), 'ALL'),
  (1, 'Vuilnis & was',       'Afval scheiden: gft, plastic/blik, papier, restafval, statiegeld. Was draaien en opvouwen.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'medocstaal@gmail.com'), 'ALL'),
  (1, 'Woonkamer & gang',    'Woonkamer stofzuigen en dweilen. Gang en trap schoonmaken. Eettafel opruimen.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'arwenleonor@gmail.com'), 'ALL');

-- ── Week 2 ────────────────────────────────────────
INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access) VALUES
  (2, 'Keuken & vaatwasser', 'Vaatwasser leegmaken, aanrecht, oven en inductieplaat schoonmaken.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'desmondstaal@gmail.com'), 'ALL'),
  (2, 'Badkamer & toilet',   'Wastafel, douche, spiegel en toilet grondig schoonmaken en droogvegen.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'medocstaal@gmail.com'), 'ALL'),
  (2, 'Vuilnis & was',       'Afval scheiden: gft, plastic/blik, papier, restafval, statiegeld. Was draaien en opvouwen.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'arwenleonor@gmail.com'), 'ALL'),
  (2, 'Woonkamer & gang',    'Woonkamer stofzuigen en dweilen. Gang en trap schoonmaken. Eettafel opruimen.', FALSE, NULL, NULL, NULL, 'ALL');

-- ── Week 3 ────────────────────────────────────────
INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access) VALUES
  (3, 'Keuken & vaatwasser', 'Vaatwasser leegmaken, aanrecht, oven en inductieplaat schoonmaken.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'medocstaal@gmail.com'), 'ALL'),
  (3, 'Badkamer & toilet',   'Wastafel, douche, spiegel en toilet grondig schoonmaken en droogvegen.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'arwenleonor@gmail.com'), 'ALL'),
  (3, 'Vuilnis & was',       'Afval scheiden: gft, plastic/blik, papier, restafval, statiegeld. Was draaien en opvouwen.', FALSE, NULL, NULL, NULL, 'ALL'),
  (3, 'Woonkamer & gang',    'Woonkamer stofzuigen en dweilen. Gang en trap schoonmaken. Eettafel opruimen.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'simontalsma2@gmail.com'), 'ALL');

-- ── Week 4 ────────────────────────────────────────
INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access) VALUES
  (4, 'Keuken & vaatwasser', 'Vaatwasser leegmaken, aanrecht, oven en inductieplaat schoonmaken.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'arwenleonor@gmail.com'), 'ALL'),
  (4, 'Badkamer & toilet',   'Wastafel, douche, spiegel en toilet grondig schoonmaken en droogvegen.', FALSE, NULL, NULL, NULL, 'ALL'),
  (4, 'Vuilnis & was',       'Afval scheiden: gft, plastic/blik, papier, restafval, statiegeld. Was draaien en opvouwen.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'simontalsma2@gmail.com'), 'ALL'),
  (4, 'Woonkamer & gang',    'Woonkamer stofzuigen en dweilen. Gang en trap schoonmaken. Eettafel opruimen.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'desmondstaal@gmail.com'), 'ALL');

-- ── Week 5 ────────────────────────────────────────
INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access) VALUES
  (5, 'Keuken & vaatwasser', 'Vaatwasser leegmaken, aanrecht, oven en inductieplaat schoonmaken.', FALSE, NULL, NULL, NULL, 'ALL'),
  (5, 'Badkamer & toilet',   'Wastafel, douche, spiegel en toilet grondig schoonmaken en droogvegen.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'simontalsma2@gmail.com'), 'ALL'),
  (5, 'Vuilnis & was',       'Afval scheiden: gft, plastic/blik, papier, restafval, statiegeld. Was draaien en opvouwen.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'desmondstaal@gmail.com'), 'ALL'),
  (5, 'Woonkamer & gang',    'Woonkamer stofzuigen en dweilen. Gang en trap schoonmaken. Eettafel opruimen.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'medocstaal@gmail.com'), 'ALL');

-- ==================================================
-- VIEW
-- ==================================================

CREATE OR REPLACE VIEW view_users AS
SELECT
  u.id,
  u.username AS name,
  u.email,
  u.role,
  r.name AS room_name
FROM users u
LEFT JOIN rooms r ON r.occupant_id = u.id
ORDER BY u.role, u.username;

COMMIT;