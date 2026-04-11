-- ==================================================
-- Villa Vredestein
-- ==================================================

BEGIN;

-- ==================================================
-- USERS
-- ==================================================

-- Admin (Vredestein1906!)
INSERT INTO users (email, username, password, role, status_toggle)
VALUES
  ('villavredestein@gmail.com', 'Admin', '$2a$10$C6cl7EPzdrsj2xIZ7ECJbeZzAUVqAICC6qG0uQ.GwSnQjqlbnmiGm', 'ADMIN', true)
ON CONFLICT (email) DO NOTHING;

-- Students (Student1234!)
INSERT INTO users (email, username, password, role, status_toggle)
VALUES
  ('arwenleonor@gmail.com',   'Arwen',   '$2a$10$8jkpw3jcNo8VPa/OTIj/W.E0Z9sDghGL1hwupThQuQTFpWYhN2uMm', 'STUDENT', true),
  ('ikheetalvar@gmail.com',   'Alvar',   '$2a$10$8jkpw3jcNo8VPa/OTIj/W.E0Z9sDghGL1hwupThQuQTFpWYhN2uMm', 'STUDENT', true),
  ('desmondstaal@gmail.com',  'Desmond', '$2a$10$8jkpw3jcNo8VPa/OTIj/W.E0Z9sDghGL1hwupThQuQTFpWYhN2uMm', 'STUDENT', true),
  ('medocstaal@gmail.com',    'Medoc',   '$2a$10$8jkpw3jcNo8VPa/OTIj/W.E0Z9sDghGL1hwupThQuQTFpWYhN2uMm', 'STUDENT', true),
  ('simontalsma2@gmail.com',  'Simon',   '$2a$10$8jkpw3jcNo8VPa/OTIj/W.E0Z9sDghGL1hwupThQuQTFpWYhN2uMm', 'STUDENT', true)
ON CONFLICT (email) DO NOTHING;

-- Set Medoc's contract file
UPDATE users
SET contract_file = 'Woonafspraken 2023-Frankrijk-Medoc.pdf'
WHERE email = 'medocstaal@gmail.com'
  AND (contract_file IS NULL OR contract_file != 'Woonafspraken 2023-Frankrijk-Medoc.pdf');

-- Cleaner (Cleaner1234!)
INSERT INTO users (email, username, password, role, status_toggle)
VALUES
  ('cleaner@villavredestein.com', 'Cleaner', '$2a$10$lsHY1ORj580Kn9Dt3sbWF.yu/iNfsbOvsynzxZKy0sb9osdcpFQqS', 'CLEANER', true)
ON CONFLICT (email) DO NOTHING;

-- ==================================================
-- ROOMS
-- ==================================================

INSERT INTO rooms (name, occupant_id)
VALUES
  ('Japan',       NULL),
  ('Argentinië',  NULL),
  ('Thailand',    NULL),
  ('Italië',      NULL),
  ('Frankrijk',   NULL),
  ('Oekraïne',    NULL)
ON CONFLICT (name) DO NOTHING;

-- Italië → Arwen
UPDATE rooms
SET occupant_id = (SELECT id FROM users WHERE email = 'arwenleonor@gmail.com')
WHERE name = 'Italië'
  AND (occupant_id IS DISTINCT FROM (SELECT id FROM users WHERE email = 'arwenleonor@gmail.com'));

-- Oekraïne → Alvar
UPDATE rooms
SET occupant_id = (SELECT id FROM users WHERE email = 'ikheetalvar@gmail.com')
WHERE name = 'Oekraïne'
  AND (occupant_id IS DISTINCT FROM (SELECT id FROM users WHERE email = 'ikheetalvar@gmail.com'));

-- Thailand → Desmond
UPDATE rooms
SET occupant_id = (SELECT id FROM users WHERE email = 'desmondstaal@gmail.com')
WHERE name = 'Thailand'
  AND (occupant_id IS DISTINCT FROM (SELECT id FROM users WHERE email = 'desmondstaal@gmail.com'));

-- Frankrijk → Medoc
UPDATE rooms
SET occupant_id = (SELECT id FROM users WHERE email = 'medocstaal@gmail.com')
WHERE name = 'Frankrijk'
  AND (occupant_id IS DISTINCT FROM (SELECT id FROM users WHERE email = 'medocstaal@gmail.com'));

-- Argentinië → Simon
UPDATE rooms
SET occupant_id = (SELECT id FROM users WHERE email = 'simontalsma2@gmail.com')
WHERE name = 'Argentinië'
  AND (occupant_id IS DISTINCT FROM (SELECT id FROM users WHERE email = 'simontalsma2@gmail.com'));

-- Japan blijft leeg

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
-- PAYMENTS
-- ==================================================

INSERT INTO payments (amount, created_at, paid_at, status, description, student_id)
SELECT
  350.00, NOW() - INTERVAL '1 month', NOW() - INTERVAL '1 month', 'PAID',
  'Rent - Alvar (previous month)',
  (SELECT id FROM users WHERE email = 'ikheetalvar@gmail.com')
WHERE NOT EXISTS (SELECT 1 FROM payments WHERE description = 'Rent - Alvar (previous month)');

INSERT INTO payments (amount, created_at, paid_at, status, description, student_id)
SELECT
  350.00, NOW() - INTERVAL '1 month', NOW() - INTERVAL '1 month', 'PAID',
  'Rent - Medoc (previous month)',
  (SELECT id FROM users WHERE email = 'medocstaal@gmail.com')
WHERE NOT EXISTS (SELECT 1 FROM payments WHERE description = 'Rent - Medoc (previous month)');

INSERT INTO payments (amount, created_at, paid_at, status, description, student_id)
SELECT
  350.00, NOW() - INTERVAL '1 month', NOW() - INTERVAL '1 month', 'PAID',
  'Rent - Simon (previous month)',
  (SELECT id FROM users WHERE email = 'simontalsma2@gmail.com')
WHERE NOT EXISTS (SELECT 1 FROM payments WHERE description = 'Rent - Simon (previous month)');

INSERT INTO payments (amount, created_at, paid_at, status, description, student_id)
SELECT
  350.00, NOW(), NULL, 'OPEN',
  'Rent - Desmond (current month)',
  (SELECT id FROM users WHERE email = 'desmondstaal@gmail.com')
WHERE NOT EXISTS (SELECT 1 FROM payments WHERE description = 'Rent - Desmond (current month)');

-- ==================================================
-- INVOICES
-- ==================================================

INSERT INTO invoices (title, description, amount, issue_date, due_date, invoice_month, invoice_year, reminder_count, status, student_id)
SELECT
  'Rent current month - Alvar', 'Monthly rent invoice - Alvar', 350.00,
  CURRENT_DATE, CURRENT_DATE + INTERVAL '14 day',
  CAST(EXTRACT(MONTH FROM CURRENT_DATE) AS int), CAST(EXTRACT(YEAR FROM CURRENT_DATE) AS int),
  0, 'OPEN',
  (SELECT id FROM users WHERE email = 'ikheetalvar@gmail.com')
WHERE NOT EXISTS (SELECT 1 FROM invoices WHERE title = 'Rent current month - Alvar');

INSERT INTO invoices (title, description, amount, issue_date, due_date, invoice_month, invoice_year, reminder_count, status, student_id)
SELECT
  'Rent current month - Desmond', 'Monthly rent invoice - Desmond', 350.00,
  CURRENT_DATE, CURRENT_DATE + INTERVAL '14 day',
  CAST(EXTRACT(MONTH FROM CURRENT_DATE) AS int), CAST(EXTRACT(YEAR FROM CURRENT_DATE) AS int),
  0, 'OPEN',
  (SELECT id FROM users WHERE email = 'desmondstaal@gmail.com')
WHERE NOT EXISTS (SELECT 1 FROM invoices WHERE title = 'Rent current month - Desmond');

INSERT INTO invoices (title, description, amount, issue_date, due_date, invoice_month, invoice_year, reminder_count, status, student_id)
SELECT
  'Rent current month - Medoc', 'Monthly rent invoice - Medoc', 350.00,
  CURRENT_DATE, CURRENT_DATE + INTERVAL '14 day',
  CAST(EXTRACT(MONTH FROM CURRENT_DATE) AS int), CAST(EXTRACT(YEAR FROM CURRENT_DATE) AS int),
  0, 'OPEN',
  (SELECT id FROM users WHERE email = 'medocstaal@gmail.com')
WHERE NOT EXISTS (SELECT 1 FROM invoices WHERE title = 'Rent current month - Medoc');

INSERT INTO invoices (title, description, amount, issue_date, due_date, invoice_month, invoice_year, reminder_count, status, student_id)
SELECT
  'Rent current month - Simon', 'Monthly rent invoice - Simon', 350.00,
  CURRENT_DATE, CURRENT_DATE + INTERVAL '14 day',
  CAST(EXTRACT(MONTH FROM CURRENT_DATE) AS int), CAST(EXTRACT(YEAR FROM CURRENT_DATE) AS int),
  0, 'OPEN',
  (SELECT id FROM users WHERE email = 'simontalsma2@gmail.com')
WHERE NOT EXISTS (SELECT 1 FROM invoices WHERE title = 'Rent current month - Simon');

-- ==================================================
-- CLEANING TASKS
-- 6 tasks × 6 rotation weeks = 36 rows
-- Rotation: rotationWeek = ((isoWeek - 1) % 6) + 1
--
-- Students (index 0–5):
--   0 = Arwen   (arwenleonor@gmail.com)
--   1 = Alvar   (ikheetalvar@gmail.com)
--   2 = Desmond (desmondstaal@gmail.com)
--   3 = Medoc   (medocstaal@gmail.com)
--   4 = Simon   (simontalsma2@gmail.com)
--   5 = Japan   (room empty — assigned_to_id = NULL)
--
-- Tasks (index 0–5):
--   0 = Kitchen & dishwasher
--   1 = Bathroom & toilet
--   2 = Take out the trash
--   3 = Hallway & stairs
--   4 = Living room
--   5 = Garden & outdoor
--
-- Rule: student[i] gets task[(i + rotationWeek - 1) % 6]
-- ==================================================

-- Clean up old seed data so the rotation is always correct
DELETE FROM cleaning_tasks WHERE week_number BETWEEN 1 AND 6;

-- ── Week 1 ────────────────────────────────────────
-- Arwen=Kitchen, Alvar=Bathroom, Desmond=Trash, Medoc=Hallway, Simon=Living room, Japan=Garden
INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access) VALUES
  (1, 'Kitchen & dishwasher',    'Empty the dishwasher, clean the countertop, oven and induction plate.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'arwenleonor@gmail.com'),  'ALL'),
  (1, 'Bathroom & toilet',       'Clean the sink, shower, mirror and toilet thoroughly and dry everything.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'ikheetalvar@gmail.com'),   'ALL'),
  (1, 'Take out the trash',      'Separate: organic, plastics/metal, paper/cardboard, residual waste, deposit bottles.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'desmondstaal@gmail.com'), 'ALL'),
  (1, 'Hallway & stairs',        'Vacuum and mop the hallway and stairs. Keep the dining table clean and tidy.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'medocstaal@gmail.com'),   'ALL'),
  (1, 'Living room',             'Vacuum and mop the living room. Dust the shelves and keep surfaces tidy.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'simontalsma2@gmail.com'), 'ALL'),
  (1, 'Garden & outdoor',        'Tidy the garden, sweep the terrace and clear the outdoor area.', FALSE, NULL, NULL, NULL, 'ALL');

-- ── Week 2 ────────────────────────────────────────
-- Arwen=Bathroom, Alvar=Trash, Desmond=Hallway, Medoc=Living room, Simon=Garden, Japan=Kitchen
INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access) VALUES
  (2, 'Kitchen & dishwasher',    'Empty the dishwasher, clean the countertop, oven and induction plate.', FALSE, NULL, NULL, NULL, 'ALL'),
  (2, 'Bathroom & toilet',       'Clean the sink, shower, mirror and toilet thoroughly and dry everything.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'arwenleonor@gmail.com'),  'ALL'),
  (2, 'Take out the trash',      'Separate: organic, plastics/metal, paper/cardboard, residual waste, deposit bottles.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'ikheetalvar@gmail.com'),   'ALL'),
  (2, 'Hallway & stairs',        'Vacuum and mop the hallway and stairs. Keep the dining table clean and tidy.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'desmondstaal@gmail.com'), 'ALL'),
  (2, 'Living room',             'Vacuum and mop the living room. Dust the shelves and keep surfaces tidy.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'medocstaal@gmail.com'),   'ALL'),
  (2, 'Garden & outdoor',        'Tidy the garden, sweep the terrace and clear the outdoor area.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'simontalsma2@gmail.com'), 'ALL');

-- ── Week 3 ────────────────────────────────────────
-- Arwen=Trash, Alvar=Hallway, Desmond=Living room, Medoc=Garden, Simon=Kitchen, Japan=Bathroom
INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access) VALUES
  (3, 'Kitchen & dishwasher',    'Empty the dishwasher, clean the countertop, oven and induction plate.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'simontalsma2@gmail.com'), 'ALL'),
  (3, 'Bathroom & toilet',       'Clean the sink, shower, mirror and toilet thoroughly and dry everything.', FALSE, NULL, NULL, NULL, 'ALL'),
  (3, 'Take out the trash',      'Separate: organic, plastics/metal, paper/cardboard, residual waste, deposit bottles.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'arwenleonor@gmail.com'),  'ALL'),
  (3, 'Hallway & stairs',        'Vacuum and mop the hallway and stairs. Keep the dining table clean and tidy.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'ikheetalvar@gmail.com'),   'ALL'),
  (3, 'Living room',             'Vacuum and mop the living room. Dust the shelves and keep surfaces tidy.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'desmondstaal@gmail.com'), 'ALL'),
  (3, 'Garden & outdoor',        'Tidy the garden, sweep the terrace and clear the outdoor area.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'medocstaal@gmail.com'),   'ALL');

-- ── Week 4 ────────────────────────────────────────
-- Arwen=Hallway, Alvar=Living room, Desmond=Garden, Medoc=Kitchen, Simon=Bathroom, Japan=Trash
INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access) VALUES
  (4, 'Kitchen & dishwasher',    'Empty the dishwasher, clean the countertop, oven and induction plate.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'medocstaal@gmail.com'),   'ALL'),
  (4, 'Bathroom & toilet',       'Clean the sink, shower, mirror and toilet thoroughly and dry everything.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'simontalsma2@gmail.com'), 'ALL'),
  (4, 'Take out the trash',      'Separate: organic, plastics/metal, paper/cardboard, residual waste, deposit bottles.', FALSE, NULL, NULL, NULL, 'ALL'),
  (4, 'Hallway & stairs',        'Vacuum and mop the hallway and stairs. Keep the dining table clean and tidy.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'arwenleonor@gmail.com'),  'ALL'),
  (4, 'Living room',             'Vacuum and mop the living room. Dust the shelves and keep surfaces tidy.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'ikheetalvar@gmail.com'),   'ALL'),
  (4, 'Garden & outdoor',        'Tidy the garden, sweep the terrace and clear the outdoor area.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'desmondstaal@gmail.com'), 'ALL');

-- ── Week 5 ────────────────────────────────────────
-- Arwen=Living room, Alvar=Garden, Desmond=Kitchen, Medoc=Bathroom, Simon=Trash, Japan=Hallway
INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access) VALUES
  (5, 'Kitchen & dishwasher',    'Empty the dishwasher, clean the countertop, oven and induction plate.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'desmondstaal@gmail.com'), 'ALL'),
  (5, 'Bathroom & toilet',       'Clean the sink, shower, mirror and toilet thoroughly and dry everything.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'medocstaal@gmail.com'),   'ALL'),
  (5, 'Take out the trash',      'Separate: organic, plastics/metal, paper/cardboard, residual waste, deposit bottles.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'simontalsma2@gmail.com'), 'ALL'),
  (5, 'Hallway & stairs',        'Vacuum and mop the hallway and stairs. Keep the dining table clean and tidy.', FALSE, NULL, NULL, NULL, 'ALL'),
  (5, 'Living room',             'Vacuum and mop the living room. Dust the shelves and keep surfaces tidy.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'arwenleonor@gmail.com'),  'ALL'),
  (5, 'Garden & outdoor',        'Tidy the garden, sweep the terrace and clear the outdoor area.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'ikheetalvar@gmail.com'),   'ALL');

-- ── Week 6 ────────────────────────────────────────
-- Arwen=Garden, Alvar=Kitchen, Desmond=Bathroom, Medoc=Trash, Simon=Hallway, Japan=Living room
INSERT INTO cleaning_tasks (week_number, name, description, completed, comment, incident_report, assigned_to_id, role_access) VALUES
  (6, 'Kitchen & dishwasher',    'Empty the dishwasher, clean the countertop, oven and induction plate.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'ikheetalvar@gmail.com'),   'ALL'),
  (6, 'Bathroom & toilet',       'Clean the sink, shower, mirror and toilet thoroughly and dry everything.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'desmondstaal@gmail.com'), 'ALL'),
  (6, 'Take out the trash',      'Separate: organic, plastics/metal, paper/cardboard, residual waste, deposit bottles.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'medocstaal@gmail.com'),   'ALL'),
  (6, 'Hallway & stairs',        'Vacuum and mop the hallway and stairs. Keep the dining table clean and tidy.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'simontalsma2@gmail.com'), 'ALL'),
  (6, 'Living room',             'Vacuum and mop the living room. Dust the shelves and keep surfaces tidy.', FALSE, NULL, NULL, NULL, 'ALL'),
  (6, 'Garden & outdoor',        'Tidy the garden, sweep the terrace and clear the outdoor area.', FALSE, NULL, NULL, (SELECT id FROM users WHERE email = 'arwenleonor@gmail.com'),  'ALL');

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